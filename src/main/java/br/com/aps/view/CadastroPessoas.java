package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Usuario;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Cadastro de pessoas (tabela "usuario" do Lucas): nome, e-mail, área/profissão, matrícula e se está ativa.
 * As regras e os limites (nome ate 100, e-mail unico ate 120, área/profissão livre até 100 caracteres e matrícula opcional única,
 * historico impede exclusao) estao em UsuarioController e Regras.
 */
public class CadastroPessoas extends JPanel {
    private final JTextField campoNome = new JTextField();
    private final JTextField campoEmail = new JTextField();
    private final JTextField campoTipo = new JTextField();
    private final JTextField campoMatricula = new JTextField();
    private final JComboBox<String> campoStatus = new JComboBox<>(new String[] {"Ativo", "Inativo"});
    private final JTable tabela = Estilo.criarTabela(
        new String[] {"ID", "Nome", "E-mail", "Área/profissão", "Status", "Equipamento em uso", "Matrícula"});
    private final ArrayList<Usuario> usuarios = new ArrayList<>(); // mesma ordem das linhas da tabela

    public CadastroPessoas() {
        super(new BorderLayout(0, 12));
        setOpaque(false);

        campoNome.putClientProperty("JTextField.placeholderText", "Nome completo (até " + Regras.NOME_USUARIO_MAX + " letras)");
        campoEmail.putClientProperty("JTextField.placeholderText", "nome@empresa.com");

        JPanel cartaoForm = Estilo.cartao();
        cartaoForm.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 20, 12, 20));
        ((BorderLayout) cartaoForm.getLayout()).setVgap(8);
        cartaoForm.add(Estilo.subtitulo("Dados da pessoa"), BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(2, 3, 16, 8));
        campos.setOpaque(false);
        campos.add(Estilo.campo("Nome", campoNome));
        campos.add(Estilo.campo("E-mail (único)", campoEmail));
        campos.add(Estilo.campo("Área/profissão", campoTipo));
        campos.add(Estilo.campo("Status", campoStatus));
        campos.add(Estilo.campo("Matrícula (opcional, única)", campoMatricula));

        JButton cadastrar = Estilo.botaoPrincipal("Cadastrar");
        JButton modificar = Estilo.botao("Modificar");
        JButton excluir = Estilo.botao("Excluir");
        JButton limpar = Estilo.botao("Limpar");

        JLabel dica = new JLabel("Pessoa nova nasce Ativa. Quem tem histórico de empréstimos não pode ser excluída: use o status Inativo.");
        dica.setForeground(Estilo.TEXTO_SUAVE);
        dica.setFont(dica.getFont().deriveFont(12f));
        for (JButton acao : new JButton[] {cadastrar, modificar, excluir, limpar}) {
            acao.putClientProperty("FlatLaf.style", (String) acao.getClientProperty("FlatLaf.style") + "; margin: 5,16,5,16");
        }

        JPanel miolo = new JPanel(new BorderLayout(0, 8));
        miolo.setOpaque(false);
        miolo.add(campos, BorderLayout.NORTH);
        miolo.add(Estilo.linhaBotoes(cadastrar, modificar, excluir, limpar), BorderLayout.CENTER);
        miolo.add(dica, BorderLayout.SOUTH);
        cartaoForm.add(miolo, BorderLayout.CENTER);
        add(cartaoForm, BorderLayout.NORTH);

        JPanel cartaoTabela = Estilo.cartao();
        cartaoTabela.add(Estilo.subtitulo("Cadastro"), BorderLayout.NORTH);
        Estilo.colorirSituacao(tabela, 4);
        Estilo.larguras(tabela, 50, 180, 240, 130, 100, 330);
        cartaoTabela.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(cartaoTabela, BorderLayout.CENTER);

        cadastrar.addActionListener(e -> cadastrar());
        modificar.addActionListener(e -> modificar());
        excluir.addActionListener(e -> excluir());
        limpar.addActionListener(e -> limpar());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormulario();
            }
        });
        campoStatus.setEnabled(false); // pessoa nova sempre nasce Ativa

        atualizarTabela();
    }

    private void atualizarTabela() {
        try {
            Map<Integer, String> emUso = new HashMap<>(); // pessoa -> equipamento do emprestimo aberto
            for (EmprestimoDetalhe aberto : Aplicacao.emprestimos().abertos()) {
                emUso.put(aberto.emprestimo().getUsuarioId(),
                    aberto.equipamentoNome() + " (" + aberto.equipamentoPatrimonio() + ")");
            }
            usuarios.clear();
            usuarios.addAll(Aplicacao.usuarios().listar());
            Estilo.modelo(tabela).setRowCount(0);
            for (Usuario u : usuarios) {
                Estilo.modelo(tabela).addRow(new String[] {String.valueOf(u.getId()), u.getNome(), u.getEmail(),
                    u.getAreaProfissao(), u.isAtivo() ? "Ativo" : "Inativo", emUso.getOrDefault(u.getId(), "-"), u.getMatricula()});
            }
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private Usuario pessoaSelecionada() {
        int linha = tabela.getSelectedRow();
        return linha < 0 || linha >= usuarios.size() ? null : usuarios.get(linha);
    }

    private void preencherFormulario() {
        Usuario u = pessoaSelecionada();
        if (u == null) {
            return;
        }
        campoNome.setText(u.getNome());
        campoEmail.setText(u.getEmail());
        campoTipo.setText(u.getAreaProfissao());
        campoMatricula.setText(u.getMatricula());
        campoStatus.setSelectedItem(u.isAtivo() ? "Ativo" : "Inativo");
        campoStatus.setEnabled(true);
    }

    private void cadastrar() {
        try {
            Aplicacao.usuarios().cadastrar(campoNome.getText(), campoEmail.getText(),
                campoTipo.getText(), campoMatricula.getText());
            atualizarTabela();
            limpar();
            Mensagens.info(this, "Pessoa cadastrada.");
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void modificar() {
        Usuario u = pessoaSelecionada();
        if (u == null) {
            Mensagens.aviso(this, "Selecione uma pessoa na tabela.");
            return;
        }
        try {
            boolean ativo = campoStatus.getSelectedItem().equals("Ativo");
            Aplicacao.usuarios().atualizar(u.getId(), campoNome.getText(), campoEmail.getText(),
                campoTipo.getText(), ativo, campoMatricula.getText());
            atualizarTabela();

            String mensagem = "Pessoa atualizada.";
            if (!ativo && Aplicacao.usuarios().possuiEmprestimoAberto(u.getId())) {
                // A regra do Lucas so exige pessoa ativa para EMPRESTAR; devolver continua permitido.
                mensagem += "\nAtenção: ela ainda está com um equipamento emprestado."
                    + " Ela poderá devolvê-lo, mas não poderá fazer novos empréstimos.";
            }
            Mensagens.info(this, mensagem);
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void excluir() {
        Usuario u = pessoaSelecionada();
        if (u == null) {
            Mensagens.aviso(this, "Selecione uma pessoa na tabela.");
            return;
        }
        if (!Mensagens.confirmar(this, "Excluir \"" + u.getNome() + "\"?")) {
            return;
        }
        try {
            Aplicacao.usuarios().excluir(u.getId());
            atualizarTabela();
            limpar();
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void limpar() {
        campoNome.setText("");
        campoEmail.setText("");
        campoTipo.setText("");
        campoMatricula.setText("");
        campoStatus.setSelectedIndex(0);
        campoStatus.setEnabled(false);
        tabela.clearSelection();
    }
}
