package br.com.aps.util;

import br.com.aps.controller.*;
import br.com.aps.dao.*;
import br.com.aps.view.*;
import org.flywaydb.core.Flyway;
import java.sql.*;
import java.time.LocalDate;
import java.nio.file.*;
import javax.swing.*;

/** Teste destrutivo apenas para banco ISOLADO vazio aps_teste_correcao na porta 33317. */
public class AtualizacaoBancoCheck {
    static void check(boolean ok, String texto) {
        if (!ok) throw new AssertionError(texto);
        System.out.println("OK: " + texto);
    }
    static void recusa(Runnable acao) {
        try { acao.run(); } catch (RegraNegocioException esperada) { return; }
        throw new AssertionError("Operação inválida aceita");
    }
    private static void layout(java.awt.Container c) {
        c.doLayout();
        for (var filho : c.getComponents()) if (filho instanceof java.awt.Container container) layout(container);
    }
    public static void main(String[] args) throws Exception {
        var config = DatabaseConfig.load();
        if (!config.url().startsWith("jdbc:mysql://127.0.0.1:33317/aps_teste_correcao?"))
            throw new IllegalStateException("Use exclusivamente o banco isolado de teste.");
        var original = Flyway.configure().dataSource(config.url(), config.user(), config.password())
            .locations("classpath:db/migration").target("3").load();
        original.migrate();
        try (var c = ConnectionFactory.getConnection(); var st = c.createStatement()) {
            st.executeUpdate("INSERT INTO usuario(nome,email,area_profissao,matricula) VALUES ('Pessoa antiga','antiga@teste.com','Engenharia','ANTIGA')");
            st.executeUpdate("INSERT INTO equipamento(patrimonio,nome,categoria,estado,modelo,marca,numero_serie) VALUES ('ANTIGO','Notebook antigo','Notebook','BOM','Modelo antigo','Marca antiga','SERIE-ANTIGA')");
            st.executeUpdate("INSERT INTO emprestimo(usuario_id,equipamento_id,data_retirada,data_prevista,data_devolucao,status) VALUES (1,1,'2026-01-01','2026-01-03','2026-01-02','DEVOLVIDO')");
        }
        DatabaseMigrator.migrate();
        DatabaseMigrator.migrate();
        try (var c = ConnectionFactory.getConnection(); var st = c.createStatement();
             var rs = st.executeQuery("SELECT checksum FROM flyway_schema_history WHERE version='3'")) {
            check(rs.next() && rs.getInt(1) == 1634580469, "V3 original preservada; V4 aplicada e reinicialização válida");
        }
        var ud = new UsuarioDAO(); var ed = new EquipamentoDAO();
        check("Engenharia".equals(ud.buscarPorId(1).getAreaProfissao()), "Pessoa antiga preservada");
        check("SERIE-ANTIGA".equals(ed.buscarPorId(1).getNumeroSerie()), "Equipamento antigo preservado");
        var usuarios = new UsuarioController(); var equipamentos = new EquipamentoController();
        var emprestimos = new EmprestimoController();
        var u = usuarios.cadastrar("Pessoa nova", "nova@teste.com", "Analista de TI", "NOVA");
        usuarios.atualizar(u.getId(), "Pessoa editada", u.getEmail(), "Suporte técnico", true, "NOVA-2");
        check("NOVA-2".equals(ud.buscarPorId(u.getId()).getMatricula()), "Cadastro e edição de matrícula");
        recusa(() -> usuarios.cadastrar("Duplicada", "outra@teste.com", "Área", "NOVA-2"));
        var e = equipamentos.cadastrar("NOVO", "Notebook novo", "Notebook", "Bom", "Latitude", "Dell", "SN-NOVO", "Windows", "TI");
        equipamentos.atualizar(e.getId(), "NOVO", "Notebook editado", "Notebook", "Bom", "Latitude 2", "Dell", "SN-NOVO", "Linux", "Suporte");
        equipamentos.alterarStatus(e.getId(), "INDISPONIVEL");
        equipamentos.alterarStatus(e.getId(), "DISPONIVEL");
        check("Linux".equals(ed.buscarPorId(e.getId()).getSistemaOperacional()), "Edição e status preservam campos do equipamento");
        recusa(() -> equipamentos.cadastrar("DUP", "Duplicado", "Notebook", "Bom", "M", "M", "SN-NOVO", "", ""));
        var emp = emprestimos.emprestar(u.getId(), e.getId(), LocalDate.now().plusDays(7));
        recusa(() -> emprestimos.emprestar(u.getId(), e.getId(), LocalDate.now().plusDays(7)));
        // Uma falha no detalhe deve desfazer toda a devolução.
        try {
            new EmprestimoDAO().registrarDevolucao(emp.getId(), java.time.LocalDateTime.now(), "BOM", "x".repeat(256));
            throw new AssertionError("Observação excessiva foi aceita");
        } catch (SQLException esperado) {}
        check("ABERTO".equals(new EmprestimoDAO().buscarPorId(emp.getId()).getStatus())
            && "EMPRESTADO".equals(ed.buscarPorId(e.getId()).getStatus()), "Rollback da devolução preserva empréstimo e equipamento");
        emprestimos.devolver(emp.getId(), "Regular", "Marcas de uso", "Operador de teste");
        recusa(() -> emprestimos.devolver(emp.getId(), "Bom", "Duplicada", "Operador de teste"));
        var relatorio = new DevolucaoDAO().listarDetalhado();
        check(relatorio.size() == 2 && relatorio.stream().anyMatch(d -> "Marcas de uso".equals(d.observacao())), "Relatório persistido e recuperado em nova conexão");
        check(relatorio.stream().anyMatch(d -> d.estado() == null), "Devolução antiga incluída sem inventar estado histórico");
        var modelo = new javax.swing.table.DefaultTableModel(new Object[][]{{"=1+1", "ação;\"teste\""}}, new String[]{"Pessoa", "Observações"});
        var csv = Files.createTempFile("aps-relatorio-", ".csv");
        ExportadorRelatorio.salvar(modelo, csv);
        check(Files.readString(csv).contains("'=1+1") && Files.readString(csv).contains("ação;\"\"teste\"\""), "CSV UTF-8 com escape e proteção de fórmulas");
        Files.delete(csv);
        SwingUtilities.invokeAndWait(() -> {
            Estilo.instalar();
            JPanel[] telas = {new CadastroPessoas(), new CadastroEquipamentos(), new TelaRelatorios(), new TelaConsultar(), new TelaEmprestimos(), new TelaDevolucao()};
            for (var tela : telas) {
                check(tela.getComponentCount() > 0, "Tela carregada: " + tela.getClass().getSimpleName());
                String pasta = System.getProperty("aps.test.shots");
                if (pasta != null) {
                    tela.setSize(1200, 600);
                    layout(tela);
                    var img = new java.awt.image.BufferedImage(1200, 600, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    var g = img.createGraphics(); g.setColor(Estilo.FUNDO); g.fillRect(0, 0, 1200, 600); tela.printAll(g); g.dispose();
                    try { javax.imageio.ImageIO.write(img, "png", new java.io.File(pasta, tela.getClass().getSimpleName() + ".png")); }
                    catch (java.io.IOException erro) { throw new RuntimeException(erro); }
                }
            }
        });
        System.out.println("ATUALIZACAO E FLUXO COMPLETO: OK");
        System.exit(0);
    }
}
