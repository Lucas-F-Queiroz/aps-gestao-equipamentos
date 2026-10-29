package br.com.aps.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

/**
 * Icones desenhados em codigo (nao precisam de imagens). Servem para qualquer tamanho:
 * cada desenho foi criado numa area de 44 x 44 e e ampliado ou reduzido na hora de pintar.
 */
public class Icone implements Icon {
    public static final int COMPUTADOR = 0;
    public static final int USUARIOS = 1;
    public static final int PRANCHETA = 2;
    public static final int LUPA = 3;
    public static final int SAIR = 4;
    public static final int CASA = 5;
    public static final int MAIS = 6;
    public static final int DEVOLVER = 7;
    public static final int RELATORIO = 8;

    private final int tipo;
    private final Color cor;
    private final Color detalhe;
    private final int tamanho;
    private final boolean comFundo;

    /** cor = cor do desenho; detalhe = cor dos tracinhos por cima (ex.: linhas da prancheta). */
    public Icone(int tipo, Color cor, Color detalhe, int tamanho, boolean comFundo) {
        this.tipo = tipo;
        this.cor = cor;
        this.detalhe = detalhe;
        this.tamanho = tamanho;
        this.comFundo = comFundo;
    }

    @Override
    public int getIconWidth() {
        return tamanho;
    }

    @Override
    public int getIconHeight() {
        return tamanho;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D p = (Graphics2D) g.create();
        p.translate(x, y);
        p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (comFundo) {
            p.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 30));
            p.fillRoundRect(0, 0, tamanho, tamanho, 22, 22);
        }

        // Area onde o desenho aparece: com fundo ele ocupa so o miolo do quadrado.
        double area = comFundo ? tamanho * 0.62 : tamanho;
        double base = (tipo == SAIR) ? 22 : 44;   // tamanho em que o desenho foi criado
        double deslocamento = (tamanho - area) / 2.0;
        p.translate(deslocamento, deslocamento);
        p.scale(area / base, area / base);

        p.setColor(cor);
        p.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (tipo == COMPUTADOR) {
            p.drawRoundRect(7, 5, 30, 26, 3, 3);
            p.drawLine(22, 32, 22, 36);
            p.fillRoundRect(2, 36, 40, 5, 3, 3);
        } else if (tipo == USUARIOS) {
            p.fillOval(16, 3, 12, 12);
            p.fillOval(3, 8, 9, 9);
            p.fillOval(32, 8, 9, 9);
            p.fillRoundRect(0, 19, 15, 16, 10, 10);
            p.fillRoundRect(29, 19, 15, 16, 10, 10);
            p.fillRoundRect(13, 18, 18, 24, 12, 12);
        } else if (tipo == PRANCHETA) {
            p.fillRoundRect(7, 7, 30, 35, 4, 4);
            p.setColor(detalhe);
            p.drawRoundRect(16, 3, 12, 7, 3, 3);
            for (int linha = 18; linha <= 34; linha += 8) {
                p.drawLine(14, linha, 30, linha);
            }
        } else if (tipo == LUPA) {
            p.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            p.drawOval(5, 4, 26, 26);
            p.drawLine(28, 28, 40, 40);
        } else if (tipo == CASA) {
            p.drawLine(4, 22, 22, 6);   // telhado
            p.drawLine(22, 6, 40, 22);
            p.drawRoundRect(10, 20, 24, 20, 3, 3);   // paredes
            p.drawLine(22, 40, 22, 30);              // porta
        } else if (tipo == RELATORIO) {
            p.drawRoundRect(8, 3, 28, 38, 5, 5);   // folha
            p.fillRect(14, 26, 4, 9);              // tres barras de grafico
            p.fillRect(21, 19, 4, 16);
            p.fillRect(28, 12, 4, 23);
        } else if (tipo == DEVOLVER) {
            p.drawLine(7, 14, 17, 5);               // ponta da seta (aponta para a esquerda)
            p.drawLine(7, 14, 17, 23);
            p.drawLine(7, 14, 28, 14);              // linha de cima
            p.drawArc(18, 14, 20, 24, 90, -180);    // curva (volta)
            p.drawLine(28, 38, 12, 38);             // linha de baixo
        } else if (tipo == MAIS) {
            p.drawRoundRect(5, 5, 34, 34, 10, 10);   // quadrado
            p.drawLine(22, 13, 22, 31);              // sinal de +
            p.drawLine(13, 22, 31, 22);
        } else {
            // SAIR: porta com seta (desenhada numa area de 22 x 22)
            p.setStroke(new BasicStroke(2));
            p.drawLine(9, 3, 2, 3);
            p.drawLine(2, 3, 2, 19);
            p.drawLine(2, 19, 9, 19);
            p.drawLine(8, 11, 20, 11);
            p.drawLine(15, 6, 20, 11);
            p.drawLine(15, 16, 20, 11);
        }
        p.dispose();
    }
}
