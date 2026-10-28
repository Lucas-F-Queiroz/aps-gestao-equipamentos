package br.com.aps.view;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Cores e "atalhos" de visual. Concentrar aqui evita repetir codigo nas telas:
 * para mudar o estilo do sistema inteiro, basta mexer neste arquivo.
 */
public class Estilo {
    public static final Color FUNDO = new Color(241, 245, 249);
    public static final Color BORDA = new Color(226, 232, 240);
    public static final Color TEXTO_SUAVE = new Color(100, 116, 139);
    public static final Color AZUL = new Color(37, 99, 235);
    public static final Color VERDE = new Color(22, 163, 74);
    public static final Color LARANJA = new Color(234, 88, 12);
    public static final Color VERMELHO = new Color(220, 38, 38);
    public static final Color BARRA_LATERAL = new Color(15, 23, 42);

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA_HORA_SEGUNDOS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATA_CURTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** "27/10/2026 14:30" (ou "-" se nao tem data). */
    public static String dataHora(LocalDateTime data) {
        return data == null ? "-" : data.format(DATA_HORA);
    }

    /** "27/10/2026 14:30:05" */
    public static String dataHoraSegundos(LocalDateTime data) {
        return data == null ? "-" : data.format(DATA_HORA_SEGUNDOS);
    }

    /** "27/10/2026" */
    public static String dia(LocalDateTime data) {
        return data == null ? "-" : data.format(DATA_CURTA);
    }

