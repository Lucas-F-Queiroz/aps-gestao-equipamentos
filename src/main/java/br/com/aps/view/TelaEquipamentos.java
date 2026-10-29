package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;

/**
 * Inventario e consulta. Cadastros e emprestimos sao alterados em telas proprias.
 * Busca em todos os campos, filtros por categoria/estado/status e "Ordenar por" com um
 * criterio de cada vez (tambem da para ordenar clicando no cabecalho das colunas).
 */
public class TelaEquipamentos extends JPanel {
    // Criterios de ordenacao e a posicao de cada um na tabela
    private static final String[] CRITERIOS = {"Nome", "ID", "Categoria", "Patrimônio"};
    private static final int[] COLUNA_DO_CRITERIO = {2, 0, 3, 1};

    private final JTable tabela = Estilo.criarTabela(new String[]{
        "ID", "Patrimônio", "Equipamento", "Categoria", "Estado", "Status", "Com quem", "Prevista"});
    private final JComboBox<String> ordenarPor = new JComboBox<>(CRITERIOS);
    private final JComboBox<String> ordem = new JComboBox<>(new String[]{"Crescente", "Decrescente"});
    private final FiltroTabela filtro = new FiltroTabela(tabela);

    public TelaEquipamentos() {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        JButton atualizar = Estilo.botao("Atualizar");
        filtro.adicionarFiltro("Categoria", 3);
        filtro.adicionarFiltro("Estado", 4);
        filtro.adicionarFiltro("Status", 5);
        filtro.adicionarCampo("Ordenar por", ordenarPor);
        filtro.adicionarCampo("Ordem", ordem);
        filtro.adicionarCampo(" ", atualizar);

        Estilo.larguras(tabela, 50, 120, 320, 150, 120, 130, 160, 130);
        Estilo.colorirSituacao(tabela, 4);
        Estilo.colorirSituacao(tabela, 5);

        JPanel painel = Estilo.cartao();
        painel.add(filtro, BorderLayout.NORTH);
        painel.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(painel, BorderLayout.CENTER);

        atualizar.addActionListener(e -> carregar());
        ordenarPor.addActionListener(e -> ordenar());
        ordem.addActionListener(e -> ordenar());
        carregar();
        ordenar();
    }

    /** Aplica o criterio escolhido nas listas "Ordenar por" e "Ordem". */
    private void ordenar() {
        int coluna = COLUNA_DO_CRITERIO[ordenarPor.getSelectedIndex()];
        SortOrder sentido = ordem.getSelectedIndex() == 0 ? SortOrder.ASCENDING : SortOrder.DESCENDING;
        tabela.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(coluna, sentido)));
    }

    private void carregar() {
        try {
            Map<Integer, EmprestimoDetalhe> abertos = new HashMap<>();
            for (EmprestimoDetalhe d : Aplicacao.emprestimos().abertos()) {
                abertos.put(d.emprestimo().getEquipamentoId(), d);
            }
            Estilo.modelo(tabela).setRowCount(0);
            for (Equipamento e : Aplicacao.equipamentos().listar()) {
                EmprestimoDetalhe d = abertos.get(e.getId());
                Estilo.modelo(tabela).addRow(new Object[]{
                    e.getId(), e.getPatrimonio(), e.getNome(), e.getCategoria(),
                    Regras.rotulo(e.getEstado()), Regras.rotulo(e.getStatus()),
                    d == null ? "-" : d.usuarioNome(),
                    d == null ? "-" : Estilo.dia(d.emprestimo().getDataPrevista())
                });
            }
            filtro.atualizar();
        } catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }
}
