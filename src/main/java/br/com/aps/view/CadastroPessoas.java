package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.RegraNegocioException;
import br.com.aps.controller.Regras;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Usuario;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Cadastro de pessoas. Em cima, o formulario; embaixo, a lista com busca, filtros e o menu "⋮"
 * de cada linha (Visualizar, Editar, Alterar situacao, Excluir). "Funcao" e o tipo da pessoa
 * (Aluno, Professor ou Funcionario). A validacao e o acesso ao banco ficam no Controller e no DAO.
 */
public class CadastroPessoas extends JPanel {
    private static final String[] SITUACOES = {"Ativo", "Inativo"};

    private final JTextField nome = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField cpf = new JTextField();
    private final JTextField celular = new JTextField();
    private final JComboBox<String> funcao = new JComboBox<>(Regras.rotulos(Regras.TIPOS_USUARIO));
    private final JCheckBox ativo = new JCheckBox("Pessoa ativa", true);
    private final JTable tabela = Estilo.criarTabela(
        new String[]{"ID", "Nome", "E-mail", "CPF", "Celular", "Função", "Situação", "Ações"});
    private final List<Usuario> linhas = new ArrayList<>();
    private final FiltroTabela filtro = new FiltroTabela(tabela);

    public CadastroPessoas() {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        nome.putClientProperty("JTextField.placeholderText", "Nome completo");
        email.putClientProperty("JTextField.placeholderText", "nome@empresa.com");
        cpf.putClientProperty("JTextField.placeholderText", "000.000.000-00 (opcional)");
        celular.putClientProperty("JTextField.placeholderText", "(11) 91234-5678 (opcional)");

        // ---------- formulario
        JPanel cadastro = Estilo.cartao();
        cadastro.add(Estilo.subtitulo("Cadastro de pessoas"), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(2, 3, 12, 10));
        campos.setOpaque(false);
        campos.add(Estilo.campo("Nome", nome));
        campos.add(Estilo.campo("E-mail", email));
        campos.add(Estilo.campo("CPF (opcional)", cpf));
        campos.add(Estilo.campo("Celular com DDD (opcional)", celular));
        campos.add(Estilo.campo("Função", funcao));
        campos.add(Estilo.campo("Situação (edição)", ativo));
        cadastro.add(campos, BorderLayout.CENTER);

        JButton criar = Estilo.botaoPrincipal("Cadastrar");
        JButton salvar = Estilo.botao("Salvar alterações");
        JButton excluirForm = Estilo.botao("Excluir");
        JButton limpar = Estilo.botao("Limpar");
        cadastro.add(Estilo.linhaBotoes(criar, salvar, excluirForm, limpar), BorderLayout.SOUTH);
        ativo.setEnabled(false); // pessoa nova nasce ativa; o campo so vale ao editar uma pessoa da lista
        add(cadastro, BorderLayout.NORTH);

        // ---------- lista: busca, filtros, botao "Alterar situacao" e menu "⋮"
        JButton alterarSituacao = Estilo.botaoPrincipal("Alterar situação");
        filtro.adicionarFiltro("Função", 5).adicionarFiltro("Situação", 6).adicionarBotao(alterarSituacao);
        Estilo.larguras(tabela, 50, 180, 230, 130, 130, 130, 120, 70);
        Estilo.colorirSituacao(tabela, 6);
        JPopupMenu menu = MenuAcoes.instalar(tabela, 7);
        menu.add(MenuAcoes.item("Visualizar", "olho", Estilo.BARRA_LATERAL, this::visualizar));
        menu.add(MenuAcoes.item("Editar", "editar", Estilo.BARRA_LATERAL, this::editar));
        menu.add(MenuAcoes.item("Alterar situação", "pessoa", Estilo.BARRA_LATERAL, this::alterarSituacao));
        menu.add(MenuAcoes.item("Excluir", "excluir", Estilo.VERMELHO, this::excluir));
        JPanel listagem = Estilo.cartao();
        listagem.add(filtro, BorderLayout.NORTH);
        listagem.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(listagem, BorderLayout.CENTER);

        // ---------- botoes
        criar.addActionListener(e -> executar(() -> {
            cadastrar(nome.getText(), email.getText(),
                Regras.codigo((String) funcao.getSelectedItem()), cpf.getText(), celular.getText());
            limpar();
        }));
        salvar.addActionListener(e -> executar(() -> {
            Usuario u = exigirSelecao();
            atualizar(u.getId(), nome.getText(), email.getText(),
                Regras.codigo((String) funcao.getSelectedItem()), ativo.isSelected(), cpf.getText(), celular.getText());
        }));
        excluirForm.addActionListener(e -> Mensagens.executar(this, this::excluir));
        limpar.addActionListener(e -> limpar());
        alterarSituacao.addActionListener(e -> Mensagens.executar(this, this::alterarSituacao));
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });
        carregar();
    }

    // ------------------------------------------------------------ janelas (modais) do menu "⋮"

    /** Visualizar: somente leitura, com o resumo dos emprestimos da pessoa. */
    private void visualizar() {
        Usuario u = exigirSelecao();
        List<EmprestimoDetalhe> dela = TarefaBanco.consultar(this, Aplicacao.emprestimos()::listarDetalhado).stream()
            .filter(d -> d.emprestimo().getUsuarioId() == u.getId()).collect(Collectors.toList());
        String emUso = dela.stream()
            .filter(d -> Emprestimo.STATUS_ABERTO.equals(d.emprestimo().getStatus()))
            .map(d -> d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")")
            .collect(Collectors.joining(", "));
        new Modal(this, "Visualizar pessoa")
            .info("Nome", u.getNome())
            .info("E-mail", u.getEmail())
            .info("CPF", u.getCpf() == null ? "Não informado" : u.getCpf())
            .info("Celular", u.getCelular() == null ? "Não informado" : u.getCelular())
            .info("Função", Regras.rotulo(u.getTipo()))
            .selo("Situação", u.isAtivo() ? "Ativo" : "Inativo")
            .info("Empréstimos registrados", String.valueOf(dela.size()))
            .info("Equipamento em uso", emUso.isEmpty() ? "nenhum" : emUso)
            .exibirSomenteLeitura();
    }

    /** Editar: muda nome, e-mail, funcao e situacao da pessoa escolhida. */
    private void editar() {
        Usuario u = exigirSelecao();
        JTextField campoNome = new JTextField(u.getNome());
        JTextField campoEmail = new JTextField(u.getEmail());
        JTextField campoCpf = new JTextField(u.getCpf());
        JTextField campoCelular = new JTextField(u.getCelular());
        JComboBox<String> campoFuncao = new JComboBox<>(Regras.rotulos(Regras.TIPOS_USUARIO));
        campoFuncao.setSelectedItem(Regras.rotulo(u.getTipo()));
        JComboBox<String> campoSituacao = new JComboBox<>(SITUACOES);
        campoSituacao.setSelectedItem(u.isAtivo() ? "Ativo" : "Inativo");

        Modal modal = new Modal(this, "Editar pessoa")
            .campo("Nome", campoNome).campo("E-mail", campoEmail)
            .campo("CPF (opcional)", campoCpf).campo("Celular com DDD (opcional)", campoCelular)
            .campo("Função", campoFuncao).campo("Situação", campoSituacao);
        if (modal.exibir(() -> atualizar(u.getId(), campoNome.getText(),
                campoEmail.getText(), Regras.codigo((String) campoFuncao.getSelectedItem()),
                "Ativo".equals(campoSituacao.getSelectedItem()), campoCpf.getText(), campoCelular.getText()))) {
            carregar();
        }
    }

    /** Alterar situacao: so ativa ou inativa a pessoa (os outros dados ficam como estao). */
    private void alterarSituacao() {
        Usuario u = exigirSelecao();
        JComboBox<String> novaSituacao = new JComboBox<>(SITUACOES);
        novaSituacao.setSelectedItem(u.isAtivo() ? "Inativo" : "Ativo"); // sugere o contrario do atual

        Modal modal = new Modal(this, "Alterar situação da pessoa")
            .info("Nome", u.getNome())
            .selo("Situação atual", u.isAtivo() ? "Ativo" : "Inativo")
            .campo("Nova situação", novaSituacao);
        if (modal.exibir(() -> atualizar(u.getId(), u.getNome(), u.getEmail(),
                u.getTipo(), "Ativo".equals(novaSituacao.getSelectedItem()), u.getCpf(), u.getCelular()))) {
            carregar();
        }
    }

    private void excluir() {
        Usuario u = exigirSelecao();
        if (Mensagens.confirmar(this, "Excluir " + u.getNome() + "?")) {
            TarefaBanco.executar(this, () -> Aplicacao.usuarios().excluir(u.getId()));
            limpar();
            carregar();
        }
    }

    // ------------------------------------------------------------ apoio

    private void cadastrar(String nome, String email, String tipo, String cpf, String celular) {
        TarefaBanco.executar(this, () -> Aplicacao.usuarios().cadastrar(nome, email, tipo, cpf, celular));
    }

    private void atualizar(int id, String nome, String email, String tipo, boolean ativo, String cpf, String celular) {
        TarefaBanco.executar(this, () -> Aplicacao.usuarios().atualizar(id, nome, email, tipo, ativo, cpf, celular));
    }

    private Usuario exigirSelecao() {
        Usuario u = selecionado();
        if (u == null) throw new RegraNegocioException("Selecione uma pessoa.");
        return u;
    }

    /** Pessoa da linha selecionada (linhaSelecionada ja considera ordenacao e filtro). */
    private Usuario selecionado() {
        int i = Estilo.linhaSelecionada(tabela);
        return i >= 0 && i < linhas.size() ? linhas.get(i) : null;
    }

    private void preencher() {
        Usuario u = selecionado();
        if (u == null) return;
        nome.setText(u.getNome());
        email.setText(u.getEmail());
        cpf.setText(u.getCpf());
        celular.setText(u.getCelular());
        funcao.setSelectedItem(Regras.rotulo(u.getTipo()));
        ativo.setSelected(u.isAtivo());
        ativo.setEnabled(true);
    }

    private void limpar() {
        tabela.clearSelection();
        nome.setText("");
        email.setText("");
        cpf.setText("");
        celular.setText("");
        funcao.setSelectedIndex(0);
        ativo.setSelected(true);
        ativo.setEnabled(false);
    }

    private void carregar() {
        Usuario anterior = selecionado(); // para manter a mesma linha escolhida depois de recarregar
        List<Usuario> atualizadas = TarefaBanco.consultar(this, Aplicacao.usuarios()::listar);
        linhas.clear();
        linhas.addAll(atualizadas);
        Estilo.modelo(tabela).setRowCount(0);
        for (Usuario u : linhas) {
            Estilo.modelo(tabela).addRow(new Object[]{u.getId(), u.getNome(), u.getEmail(),
                u.getCpf(), u.getCelular(), Regras.rotulo(u.getTipo()), u.isAtivo() ? "Ativo" : "Inativo", ""});
        }
        filtro.atualizar();
        if (anterior != null) {
            for (int i = 0; i < linhas.size(); i++) {
                int visual = tabela.convertRowIndexToView(i);
                if (linhas.get(i).getId() == anterior.getId() && visual >= 0) {
                    tabela.setRowSelectionInterval(visual, visual);
                }
            }
        }
    }

    private void executar(Runnable acao) {
        try { acao.run(); carregar(); Mensagens.info(this, "Operação concluída."); }
        catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }
}
