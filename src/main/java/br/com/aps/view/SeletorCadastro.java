package br.com.aps.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.function.IntConsumer;

import javax.swing.JButton;
import javax.swing.JPanel;

/** Seletor visual das duas abas; nao acessa regras de negocio nem persistencia. */
public final class SeletorCadastro extends JPanel {
    private final JButton[] botoes = new JButton[2];
    private int selecionado;

    /** Indice 0 = equipamentos; indice 1 = pessoas. */
    public SeletorCadastro(IntConsumer aoSelecionar) {
        setOpaque(false);
        setLayout(new GridLayout(1, 2, 0, 0));
        setPreferredSize(new Dimension(330, 40));

        String[] nomes = {"Equipamentos", "Pessoas"};
        for (int i = 0; i < nomes.length; i++) {
            final int indice = i;
            JButton botao = new JButton(nomes[i]) {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D desenho = (Graphics2D) graphics.create();
                    desenho.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                    Path2D forma = new Path2D.Float();
                    forma.moveTo(0, getHeight());
                    forma.lineTo(6, 10);
                    forma.quadTo(8, 1, 18, 1);
                    forma.lineTo(getWidth() - 18, 1);
                    forma.quadTo(getWidth() - 8, 1, getWidth() - 6, 10);
                    forma.lineTo(getWidth(), getHeight());
                    forma.closePath();

                    desenho.setColor(indice == selecionado ? Estilo.AZUL : new Color(241, 245, 249));
                    desenho.fill(forma);
                    desenho.setColor(Estilo.BORDA);
                    desenho.draw(forma);
                    desenho.dispose();
                    super.paintComponent(graphics);
                }
            };

            botao.setOpaque(false);
            botao.setContentAreaFilled(false);
            botao.setBorderPainted(false);
            botao.setFocusPainted(true);
            botao.addActionListener(evento -> {
                selecionado = indice;
                atualizarCores();
                aoSelecionar.accept(indice);
            });

            botoes[i] = botao;
            add(botao);
        }
        atualizarCores();
    }

    private void atualizarCores() {
        for (int i = 0; i < botoes.length; i++) {
            Color cor = i == selecionado ? Color.WHITE : Estilo.BARRA_LATERAL;
            botoes[i].setForeground(cor);
            botoes[i].setSelected(i == selecionado);
            botoes[i].setIcon(new IconeAcao(i == 0 ? "equipamento" : "pessoa", cor));
        }
        repaint();
    }
}
