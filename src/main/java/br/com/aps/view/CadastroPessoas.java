package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.model.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/** Formulario Swing: a validacao e o acesso ao banco ficam no Controller e DAO. */
public class CadastroPessoas extends JPanel {
    private final JTextField nome = new JTextField();
    private final JTextField email = new JTextField();
    private final JComboBox<String> tipo = new JComboBox<>(new String[] {"ALUNO", "PROFESSOR", "FUNCIONARIO"});
    private final JCheckBox ativo = new JCheckBox("Ativo (edição)", true);
    private final JTable tabela = Estilo.criarTabela(new String[]{"ID", "Nome", "E-mail", "Tipo", "Situação"});
    private final List<Usuario> linhas = new ArrayList<>();

    public CadastroPessoas() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        JPanel cadastro = Estilo.cartao();
        cadastro.add(Estilo.subtitulo("Cadastro de pessoas"), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(2, 2, 12, 8));
        campos.setOpaque(false);
        campos.add(Estilo.campo("Nome", nome));
        campos.add(Estilo.campo("E-mail", email));
        campos.add(Estilo.campo("Tipo", tipo));
        campos.add(Estilo.campo("Situação", ativo));
        cadastro.add(campos, BorderLayout.CENTER);

        JButton criar = Estilo.botaoPrincipal("Cadastrar");
        JButton salvar = Estilo.botao("Salvar alterações");
        JButton excluir = Estilo.botao("Excluir");
        JButton limpar = Estilo.botao("Limpar");
        cadastro.add(Estilo.linhaBotoes(criar, salvar, excluir, limpar), BorderLayout.SOUTH);
        // Novos cadastros nascem ativos. O campo so e editavel apos escolher uma pessoa.
        ativo.setEnabled(false);
        add(cadastro, BorderLayout.NORTH);
        JPanel listagem = Estilo.cartao();
        listagem.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(listagem, BorderLayout.CENTER);

        criar.addActionListener(e -> executar(() -> {
            Aplicacao.usuarios().cadastrar(nome.getText(), email.getText(), (String) tipo.getSelectedItem());
            limpar();
        }));
        salvar.addActionListener(e -> executar(() -> {
            Usuario u = selecionado();
            if (u == null) throw new IllegalArgumentException("Selecione uma pessoa.");
            Aplicacao.usuarios().atualizar(u.getId(), nome.getText(), email.getText(),
                (String) tipo.getSelectedItem(), ativo.isSelected());
        }));
        excluir.addActionListener(e -> {
            Usuario u = selecionado();
            if (u == null) { Mensagens.aviso(this, "Selecione uma pessoa."); return; }
            if (Mensagens.confirmar(this, "Excluir " + u.getNome() + "?")) {
                executar(() -> { Aplicacao.usuarios().excluir(u.getId()); limpar(); });
            }
        });
        limpar.addActionListener(e -> limpar());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });
        carregar();
    }

    private Usuario selecionado() {
        int i = tabela.getSelectedRow();
        return i >= 0 && i < linhas.size() ? linhas.get(i) : null;
    }

    private void preencher() {
        Usuario u = selecionado();
        if (u == null) return;
        nome.setText(u.getNome());
        email.setText(u.getEmail());
        tipo.setSelectedItem(u.getTipo());
        ativo.setSelected(u.isAtivo());
        ativo.setEnabled(true);
    }

    private void limpar() {
        tabela.clearSelection();
        nome.setText("");
        email.setText("");
        tipo.setSelectedIndex(0);
        ativo.setSelected(true);
        ativo.setEnabled(false);
    }

    private void carregar() {
        linhas.clear();
        linhas.addAll(Aplicacao.usuarios().listar());
        Estilo.modelo(tabela).setRowCount(0);
        for (Usuario u : linhas) {
            Estilo.modelo(tabela).addRow(new Object[]{
                u.getId(), u.getNome(), u.getEmail(), u.getTipo(), u.isAtivo() ? "Ativo" : "Inativo"});
        }
    }

    private void executar(Runnable acao) {
        try { acao.run(); carregar(); Mensagens.info(this, "Operação concluída."); }
        catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }
}
