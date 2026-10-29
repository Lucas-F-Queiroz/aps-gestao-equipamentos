package br.com.aps.view;

import java.awt.*;
import javax.swing.Icon;

/** Ícones vetoriais para manter nitidez nas escalas do Windows. */
public final class IconeAcao implements Icon {
    private final String tipo;
    private final Color cor;
    public IconeAcao(String tipo, Color cor) { this.tipo = tipo; this.cor = cor; }
    public int getIconWidth() { return 20; }
    public int getIconHeight() { return 20; }
    public void paintIcon(Component c, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create(); g.translate(x, y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c.isEnabled() ? cor : new Color(160, 174, 192));
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (tipo) {
            case "download": g.drawLine(10,2,10,13); g.drawLine(5,8,10,13); g.drawLine(15,8,10,13); g.drawPolyline(new int[]{3,3,17,17},new int[]{13,17,17,13},4); break;
            case "voltar": g.drawLine(3,10,18,10); g.drawLine(3,10,9,4); g.drawLine(3,10,9,16); break;
            case "calendario": g.drawRoundRect(2,4,16,14,3,3);g.drawLine(2,8,18,8);g.drawLine(6,2,6,6);g.drawLine(14,2,14,6);for(int i=5;i<17;i+=5)for(int j=11;j<17;j+=4)g.fillRect(i,j,2,2);break;
            case "busca": g.drawOval(2,2,11,11);g.drawLine(12,12,18,18);break;
            case "pessoa": g.drawOval(7,2,6,6);g.drawRoundRect(3,11,14,7,5,5);break;
            case "equipamento": g.drawRoundRect(4,3,12,11,2,2);g.drawPolyline(new int[]{4,1,19,16},new int[]{14,17,17,14},4);break;
            case "relatorio": g.drawPolyline(new int[]{4,4,12,17,17,4},new int[]{18,2,2,7,18,18},6);g.drawLine(12,2,12,7);g.drawLine(12,7,17,7);g.drawRect(7,11,2,4);g.drawRect(11,9,2,6);break;
            case "mais": g.drawLine(3,10,17,10);g.drawLine(10,3,10,17);break;
            case "editar": g.drawPolygon(new int[]{3,5,17,15},new int[]{17,11,2,4},4);g.drawLine(3,18,18,18);break;
            case "excluir": g.drawRect(5,6,10,12);g.drawLine(3,5,17,5);g.drawLine(7,2,13,2);g.drawLine(8,9,8,15);g.drawLine(12,9,12,15);break;
            case "olho": g.drawOval(2,5,16,10);g.fillOval(8,8,4,4);break;
            case "relogio": g.drawOval(2,2,16,16);g.drawLine(10,5,10,10);g.drawLine(10,10,13,12);break;
            case "trocar": g.drawLine(3,7,16,7);g.drawLine(12,3,16,7);g.drawLine(12,11,16,7);g.drawLine(17,13,4,13);g.drawLine(8,9,4,13);g.drawLine(8,17,4,13);break;
            case "pontos": g.fillOval(8,3,4,4);g.fillOval(8,8,4,4);g.fillOval(8,13,4,4);break;
            case "seta": g.drawLine(7,3,14,10);g.drawLine(14,10,7,17);break;
            default: g.drawLine(14,2,7,12);g.drawPolygon(new int[]{7,13,10,2},new int[]{10,14,18,15},4);break;
        }
        g.dispose();
    }
}
