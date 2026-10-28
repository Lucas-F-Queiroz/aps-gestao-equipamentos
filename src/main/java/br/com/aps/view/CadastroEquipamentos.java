package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.model.Equipamento;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/** Cadastro separado da disponibilidade: status e atualizado por operacao atomica no DAO. */
public class CadastroEquipamentos extends JPanel {
    private final JTextField patrimonio = new JTextField();
    private final JTextField nome = new JTextField();
    private final JTextField categoria = new JTextField();
    private final JComboBox<String> estado = new JComboBox<>(new String[]{"NOVO", "BOM", "REGULAR", "DANIFICADO"});
    private final JTable tabela = Estilo.criarTabela(
        new String[]{"Patrimônio", "Nome", "Categoria", "Estado", "Status"});
    private final List<Equipamento> linhas = new ArrayList<>();

    public CadastroEquipamentos() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        JPanel cadastro = Estilo.cartao();
        cadastro.add(Estilo.subtitulo("Cadastro de equipamentos"), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(2, 2, 12, 8));
        campos.setOpaque(false);
        campos.add(Estilo.campo("Patrimônio", patrimonio));
        campos.add(Estilo.campo("Nome", nome));
        campos.add(Estilo.campo("Categoria", categoria));
        campos.add(Estilo.campo("Estado de conservação", estado));
        cadastro.add(campos, BorderLayout.CENTER);
        JButton criar = Estilo.botaoPrincipal("Cadastrar");
        JButton salvar = Estilo.botao("Salvar alterações");
        JButton disponivel = Estilo.botao("Disponível");
        JButton indisponivel = Estilo.botao("Indisponível");
        JButton excluir = Estilo.botao("Excluir");
        JButton limpar = Estilo.botao("Limpar");
        cadastro.add(Estilo.linhaBotoes(criar, salvar, disponivel, indisponivel, excluir, limpar),
            BorderLayout.SOUTH);
        add(cadastro, BorderLayout.NORTH);
        JPanel listagem = Estilo.cartao();
        listagem.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(listagem, BorderLayout.CENTER);

        criar.addActionListener(e -> executar(() -> {
            Aplicacao.equipamentos().cadastrar(
                patrimonio.getText(), nome.getText(), categoria.getText(), (String) estado.getSelectedItem());
            limpar();
        }));
        salvar.addActionListener(e -> executar(() -> {
            Equipamento atual = exigirSelecao();
            Aplicacao.equipamentos().atualizar(atual.getId(),
                patrimonio.getText(), nome.getText(), categoria.getText(), (String) estado.getSelectedItem());
        }));
        disponivel.addActionListener(e -> mudarStatus(Equipamento.STATUS_DISPONIVEL));
        indisponivel.addActionListener(e -> mudarStatus(Equipamento.STATUS_INDISPONIVEL));
        excluir.addActionListener(e -> {
            Equipamento atual = selecionado();
            if (atual == null) { Mensagens.aviso(this, "Selecione um equipamento."); return; }
            if (Mensagens.confirmar(this, "Excluir " + atual.getNome() + "?")) {
                executar(() -> { Aplicacao.equipamentos().excluir(atual.getId()); limpar(); });
            }
        });
        limpar.addActionListener(e -> limpar());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });
        carregar();
    }

    private void mudarStatus(String status) {
        executar(() -> Aplicacao.equipamentos().alterarStatus(exigirSelecao().getId(), status));
    }

    private Equipamento exigirSelecao() {
        Equipamento atual = selecionado();
        if (atual == null) throw new IllegalArgumentException("Selecione um equipamento.");
        return atual;
    }

    private Equipamento selecionado() {
        int i = tabela.getSelectedRow();
        return i >= 0 && i < linhas.size() ? linhas.get(i) : null;
    }

    private void preencher() {
        Equipamento e = selecionado();
        if (e == null) return;
        patrimonio.setText(e.getPatrimonio());
        nome.setText(e.getNome());
        categoria.setText(e.getCategoria());
        estado.setSelectedItem(e.getEstado());
    }

    private void limpar() {
        tabela.clearSelection();
        patrimonio.setText("");
        nome.setText("");
        categoria.setText("");
        estado.setSelectedIndex(0);
    }

    private void carregar() {
        linhas.clear();
        linhas.addAll(Aplicacao.equipamentos().listar());
        Estilo.modelo(tabela).setRowCount(0);
        for (Equipamento e : linhas) {
            Estilo.modelo(tabela).addRow(new Object[]{
                e.getPatrimonio(), e.getNome(), e.getCategoria(), e.getEstado(), e.getStatus()});
        }
    }

    private void executar(Runnable acao) {
        try { acao.run(); carregar(); Mensagens.info(this, "Operação concluída."); }
        catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }
}
