package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.EmprestimoDetalhe;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/** Consulta de emprestimos, inclusive os devolvidos. Sem comandos SQL na interface. */
public class TelaConsultar extends JPanel {
    private final JTextField busca = new JTextField();
    private final JComboBox<String> filtro = new JComboBox<>(
        new String[]{"Todos", "Em uso", "Atrasado", "Devolvido"});
    private final JTable tabela = Estilo.criarTabela(new String[]{
        "Equipamento", "Pessoa", "Retirada", "Devolução prevista", "Situação"});
    private final JLabel resultado = new JLabel(" ");

    public TelaConsultar() {
        super(new BorderLayout(0, 18));
        setOpaque(false);

        busca.putClientProperty("JTextField.placeholderText", "Digite para filtrar a tabela");
        busca.putClientProperty("JTextField.leadingIcon", new IconeAcao("busca", Estilo.TEXTO_SUAVE));

        JPanel topo = Estilo.cartao();
        JPanel linha = new JPanel(new BorderLayout(16, 0));
        linha.setOpaque(false);

        JPanel campos = new JPanel(new GridLayout(1, 2, 16, 0));
        campos.setOpaque(false);
        campos.add(Estilo.campo("Buscar", busca));
        campos.add(Estilo.campo("Situação", filtro));

        JButton pesquisar = Estilo.botaoPrincipal("Pesquisar");
        linha.add(campos, BorderLayout.CENTER);
        linha.add(Estilo.campo(" ", pesquisar), BorderLayout.EAST);
        topo.add(linha);
        add(topo, BorderLayout.NORTH);

        JPanel lista = Estilo.cartao();
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        lista.add(resultado, BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);

        Estilo.larguras(tabela, 340, 220, 160, 190, 140);
        Estilo.colorirSituacao(tabela, 4);

        pesquisar.addActionListener(evento -> pesquisar());
        busca.addActionListener(evento -> pesquisar());
        filtro.addActionListener(evento -> pesquisar());
        pesquisar();
    }

    /** Filtra os objetos devolvidos pelo Controller; nao acessa JDBC. */
    private void pesquisar() {
        try {
            Estilo.modelo(tabela).setRowCount(0);
            String termo = busca.getText().trim().toLowerCase(Locale.ROOT);
            String statusEscolhido = (String) filtro.getSelectedItem();

            for (EmprestimoDetalhe detalhe : Aplicacao.emprestimos().listarDetalhado()) {
                String situacao = Regras.situacao(detalhe.emprestimo());
                if ("Aberto".equals(situacao)) {
                    situacao = "Em uso";
                }
                if (!"Todos".equals(statusEscolhido) && !situacao.equals(statusEscolhido)) {
                    continue;
                }

                String[] linha = {
                    detalhe.equipamentoNome() + " (" + detalhe.equipamentoPatrimonio() + ")",
                    detalhe.usuarioNome(),
                    Estilo.dia(detalhe.emprestimo().getDataRetirada()),
                    Estilo.dia(detalhe.emprestimo().getDataPrevista()),
                    situacao
                };
                if (String.join(" ", linha).toLowerCase(Locale.ROOT).contains(termo)) {
                    Estilo.modelo(tabela).addRow(linha);
                }
            }
            resultado.setText(tabela.getRowCount() + " resultado(s) encontrado(s)");
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }
}
