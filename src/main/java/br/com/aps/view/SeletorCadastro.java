package br.com.aps.view;
import java.awt.*;
import java.util.function.IntConsumer;
import javax.swing.*;
public final class SeletorCadastro extends JPanel {
    private int selecionado;
    private final JButton[] botoes=new JButton[2];
    public SeletorCadastro(IntConsumer acao) {
        setOpaque(false);setLayout(new GridLayout(1,2,0,0));setPreferredSize(new Dimension(330,40));
        String[] nomes={"Equipamentos","Pessoas"};
        for(int i=0;i<2;i++) { final int indice=i;
            JButton b=new JButton(nomes[i]) {
                @Override protected void paintComponent(Graphics graphics) {
                    var g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    var forma=new java.awt.geom.Path2D.Float();forma.moveTo(0,getHeight());forma.lineTo(6,10);forma.quadTo(8,1,18,1);forma.lineTo(getWidth()-18,1);forma.quadTo(getWidth()-8,1,getWidth()-6,10);forma.lineTo(getWidth(),getHeight());forma.closePath();
                    g.setColor(indice==selecionado?Estilo.AZUL:new Color(241,245,249));g.fill(forma);g.setColor(Estilo.BORDA);g.draw(forma);g.dispose();super.paintComponent(graphics);
                }
            };
            b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(true);
            b.addActionListener(e->{selecionado=indice;cores();acao.accept(indice==0?1:0);});botoes[i]=b;add(b);
        }cores();
    }
    private void cores(){for(int i=0;i<2;i++){Color c=i==selecionado?Color.WHITE:Estilo.BARRA_LATERAL;botoes[i].setForeground(c);botoes[i].setSelected(i==selecionado);botoes[i].setIcon(new IconeAcao(i==0?"equipamento":"pessoa",c));}repaint();}
}
