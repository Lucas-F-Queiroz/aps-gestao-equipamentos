package br.com.aps.view;
import java.awt.*;
import java.util.function.IntConsumer;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/** Botão real por linha, respeitando ordenação e filtro da JTable. */
public final class AcaoTabela extends AbstractCellEditor implements javax.swing.table.TableCellEditor, TableCellRenderer {
    private final JButton editor=Estilo.botao("");
    private final JButton desenho=Estilo.botao("");
    private int linha;
    public AcaoTabela(JTable tabela,int coluna,String icone,String descricao,IntConsumer acao){
        for(JButton b:new JButton[]{editor,desenho}){
            b.setIcon(new IconeAcao(icone,new Color(38,83,137)));b.setToolTipText(descricao);
            b.getAccessibleContext().setAccessibleName(descricao);
            b.putClientProperty("FlatLaf.style","arc: 10; margin: 2,8,2,8; background: #F8FAFC; hoverBackground: #E8F0FE; borderColor: #CBD5E1");
        }
        editor.addActionListener(e->{int atual=linha;fireEditingStopped();acao.accept(atual);});
        tabela.getColumnModel().getColumn(coluna).setCellRenderer(this);
        tabela.getColumnModel().getColumn(coluna).setCellEditor(this);
    }
    public Object getCellEditorValue(){return "";}
    public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){linha=t.convertRowIndexToModel(r);return editor;}
    public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
        return desenho;
    }
}
