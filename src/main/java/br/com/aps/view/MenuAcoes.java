package br.com.aps.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 * Coluna "Acoes" das tabelas: cada linha mostra tres pontinhos (icone) e, ao clicar, abre um menu com as acoes da linha.
 *
 * Como usar na tela:
 *   JPopupMenu menu = MenuAcoes.instalar(tabela, 6);          // 6 = posicao da coluna "Acoes"
 *   menu.add(MenuAcoes.item("Editar", "editar", Estilo.BARRA_LATERAL, () -> editar()));
 * A linha clicada vira a linha selecionada, entao a acao so precisa usar a selecao da tabela.
 */
final class MenuAcoes {
    private MenuAcoes() {
    }

    /** Prepara a coluna (aparencia e clique) e devolve o menu vazio para a tela adicionar os itens. */
    static JPopupMenu instalar(JTable tabela, int coluna) {
        JPopupMenu menu = new JPopupMenu();

        // Aparencia: tres pontinhos DESENHADOS (icone), centralizados. Nao usamos o caractere "⋮"
        // porque nem toda fonte tem esse simbolo (no Windows aparecia um quadradinho).
        tabela.getColumnModel().getColumn(coluna).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean selecionado,
                    boolean foco, int linha, int col) {
                super.getTableCellRendererComponent(t, "", selecionado, foco, linha, col);
                setIcon(new IconeAcao("pontos", selecionado ? Estilo.BARRA_LATERAL : Estilo.TEXTO_SUAVE));
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!selecionado) {
                    setBackground(linha % 2 == 0 ? Color.WHITE : new Color(247, 250, 253));
                }
                return this;
            }
        });
        tabela.getColumnModel().getColumn(coluna).setMaxWidth(70);
        ((TableRowSorter<?>) tabela.getRowSorter()).setSortable(coluna, false); // nao ordena por esta coluna

        // Clique na coluna: seleciona a linha e abre o menu logo abaixo dela, alinhado a direita da celula
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                int linha = tabela.rowAtPoint(evento.getPoint());
                if (linha >= 0 && tabela.columnAtPoint(evento.getPoint()) == coluna) {
                    tabela.setRowSelectionInterval(linha, linha);
                    Rectangle celula = tabela.getCellRect(linha, coluna, false);
                    menu.show(tabela, celula.x + celula.width - menu.getPreferredSize().width,
                        celula.y + celula.height);
                }
            }
        });
        // Maozinha ao passar o mouse sobre a coluna
        tabela.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent evento) {
                boolean sobreAcoes = tabela.rowAtPoint(evento.getPoint()) >= 0
                    && tabela.columnAtPoint(evento.getPoint()) == coluna;
                tabela.setCursor(Cursor.getPredefinedCursor(sobreAcoes ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        return menu;
    }

    /** Um item do menu: texto, icone (veja IconeAcao), cor e o que fazer ao clicar. */
    static JMenuItem item(String texto, String icone, Color cor, Runnable acao) {
        JMenuItem item = new JMenuItem(texto, new IconeAcao(icone, cor));
        item.setForeground(cor);
        item.addActionListener(evento -> Mensagens.executar(item, acao));
        return item;
    }
}