    /** Liga o FlatLaf. Chame uma vez, no inicio do programa, antes de criar janelas. */
    public static void instalar() {
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 12));
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#2563EB"));
        FlatLightLaf.setup();

        // Deixa tudo mais arredondado e arejado (valores do proprio FlatLaf)
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);

        // Campos e listas mais altos (mais confortaveis para clicar)
        UIManager.put("TextField.margin", new Insets(4, 8, 4, 8));
        UIManager.put("ComboBox.padding", new Insets(4, 8, 4, 8));

        // Linha selecionada da tabela: azul bem clarinho com texto escuro
        Color azulClaro = new Color(219, 234, 254);
        Color textoEscuro = new Color(15, 23, 42);
        UIManager.put("Table.selectionBackground", azulClaro);
        UIManager.put("Table.selectionForeground", textoEscuro);
        UIManager.put("Table.selectionInactiveBackground", azulClaro);
        UIManager.put("Table.selectionInactiveForeground", textoEscuro);
    }

    // ---------------------------------------------------------------- botoes

    /** Botao de destaque (azul): use para a acao principal da tela. */
    public static JButton botaoPrincipal(String texto) {
        JButton botao = new JButton(texto);
        botao.putClientProperty("FlatLaf.style",
            "background: #2563EB; foreground: #FFFFFF; hoverBackground: #1D4ED8;"
            + " pressedBackground: #1E40AF; borderWidth: 0; focusWidth: 0; margin: 5,14,5,14");
        return botao;
    }

    /** Botao comum (branco com borda): acoes secundarias. */
    public static JButton botao(String texto) {
        JButton botao = new JButton(texto);
        botao.putClientProperty("FlatLaf.style", "margin: 5,14,5,14");
        return botao;
    }

    /** Botao de "aba" (escolhe entre duas visoes na mesma tela). */
    public static JButton botaoAba(String texto) {
        JButton botao = new JButton(texto);
        marcarAba(botao, false);
        return botao;
    }

    /** Pinta a aba como escolhida (azul) ou normal (branca). */
    public static void marcarAba(JButton botao, boolean escolhida) {
        String estilo;
        if (escolhida) {
            estilo = "background: #2563EB; foreground: #FFFFFF; hoverBackground: #2563EB;"
                + " focusedBackground: #2563EB; focusedForeground: #FFFFFF;"
                + " pressedBackground: #1D4ED8; borderWidth: 0; focusWidth: 0";
        } else {
            estilo = "background: #FFFFFF; foreground: #334155; hoverBackground: #F1F5F9;"
                + " focusedBackground: #FFFFFF; pressedBackground: #E2E8F0; focusWidth: 0";
        }
        botao.putClientProperty("FlatLaf.style", estilo + "; arc: 10; margin: 6,20,6,20");
    }

    // ---------------------------------------------------------------- paineis

    /** Cartao branco com cantos arredondados. Tem BorderLayout e margem interna. */
    public static JPanel cartao() {
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createEmptyBorder(13, 16, 13, 16));
        painel.putClientProperty("FlatLaf.style", "arc: 20");
        return painel;
    }

    /**
     * Linha de botoes alinhada a esquerda, todos com a mesma largura.
     * GridLayout(1, n) = 1 linha e n colunas. O BorderLayout.WEST impede que estiquem.
     */
    public static JPanel linhaBotoes(JButton... botoes) {
        JPanel grade = new JPanel(new GridLayout(1, botoes.length, 10, 0));
        grade.setOpaque(false);
        for (JButton botao : botoes) {
            grade.add(botao);
        }
        JPanel linha = new JPanel(new BorderLayout());
        linha.setOpaque(false);
        linha.add(grade, BorderLayout.WEST);
        return linha;
    }

    public static JLabel subtitulo(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 14f));
        return rotulo;
    }

    /** Campo de formulario: texto pequeno em cima e o componente embaixo. */
    public static JPanel campo(String rotulo, JComponent componente) {
        JPanel painel = new JPanel(new BorderLayout(0, 3));
        painel.setOpaque(false);
        JLabel texto = new JLabel(rotulo);
        texto.setForeground(TEXTO_SUAVE);
        texto.setFont(texto.getFont().deriveFont(11f));
        painel.add(texto, BorderLayout.NORTH);
        painel.add(componente, BorderLayout.CENTER);
        return painel;
    }

    // ---------------------------------------------------------------- tabelas

    /** Tabela somente leitura (o usuario nao edita as celulas), ja com visual limpo. */
    public static JTable criarTabela(String[] colunas) {
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return "Ações".equals(getColumnName(coluna));
            }
        };
        JTable tabela = new JTable(modelo);
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int l, int c) {
                super.getTableCellRendererComponent(t,v,sel,foco,l,c);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                if(!sel) { setBackground(l % 2 == 0 ? Color.WHITE : new Color(246,249,252)); setForeground(BARRA_LATERAL); }
                return this;
            }
        });
        tabela.getTableHeader().setBackground(new Color(237,243,252));
        tabela.getTableHeader().setPreferredSize(new java.awt.Dimension(0,30));
        tabela.setTableHeader(new javax.swing.table.JTableHeader(tabela.getColumnModel()) {
            @Override public String getToolTipText(java.awt.event.MouseEvent e) {
                int c=columnAtPoint(e.getPoint());return c<0?null:String.valueOf(getColumnModel().getColumn(c).getHeaderValue());
            }
        });
        tabela.getTableHeader().setBackground(new Color(237,243,252));
        tabela.getTableHeader().setPreferredSize(new java.awt.Dimension(0,30));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setRowHeight(30);
        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(BORDA);
        tabela.setIntercellSpacing(new java.awt.Dimension(0, 1));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setFont(tabela.getTableHeader().getFont().deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) tabela.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);
        return tabela;
    }

    /** Largura de cada coluna (em pixels); a tabela ajusta as proporcoes ao tamanho da janela. */
    public static void larguras(JTable tabela, int... larguras) {
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        // A primeira coluna e estreita nas tabelas operacionais (ID ou patrimonio).
        tabela.getColumnModel().getColumn(0).setMaxWidth(larguras[0] + 20);
    }

    public static DefaultTableModel modelo(JTable tabela) {
        return (DefaultTableModel) tabela.getModel();
    }

    /** Coloca a tabela dentro de uma barra de rolagem sem borda. */
    public static JScrollPane rolagem(JTable tabela) {
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.getViewport().setBackground(Color.WHITE);
        return rolagem;
    }

    /** Pinta o texto da coluna conforme o valor: verde, azul, vermelho... */
    public static void colorirSituacao(JTable tabela, int coluna) {
        tabela.getColumnModel().getColumn(coluna).setCellRenderer(new RenderizadorSituacao());
    }

    private static class RenderizadorSituacao extends DefaultTableCellRenderer {
        private Color tinta;
        @Override public Component getTableCellRendererComponent(JTable tabela, Object valor,
                boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionado, foco, linha, coluna);
            String texto = String.valueOf(valor);
            tinta = texto.equals("Atrasado") || texto.equals("Danificado") ? VERMELHO
                : texto.equals("Devolvido") || texto.equals("Disponível") ? AZUL
                : texto.equals("Novo") || texto.equals("Bom") || texto.equals("Em uso") || texto.equals("Ativo") ? VERDE
                : LARANJA;
            setBackground(selecionado ? tabela.getSelectionBackground() : linha % 2 == 0 ? Color.WHITE : new Color(246,249,252));
            return this;
        }
        @Override protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g=(java.awt.Graphics2D)graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());g.fillRect(0,0,getWidth(),getHeight());
            g.setFont(getFont());var fm=g.getFontMetrics();int w=Math.min(getWidth()-12,fm.stringWidth(getText())+16);
            g.setColor(new Color((tinta.getRed()+255*7)/8,(tinta.getGreen()+255*7)/8,(tinta.getBlue()+255*7)/8));
            g.fillRoundRect(6,5,w,getHeight()-10,8,8);g.setColor(tinta);
            g.drawString(getText(),14,(getHeight()-fm.getHeight())/2+fm.getAscent());g.dispose();
        }
    }
}
