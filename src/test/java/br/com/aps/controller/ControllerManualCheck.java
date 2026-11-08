package br.com.aps.controller;

import br.com.aps.dao.EmprestimoDAO;
import br.com.aps.model.DevolucaoDetalhe;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.Equipamento;
import br.com.aps.model.Usuario;
import br.com.aps.util.ConnectionFactory;
import br.com.aps.util.DatabaseMigrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Teste de integracao manual (exige MySQL local): exercita os Controllers com o banco real e confere
 * cada regra do Lucas (README e banco), as validacoes de tamanho e valor, o relatorio de devolucao
 * e o historico. Cria registros com codigo unico e remove todos ao final.
 * Preparacao: database/create_database.sql e src/main/resources/application.properties.
 */
public class ControllerManualCheck {
    static final List<Integer> usuariosCriados = new ArrayList<>();
    static final List<Integer> equipamentosCriados = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        DatabaseMigrator.migrate();

        UsuarioController usuarios = new UsuarioController();
        EquipamentoController equipamentos = new EquipamentoController();
        EmprestimoController emprestimos = new EmprestimoController();
        String codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try {
            // ---------- pessoas: validacoes
            recusa(() -> usuarios.cadastrar("N".repeat(101), "a@b.com", "ALUNO"), "no máximo 100 caracteres");
            recusa(() -> usuarios.cadastrar("", "a@b.com", "ALUNO"), "Informe o campo nome");
            recusa(() -> usuarios.cadastrar("Ana", "sem-arroba", "ALUNO"), "e-mail válido");
            recusa(() -> usuarios.cadastrar("Ana", "a".repeat(120) + "@b.com", "ALUNO"), "no máximo 120 caracteres");
            recusa(() -> usuarios.cadastrar("Ana", "a@b.com", ""), "Informe o campo área/profissão");

            Usuario ana = usuarios.cadastrar("Ana " + codigo, "ana." + codigo + "@teste.com", "Funcionário");
            usuariosCriados.add(ana.getId());
            verificar(ana.getId() > 0 && ana.isAtivo() && "Funcionário".equals(buscar(usuarios, ana.getId()).getAreaProfissao()),
                "pessoa nova nasce ativa; a área/profissão preserva o texto informado");
            recusa(() -> usuarios.cadastrar("Outra", "ANA." + codigo + "@TESTE.COM", "ALUNO"), "Já existe uma pessoa com esse e-mail");
            Usuario bruno = usuarios.cadastrar("Bruno " + codigo, "bruno." + codigo + "@teste.com", "PROFESSOR");
            usuariosCriados.add(bruno.getId());
            Usuario carla = usuarios.cadastrar("Carla " + codigo, "carla." + codigo + "@teste.com", "ALUNO");
            usuariosCriados.add(carla.getId());
            usuarios.atualizar(carla.getId(), carla.getNome(), carla.getEmail(), "ALUNO", false);
            verificar(!buscar(usuarios, carla.getId()).isAtivo(), "pessoa pode ser inativada");

            // ---------- equipamentos: validacoes
            recusa(() -> equipamentos.cadastrar("P".repeat(51), "Nome", "Notebook", "BOM"), "no máximo 50 caracteres");
            recusa(() -> equipamentos.cadastrar("PAT-" + codigo, "Nome", "", "BOM"), "Informe o campo categoria");
            recusa(() -> equipamentos.cadastrar("PAT-" + codigo, "Nome", "Notebook", "QUEBRADO"), "inválido para estado");
            Equipamento notebook = equipamentos.cadastrar("PAT-" + codigo + "-1", "Notebook " + codigo, "Notebook", "Bom");
            equipamentosCriados.add(notebook.getId());
            verificar(Equipamento.STATUS_DISPONIVEL.equals(buscarEq(equipamentos, notebook.getId()).getStatus())
                    && "BOM".equals(buscarEq(equipamentos, notebook.getId()).getEstado()),
                "equipamento novo nasce DISPONIVEL; estado vai ao banco como BOM");
            recusa(() -> equipamentos.cadastrar("pat-" + codigo + "-1", "Outro", "Notebook", "NOVO"), "Já existe um equipamento com esse patrimônio");
            Equipamento celular = equipamentos.cadastrar("PAT-" + codigo + "-2", "Celular " + codigo, "Celular", "NOVO");
            equipamentosCriados.add(celular.getId());
            Equipamento monitor = equipamentos.cadastrar("PAT-" + codigo + "-3", "Monitor " + codigo, "Monitor", "REGULAR");
            equipamentosCriados.add(monitor.getId());

            // ---------- status manual: Indisponivel <-> Disponivel
            equipamentos.alterarStatus(monitor.getId(), Equipamento.STATUS_INDISPONIVEL);
            verificar(Equipamento.STATUS_INDISPONIVEL.equals(buscarEq(equipamentos, monitor.getId()).getStatus()), "DISPONIVEL -> INDISPONIVEL");
            recusa(() -> equipamentos.alterarStatus(monitor.getId(), Equipamento.STATUS_EMPRESTADO), "Disponível ou Indisponível");

            // ---------- emprestimo: regras 1 a 4
            LocalDate daquiA7 = LocalDate.now().plusDays(7);
            recusa(() -> emprestimos.emprestar(carla.getId(), notebook.getId(), daquiA7), "precisa estar ativa");        // regra 1
            recusa(() -> emprestimos.emprestar(ana.getId(), monitor.getId(), daquiA7), "status Disponível");             // regra 2
            recusa(() -> emprestimos.emprestar(ana.getId(), notebook.getId(), LocalDate.now().minusDays(1)), "anterior à retirada");
            recusa(() -> emprestimos.emprestar(ana.getId(), notebook.getId(), null), "Informe a devolução prevista");

            Emprestimo daAna = emprestimos.emprestar(ana.getId(), notebook.getId(), daquiA7);
            verificar(daAna.getId() > 0 && Emprestimo.STATUS_ABERTO.equals(daAna.getStatus()), "regra 4: o empréstimo fica ABERTO");
            verificar(Equipamento.STATUS_EMPRESTADO.equals(buscarEq(equipamentos, notebook.getId()).getStatus()), "regra 4: o equipamento passa para EMPRESTADO");
            recusa(() -> emprestimos.emprestar(bruno.getId(), notebook.getId(), daquiA7), "status Disponível");            // regras 2/3
            recusa(() -> equipamentos.alterarStatus(notebook.getId(), Equipamento.STATUS_INDISPONIVEL), "só muda quando a devolução");
            Emprestimo hoje = emprestimos.emprestar(ana.getId(), celular.getId(), LocalDate.now()); // hoje vale ate o fim do dia
            verificar(!hoje.isAtrasado(), "devolução prevista para hoje vale até o fim do dia (não está atrasado)");
            verificar(emprestimos.abertos().stream().filter(d -> d.emprestimo().getUsuarioId() == ana.getId()).count() == 2,
                "o Lucas não limita equipamentos por pessoa: a Ana está com 2");

            // atualizar cadastro de equipamento emprestado: status continua EMPRESTADO
            equipamentos.atualizar(notebook.getId(), "PAT-" + codigo + "-1", "Notebook Editado " + codigo, "Notebook", "REGULAR");
            Equipamento editado = buscarEq(equipamentos, notebook.getId());
            verificar(Equipamento.STATUS_EMPRESTADO.equals(editado.getStatus()) && "REGULAR".equals(editado.getEstado()),
                "editar o cadastro não muda o status (continua EMPRESTADO)");

            // inativar quem esta com equipamento: permitido
            verificar(usuarios.possuiEmprestimoAberto(ana.getId()), "a Ana está com empréstimo aberto");

            // ---------- atrasado e calculado pela data (emprestimo vencido criado direto no DAO)
            Emprestimo vencido = new Emprestimo(bruno.getId(), monitor.getId(),
                LocalDateTime.now().minusDays(5).withNano(0), LocalDateTime.now().minusDays(2).withNano(0));
            equipamentos.alterarStatus(monitor.getId(), Equipamento.STATUS_DISPONIVEL);
            new EmprestimoDAO().inserir(vencido);
            verificar("Atrasado".equals(Regras.situacao(vencido)) && new EmprestimoDAO().listarAtrasados().stream().anyMatch(e -> e.getId() == vencido.getId()),
                "ATRASADO é calculado: ABERTO com a data prevista vencida");

            // ---------- historico (empréstimos aparecem; devolucoes aparecem depois)
            verificar(emprestimos.historico().stream().anyMatch(e -> e.acao().equals("Empréstimo") && e.pessoa().equals(ana.getNome())),
                "o histórico lista o empréstimo");

            // ---------- devolucao: regra 5 + estado
            recusa(() -> emprestimos.devolver(daAna.getId(), "QUEBRADO", "", "Teste"), "inválido para estado do equipamento");
            recusa(() -> emprestimos.devolver(daAna.getId(), "BOM", "", ""), "responsável pelo registro");
            recusa(() -> emprestimos.devolver(daAna.getId(), "BOM", "x".repeat(256), "Teste"), "no máximo 255 caracteres");
            recusa(() -> emprestimos.devolver(999999999, "BOM", "", "Teste"), "não encontrado");
            emprestimos.devolver(daAna.getId(), "Danificado", "Tela trincada " + codigo, "Operador de teste");
            Equipamento depois = buscarEq(equipamentos, notebook.getId());
            verificar(Equipamento.STATUS_DISPONIVEL.equals(depois.getStatus()) && Equipamento.ESTADO_DANIFICADO.equals(depois.getEstado()),
                "regra 5: o equipamento volta para DISPONIVEL com o estado informado (DANIFICADO)");
            Emprestimo devolvido = new EmprestimoDAO().buscarPorId(daAna.getId());
            verificar(Emprestimo.STATUS_DEVOLVIDO.equals(devolvido.getStatus()) && devolvido.getDataDevolucao() != null,
                "regra 5: o empréstimo fica DEVOLVIDO com a data de devolução preenchida");
            recusa(() -> emprestimos.devolver(daAna.getId(), "BOM", "", "Teste"), "já foi devolvido");

            emprestimos.devolver(hoje.getId(), "Bom", "", "Operador de teste");
            emprestimos.devolver(vencido.getId(), "Regular", "Teclado com tecla solta " + codigo, "Operador de teste");
            List<DevolucaoDetalhe> devolucoes = emprestimos.devolucoes();
            DevolucaoDetalhe registro = devolucoes.stream().filter(d -> d.emprestimoId() == daAna.getId()).findFirst().orElseThrow();
            verificar("DANIFICADO".equals(registro.estado()) && ("Tela trincada " + codigo).equals(registro.observacao())
                    && registro.usuarioNome().equals(ana.getNome()), "o relatório guarda estado e observação da devolução");
            DevolucaoDetalhe semObs = devolucoes.stream().filter(d -> d.emprestimoId() == hoje.getId()).findFirst().orElseThrow();
            verificar(semObs.observacao() == null && "BOM".equals(semObs.estado()), "observação em branco fica vazia no banco");
            verificar(emprestimos.historico().stream().anyMatch(e -> e.acao().equals("Devolução") && e.pessoa().equals(ana.getNome())),
                "o histórico lista a devolução");
            verificar(emprestimos.historico().get(0).quando().compareTo(emprestimos.historico().get(1).quando()) >= 0,
                "o histórico vem do mais novo para o mais antigo");

            // ---------- regra 6: com historico nao exclui
            recusa(() -> usuarios.excluir(ana.getId()), "histórico de empréstimos");
            recusa(() -> equipamentos.excluir(notebook.getId()), "histórico de empréstimos");
            // inativar quem tem historico e permitido; quem nao tem historico pode ser excluido
            usuarios.atualizar(ana.getId(), ana.getNome(), ana.getEmail(), "FUNCIONARIO", false);
            verificar(!buscar(usuarios, ana.getId()).isAtivo(), "quem tem histórico pode ser inativado");
            Equipamento semHistorico = equipamentos.cadastrar("PAT-" + codigo + "-4", "Sem historico " + codigo, "Outro", "BOM");
            equipamentosCriados.add(semHistorico.getId());
            equipamentos.excluir(semHistorico.getId());
            verificar(equipamentos.listar().stream().noneMatch(e -> e.getId() == semHistorico.getId()), "quem não tem histórico pode ser excluído");

            System.out.println("Controllers: verificacoes concluidas com sucesso.");
        } finally {
            limpar();
        }
    }

    private static Usuario buscar(UsuarioController c, int id) {
        return c.listar().stream().filter(u -> u.getId() == id).findFirst().orElseThrow();
    }

    private static Equipamento buscarEq(EquipamentoController c, int id) {
        return c.listar().stream().filter(e -> e.getId() == id).findFirst().orElseThrow();
    }

    private static void recusa(Runnable operacao, String trecho) {
        try {
            operacao.run();
        } catch (RegraNegocioException erro) {
            if (!erro.getMessage().contains(trecho)) {
                throw new AssertionError("Mensagem inesperada: \"" + erro.getMessage() + "\" (esperava \"" + trecho + "\")");
            }
            System.out.println("OK: recusado com \"" + trecho + "\"");
            return;
        }
        throw new AssertionError("Deveria recusar: " + trecho);
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError(descricao);
        }
        System.out.println("OK: " + descricao);
    }

    private static void limpar() throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            for (int id : equipamentosCriados) {
                apagar(c, "DELETE FROM devolucao WHERE emprestimo_id IN (SELECT id FROM emprestimo WHERE equipamento_id = ?)", id);
                apagar(c, "DELETE FROM emprestimo WHERE equipamento_id = ?", id);
            }
            for (int id : usuariosCriados) {
                apagar(c, "DELETE FROM devolucao WHERE emprestimo_id IN (SELECT id FROM emprestimo WHERE usuario_id = ?)", id);
                apagar(c, "DELETE FROM emprestimo WHERE usuario_id = ?", id);
            }
            for (int id : equipamentosCriados) {
                apagar(c, "DELETE FROM equipamento WHERE id = ?", id);
            }
            for (int id : usuariosCriados) {
                apagar(c, "DELETE FROM usuario WHERE id = ?", id);
            }
        }
    }

    private static void apagar(Connection c, String sql, int id) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }
}
