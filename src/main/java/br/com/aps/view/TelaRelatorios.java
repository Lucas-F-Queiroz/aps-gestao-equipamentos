package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;
import br.com.aps.model.Usuario;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

/**
 * Relatorios: escolha o tipo (Cadastros, Emprestimos, Equipamentos ou Geral) e veja o resultado.
 * Nos tres primeiros ha busca, filtros e exportacao CSV do que esta na tela; o Geral e a consulta
 * geral em numeros. Nenhum dado e alterado.
 */
public class TelaRelatorios extends JPanel {
    private static final String[][] TIPOS = {
        {"Cadastros", "Lista de pessoas cadastradas"},
        {"Empréstimos", "Histórico de empréstimos e devoluções"},
        {"Equipamentos", "Lista de equipamentos cadastrados"},
        {"Geral", "Relatório completo do sistema"}
    };

    private final JRadioButton[] opcoes = new JRadioButton[TIPOS.length];
    private final JPanel[] paineisDasOpcoes = new JPanel[TIPOS.length];
    private final JPanel resultado = new JPanel(new BorderLayout());

    public TelaRelatorios() {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        // ---------- cabecalho: titulo e escolha do tipo de relatorio
        JPanel topo = Estilo.cartao();
        JLabel titulo = new JLabel("<html><b style='font-size:20px'>Relatórios</b><br>"
            + "<span style='color:#64748B'>Selecione o tipo de relatório, aplique os filtros e exporte, se quiser.</span></html>",
            new Icone(Icone.RELATORIO, Estilo.AZUL, Color.WHITE, 52, true), JLabel.LEFT);
        titulo.setIconTextGap(14);
        topo.add(titulo, BorderLayout.NORTH);

        ButtonGroup grupo = new ButtonGroup(); // so um tipo fica marcado
        JPanel linha = new JPanel(new GridLayout(1, TIPOS.length, 12, 0));
        linha.setOpaque(false);
        for (int i = 0; i < TIPOS.length; i++) {
            final int tipo = i;
            opcoes[i] = new JRadioButton("<html><b>" + TIPOS[i][0] + "</b><br>"
                + "<span style='color:#64748B'>" + TIPOS[i][1] + "</span></html>");
            opcoes[i].setOpaque(false);
            opcoes[i].addActionListener(e -> mostrar(tipo));
            grupo.add(opcoes[i]);

            paineisDasOpcoes[i] = new JPanel(new BorderLayout());
            paineisDasOpcoes[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDA, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            paineisDasOpcoes[i].add(opcoes[i], BorderLayout.CENTER);
            linha.add(paineisDasOpcoes[i]);
        }
        topo.add(Estilo.campo("Tipo de relatório", linha), BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        resultado.setOpaque(false);
        add(resultado, BorderLayout.CENTER);
        mostrar(0);
    }

    /** Marca a opcao escolhida e troca o resultado que aparece embaixo. */
    private void mostrar(int tipo) {
        for (int i = 0; i < TIPOS.length; i++) {
            opcoes[i].setSelected(i == tipo);
            paineisDasOpcoes[i].setBackground(i == tipo ? new Color(239, 246, 255) : Color.WHITE);
        }
        try {
            JPanel novo = switch (tipo) {
                case 0 -> relatorioDeCadastros();
                case 1 -> relatorioDeEmprestimos();
                case 2 -> relatorioDeEquipamentos();
                default -> relatorioGeral();
            };
            resultado.removeAll();
            resultado.add(novo, BorderLayout.CENTER);
            resultado.revalidate();
            resultado.repaint();
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    // ------------------------------------------------------------ os quatro relatorios

    private JPanel relatorioDeCadastros() {
        JTable tabela = Estilo.criarTabela(new String[]{"ID", "Nome", "E-mail", "CPF", "Celular", "Função", "Situação"});
        for (Usuario u : Aplicacao.usuarios().listar()) {
            Estilo.modelo(tabela).addRow(new Object[]{u.getId(), u.getNome(), u.getEmail(),
                u.getCpf(), u.getCelular(), Regras.rotulo(u.getTipo()), u.isAtivo() ? "Ativo" : "Inativo"});
        }
        Estilo.larguras(tabela, 50, 200, 230, 130, 130, 140, 120);
        Estilo.colorirSituacao(tabela, 6);
        FiltroTabela filtro = new FiltroTabela(tabela).adicionarFiltro("Função", 5).adicionarFiltro("Situação", 6);
        return resultado("Pessoas cadastradas", tabela, filtro, "relatorio-cadastros.csv");
    }

    private JPanel relatorioDeEmprestimos() {
        JTable tabela = Estilo.criarTabela(new String[]{
            "ID", "Pessoa", "Equipamento", "Retirada", "Devolução prevista", "Devolvido em", "Situação"});
        for (EmprestimoDetalhe d : Aplicacao.emprestimos().listarDetalhado()) {
            Emprestimo e = d.emprestimo();
            Estilo.modelo(tabela).addRow(new Object[]{e.getId(), d.usuarioNome(),
                d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")", Estilo.dia(e.getDataRetirada()),
                Estilo.dia(e.getDataPrevista()), Estilo.dia(e.getDataDevolucao()), Regras.situacao(e)});
        }
        Estilo.larguras(tabela, 50, 170, 320, 120, 140, 140, 110);
        Estilo.colorirSituacao(tabela, 6);
        FiltroTabela filtro = new FiltroTabela(tabela).adicionarFiltro("Situação", 6)
            .adicionarFiltro("Pessoa", 1).adicionarFiltro("Equipamento", 2)
            .adicionarPeriodo("Data do empréstimo", 3);
        return resultado("Histórico de empréstimos e devoluções", tabela, filtro, "relatorio-emprestimos.csv");
    }

    private JPanel relatorioDeEquipamentos() {
        JTable tabela = Estilo.criarTabela(new String[]{"ID", "Patrimônio", "Nome", "Categoria", "Estado", "Status"});
        for (Equipamento e : Aplicacao.equipamentos().listar()) {
            Estilo.modelo(tabela).addRow(new Object[]{e.getId(), e.getPatrimonio(), e.getNome(), e.getCategoria(),
                Regras.rotulo(e.getEstado()), Regras.rotulo(e.getStatus())});
        }
        Estilo.larguras(tabela, 50, 120, 320, 150, 120, 130);
        Estilo.colorirSituacao(tabela, 4);
        Estilo.colorirSituacao(tabela, 5);
        FiltroTabela filtro = new FiltroTabela(tabela).adicionarFiltro("Categoria", 3)
            .adicionarFiltro("Estado", 4).adicionarFiltro("Status", 5);
        return resultado("Equipamentos cadastrados", tabela, filtro, "relatorio-equipamentos.csv");
    }

    /** Geral: os totais do sistema, em tres grupos de cartoes. */
    private JPanel relatorioGeral() {
        List<Equipamento> equipamentos = Aplicacao.equipamentos().listar();
        List<EmprestimoDetalhe> emprestimos = Aplicacao.emprestimos().listarDetalhado();

        JPanel numeros = new JPanel(new GridLayout(3, 1, 0, 16)); // 3 grupos, um embaixo do outro
        numeros.setOpaque(false);
        numeros.add(grupo("Cadastros", 2,
            Estilo.cartaoNumero("Total de usuários", String.valueOf(Aplicacao.usuarios().listar().size()), Estilo.AZUL),
            Estilo.cartaoNumero("Total de equipamentos", String.valueOf(equipamentos.size()), new Color(124, 58, 237))));
        numeros.add(grupo("Situação dos equipamentos", 3,
            Estilo.cartaoNumero("Disponíveis", contar(equipamentos, Equipamento.STATUS_DISPONIVEL), Estilo.VERDE),
            Estilo.cartaoNumero("Emprestados", contar(equipamentos, Equipamento.STATUS_EMPRESTADO), Estilo.AZUL),
            Estilo.cartaoNumero("Indisponíveis", contar(equipamentos, Equipamento.STATUS_INDISPONIVEL), Estilo.LARANJA)));
        numeros.add(grupo("Empréstimos", 2,
            Estilo.cartaoNumero("Empréstimos abertos", contarEmprestimos(emprestimos, Emprestimo.STATUS_ABERTO), Estilo.AZUL),
            Estilo.cartaoNumero("Empréstimos devolvidos", contarEmprestimos(emprestimos, Emprestimo.STATUS_DEVOLVIDO), Estilo.VERDE)));
        return numeros;
    }

    // ------------------------------------------------------------ apoio

    /** Cartao do resultado: busca e filtros em cima, a tabela no meio e o botao de exportar CSV. */
    private JPanel resultado(String titulo, JTable tabela, FiltroTabela filtro, String nomeDoArquivo) {
        JButton exportar = Estilo.botaoPrincipal("Exportar CSV");
        exportar.addActionListener(e -> exportar(tabela, nomeDoArquivo));
        filtro.adicionarBotao(exportar);
        filtro.atualizar();

        JPanel cabecalho = new JPanel(new BorderLayout(0, 10));
        cabecalho.setOpaque(false);
        cabecalho.add(Estilo.subtitulo("Resultado do relatório: " + titulo), BorderLayout.NORTH);
        cabecalho.add(filtro, BorderLayout.CENTER);

        JPanel cartao = Estilo.cartao();
        cartao.add(cabecalho, BorderLayout.NORTH);
        cartao.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        return cartao;
    }

    /** Salva em CSV as linhas que estao na tela (com o filtro e a ordem atuais). */
    private void exportar(JTable tabela, String nomeDoArquivo) {
        JFileChooser escolha = new JFileChooser();
        escolha.setSelectedFile(new File(nomeDoArquivo));
        if (escolha.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ExportadorRelatorio.salvar(tabela, escolha.getSelectedFile().toPath());
                Mensagens.info(this, "Arquivo salvo: " + escolha.getSelectedFile().getName());
            } catch (IOException erro) {
                Mensagens.aviso(this, "Não foi possível salvar o arquivo.");
            }
        }
    }

    /** Um titulo e uma linha de cartoes (GridLayout com 1 linha e "colunas" colunas). */
    private JPanel grupo(String titulo, int colunas, JPanel... cartoes) {
        JPanel linha = new JPanel(new GridLayout(1, colunas, 16, 0));
        linha.setOpaque(false);
        for (JPanel cartao : cartoes) {
            linha.add(cartao);
        }
        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 15f));
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setOpaque(false);
        painel.add(rotulo, BorderLayout.NORTH);
        painel.add(linha, BorderLayout.CENTER);
        return painel;
    }

    private String contar(List<Equipamento> equipamentos, String status) {
        return String.valueOf(equipamentos.stream().filter(e -> status.equals(e.getStatus())).count());
    }

    private String contarEmprestimos(List<EmprestimoDetalhe> emprestimos, String status) {
        return String.valueOf(emprestimos.stream().filter(d -> status.equals(d.emprestimo().getStatus())).count());
    }
}
