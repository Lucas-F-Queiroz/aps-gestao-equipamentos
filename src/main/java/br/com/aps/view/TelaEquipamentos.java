package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;
import br.com.aps.controller.Regras;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;

/** Inventario e consulta. Cadastros e emprestimos sao alterados em telas proprias. */
public class TelaEquipamentos extends JPanel {
    private final JLabel totais = new JLabel();
    private final JTextField busca = new JTextField();
    private final JComboBox<String> ordenacao = new JComboBox<>(new String[]{
        "Nome e patrimônio", "Patrimônio e ID", "Categoria e nome"
    });
    private final JTable tabela = Estilo.criarTabela(new String[]{
        "Patrimônio", "Equipamento", "Categoria", "Estado", "Status", "Com quem", "Prevista"});

    public TelaEquipamentos() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        JPanel cabecalho = Estilo.cartao();
        cabecalho.add(Estilo.subtitulo("Inventário de equipamentos"), BorderLayout.NORTH);
        cabecalho.add(totais, BorderLayout.CENTER);
        JButton atualizar = Estilo.botao("Atualizar");
        cabecalho.add(atualizar, BorderLayout.EAST);
        add(cabecalho, BorderLayout.NORTH);
        JPanel painel = Estilo.cartao();
        busca.putClientProperty("JTextField.placeholderText", "Buscar patrimônio, nome ou categoria");
        JPanel filtros = new JPanel(new GridLayout(1, 2, 12, 0));
        filtros.setOpaque(false);
        filtros.add(Estilo.campo("Buscar", busca));
        filtros.add(Estilo.campo("Ordenar por", ordenacao));
        painel.add(filtros, BorderLayout.NORTH);
        painel.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(painel, BorderLayout.CENTER);
        atualizar.addActionListener(e -> carregar());
        busca.addActionListener(e -> carregar());
        ordenacao.addActionListener(e -> carregar());
        carregar();
    }

    private void carregar() {
        try {
            // Cada criterio utiliza ORDER BY explicito no DAO (inclusive desempate).
            var equipamentos = switch (ordenacao.getSelectedIndex()) {
                case 1 -> Aplicacao.equipamentos().listarPorPatrimonio();
                case 2 -> Aplicacao.equipamentos().listarPorCategoria();
                default -> Aplicacao.equipamentos().listar();
            };
            Map<Integer, EmprestimoDetalhe> abertos = new HashMap<>();
            for (EmprestimoDetalhe d : Aplicacao.emprestimos().abertos()) {
                abertos.put(d.emprestimo().getEquipamentoId(), d);
            }
            long disponiveis = equipamentos.stream()
                .filter(e -> Equipamento.STATUS_DISPONIVEL.equals(e.getStatus())).count();
            long emprestados = equipamentos.stream()
                .filter(e -> Equipamento.STATUS_EMPRESTADO.equals(e.getStatus())).count();
            totais.setText("Total: " + equipamentos.size() + "  |  Disponíveis: " + disponiveis
                + "  |  Emprestados: " + emprestados);
            Estilo.modelo(tabela).setRowCount(0);
            String termo = busca.getText().trim().toLowerCase(Locale.ROOT);
            for (Equipamento e : equipamentos) {
                String pesquisa = (e.getPatrimonio() + " " + e.getNome() + " " + e.getCategoria())
                    .toLowerCase(Locale.ROOT);
                if (!pesquisa.contains(termo)) continue;
                EmprestimoDetalhe d = abertos.get(e.getId());
                Estilo.modelo(tabela).addRow(new Object[]{
                    e.getPatrimonio(), e.getNome(), e.getCategoria(), Regras.rotulo(e.getEstado()),
                    Regras.rotulo(e.getStatus()), d == null ? "-" : d.usuarioNome(),
                    d == null ? "-" : Estilo.dia(d.emprestimo().getDataPrevista())
                });
            }
        } catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }
}
