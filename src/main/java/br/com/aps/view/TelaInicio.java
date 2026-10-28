package br.com.aps.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Pagina inicial: somente as tres areas do sistema.
 * As listagens e consultas ficam dentro de cada area, nao no inicio.
 */
public class TelaInicio extends JPanel {
    public TelaInicio(JanelaPrincipal janela) {
        super(new GridBagLayout());
        setOpaque(false);

        JPanel conteudo = new JPanel(new BorderLayout(0, 24));
        conteudo.setOpaque(false);
        conteudo.setPreferredSize(new Dimension(900, 235));

        JPanel apresentacao = new JPanel(new GridLayout(2, 1, 0, 5));
        apresentacao.setOpaque(false);

        JLabel titulo = new JLabel("Gestão de empréstimos");
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 25f));
        titulo.setForeground(Estilo.BARRA_LATERAL);

        JLabel descricao = new JLabel("Selecione a área em que deseja trabalhar.");
        descricao.setForeground(Estilo.TEXTO_SUAVE);
        apresentacao.add(titulo);
        apresentacao.add(descricao);
        conteudo.add(apresentacao, BorderLayout.NORTH);

        JPanel areas = new JPanel(new GridLayout(1, 3, 16, 0));
        areas.setOpaque(false);
        areas.add(criarAtalho(janela, 1, "Cadastros", "Pessoas e equipamentos",
            Icone.USUARIOS));
        areas.add(criarAtalho(janela, 2, "Empréstimos", "Registrar e devolver",
            Icone.PRANCHETA));
        areas.add(criarAtalho(janela, 3, "Consultas", "Inventário, buscas e relatórios",
            Icone.LUPA));
        conteudo.add(areas, BorderLayout.CENTER);

        add(conteudo);
    }

    /** Cada atalho abre uma area; nenhuma consulta ao banco ocorre no inicio. */
    private JButton criarAtalho(JanelaPrincipal janela, int area, String titulo,
                               String descricao, int icone) {
        JButton botao = new JButton("<html><b>" + titulo + "</b><br>"
            + "<span style='color:#64748B'>" + descricao + "</span></html>");
        botao.setIcon(new Icone(icone, Estilo.AZUL, Color.WHITE, 40, false));
        botao.setHorizontalAlignment(SwingConstants.LEFT);
        botao.setIconTextGap(14);
        botao.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        botao.putClientProperty("FlatLaf.style",
            "arc: 14; background: #FFFFFF; foreground: #0F172A;"
            + " borderWidth: 1; borderColor: #E2E8F0;"
            + " hoverBackground: #F8FAFC; focusWidth: 0");
        botao.addActionListener(evento -> janela.mostrar(area));
        return botao;
    }
}
