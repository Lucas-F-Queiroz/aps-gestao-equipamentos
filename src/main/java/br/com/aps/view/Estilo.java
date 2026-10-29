package br.com.aps.view;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.function.IntFunction;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

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

    private static final Color LINHA_ZEBRA = new Color(247, 250, 253);
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
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#2563EB"));
        FlatLightLaf.setup();

        // Deixa tudo mais arredondado e arejado (valores do proprio FlatLaf)
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("CheckBox.arc", 6);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);

        // Campos e listas mais altos (mais confortaveis para clicar)
        UIManager.put("TextField.margin", new Insets(5, 9, 5, 9));
        UIManager.put("ComboBox.padding", new Insets(5, 9, 5, 9));

        // Abas: mais altas, sem tracinhos entre elas e com sublinhado azul na escolhida
        UIManager.put("TabbedPane.tabHeight", 42);
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 22, 8, 22));
        UIManager.put("TabbedPane.tabSelectionHeight", 3);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.selectedForeground", AZUL);
        UIManager.put("TabbedPane.underlineColor", AZUL);
        UIManager.put("TabbedPane.hoverColor", new Color(226, 232, 240));

        // Cabecalho da tabela com a mesma margem lateral do texto das linhas (alinhados)
        UIManager.put("TableHeader.cellMargins", new Insets(4, 12, 4, 12));

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
            + " pressedBackground: #1E40AF; borderWidth: 0; focusWidth: 0; margin: 6,16,6,16");
        return botao;
    }

    /** Botao comum (branco com borda): acoes secundarias. */
    public static JButton botao(String texto) {
        JButton botao = new JButton(texto);
        botao.putClientProperty("FlatLaf.style", "margin: 6,16,6,16");
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

    /** Cartao branco com cantos arredondados e contorno suave. Tem BorderLayout e margem interna. */
    public static JPanel cartao() {
        return new PainelCartao();
    }

    /** Painel que desenha o proprio fundo branco arredondado e o contorno (nao usa a borda do Swing). */
    private static class PainelCartao extends JPanel {
        PainelCartao() {
            super(new BorderLayout(0, 10));
            setOpaque(false); // quem pinta o fundo e o metodo paintComponent abaixo
            setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g.setColor(BORDA);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g.dispose();
        }
    }

    /** Cartao com um numero grande: usado no resumo da tela inicial e no relatorio geral. */
    public static JPanel cartaoNumero(String titulo, String valor, Color cor) {
        JPanel painel = cartao();
        JLabel rotulo = new JLabel(titulo, bolinha(cor), SwingConstants.LEFT);
        rotulo.setIconTextGap(8);
        rotulo.setForeground(TEXTO_SUAVE);
        JLabel numero = new JLabel(valor);
        numero.setFont(numero.getFont().deriveFont(Font.BOLD, 32f));
        numero.setForeground(cor);
        painel.add(rotulo, BorderLayout.NORTH);
        painel.add(numero, BorderLayout.CENTER);
        return painel;
    }

    /** Bolinha colorida desenhada (no lugar do caractere "●", que depende da fonte). */
    private static Icon bolinha(Color cor) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }

            @Override
            public void paintIcon(Component componente, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(cor);
                g.fillOval(x, y, 10, 10);
                g.dispose();
            }
        };
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
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 15f));
        return rotulo;
    }

    /** Campo de formulario: texto pequeno em cima e o componente embaixo. */
    public static JPanel campo(String rotulo, JComponent componente) {
        JPanel painel = new JPanel(new BorderLayout(0, 4));
        painel.setOpaque(false);
        JLabel texto = new JLabel(rotulo);
        texto.setForeground(TEXTO_SUAVE);
        texto.setFont(texto.getFont().deriveFont(12f));
        painel.add(texto, BorderLayout.NORTH);
        painel.add(componente, BorderLayout.CENTER);
        return painel;
    }

    /**
     * Abas Swing padrao. Reconstroi somente a aba selecionada,
     * para que cada aba mostre dados atuais ao ser aberta.
     */
    public static JPanel criarAbas(String[] titulos, IntFunction<JPanel> criarTela) {
        JTabbedPane abas = new JTabbedPane();
        for (String titulo : titulos) {
            abas.addTab(titulo, new JPanel());
        }
        abas.setComponentAt(0, criarTela.apply(0));
        abas.addChangeListener(evento -> {
            int indice = abas.getSelectedIndex();
            if (indice >= 0) {
                abas.setComponentAt(indice, criarTela.apply(indice));
            }
        });

        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        painel.add(abas, BorderLayout.CENTER);
        return painel;
    }

    // ---------------------------------------------------------------- tabelas

    /**
     * Tabela somente leitura (o usuario nao edita as celulas), com visual limpo e ORDENACAO:
     * clicar no cabecalho de uma coluna ordena (setinha para cima/baixo); clicar de novo inverte.
     */
    public static JTable criarTabela(String[] colunas) {
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        JTable tabela = new JTable(modelo);

        // Ordenacao pelo cabecalho: o ComparadorCelulas entende numeros, datas e textos
        TableRowSorter<DefaultTableModel> ordenador = new TableRowSorter<>(modelo);
        ComparadorCelulas comparador = new ComparadorCelulas();
        for (int coluna = 0; coluna < colunas.length; coluna++) {
            ordenador.setComparator(coluna, comparador);
        }
        tabela.setRowSorter(ordenador);

        tabela.setDefaultRenderer(Object.class, new RenderizadorTabela());
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setRowHeight(34);
        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(BORDA);
        tabela.setIntercellSpacing(new Dimension(0, 1));

        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setBackground(new Color(237, 243, 252));
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 36));
        tabela.getTableHeader().setFont(tabela.getTableHeader().getFont().deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) tabela.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);
        return tabela;
    }

    /**
     * Posicao, no MODELO de dados, da linha selecionada (ou -1). Use sempre esta no lugar de
     * tabela.getSelectedRow(): com a tabela ordenada ou filtrada, a linha que o usuario ve
     * nao e a mesma posicao da lista de objetos que a tela guarda.
     */
    public static int linhaSelecionada(JTable tabela) {
        int visual = tabela.getSelectedRow();
        return visual < 0 ? -1 : tabela.convertRowIndexToModel(visual);
    }

    /** Texto num "selo" arredondado e colorido (o mesmo das tabelas), para formularios e janelas. */
    public static JLabel selo(String texto) {
        Color cor = corDaSituacao(texto);
        JLabel selo = new JLabel(texto, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color((cor.getRed() + 255 * 7) / 8, (cor.getGreen() + 255 * 7) / 8,
                    (cor.getBlue() + 255 * 7) / 8));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        selo.setForeground(cor);
        selo.setFont(selo.getFont().deriveFont(Font.BOLD));
        selo.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        return selo;
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

    /** Pinta a celula como um "selo" colorido conforme o valor: verde, azul, vermelho... */
    public static void colorirSituacao(JTable tabela, int coluna) {
        tabela.getColumnModel().getColumn(coluna).setCellRenderer(new RenderizadorSituacao());
    }

    /** Cor do selo para cada texto de situacao. */
    private static Color corDaSituacao(String texto) {
        switch (texto) {
            case "Disponível":
            case "Ativo":
            case "Devolvido":
            case "Novo":
            case "Bom":
                return VERDE;
            case "Emprestado":
            case "Aberto":
            case "Em uso":
                return AZUL;
            case "Atrasado":
            case "Danificado":
                return VERMELHO;
            default:
                return LARANJA; // Indisponível, Inativo, Regular
        }
    }

    /** Celulas comuns: texto escuro com margem lateral e linhas alternadas (zebra). */
    private static class RenderizadorTabela extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor,
                boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionado, foco, linha, coluna);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (!selecionado) {
                setBackground(linha % 2 == 0 ? Color.WHITE : LINHA_ZEBRA);
                setForeground(BARRA_LATERAL);
            }
            return this;
        }
    }

    /** Celulas de situacao: igual a comum, mas o texto aparece dentro de um selo arredondado colorido. */
    private static class RenderizadorSituacao extends DefaultTableCellRenderer {
        private Color cor = LARANJA;
        private boolean comSelo;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor,
                boolean selecionado, boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionado, foco, linha, coluna);
            String texto = String.valueOf(valor);
            comSelo = !texto.isBlank() && !texto.equals("-");
            cor = corDaSituacao(texto);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 10));
            setBackground(selecionado ? tabela.getSelectionBackground()
                : (linha % 2 == 0 ? Color.WHITE : LINHA_ZEBRA));
            return this;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            if (!comSelo) {
                g.setColor(BARRA_LATERAL);
                g.setFont(getFont());
                g.drawString(getText(), 12, (getHeight() + g.getFontMetrics().getAscent()
                    - g.getFontMetrics().getDescent()) / 2);
                g.dispose();
                return;
            }
            g.setFont(getFont().deriveFont(Font.BOLD));
            FontMetrics medidas = g.getFontMetrics();
            int largura = Math.min(getWidth() - 6, medidas.stringWidth(getText()) + 20);
            // fundo do selo: a cor misturada com branco (bem clarinha)
            g.setColor(new Color((cor.getRed() + 255 * 7) / 8, (cor.getGreen() + 255 * 7) / 8,
                (cor.getBlue() + 255 * 7) / 8));
            g.fillRoundRect(2, 6, largura, getHeight() - 12, getHeight() - 12, getHeight() - 12);
            g.setColor(cor);
            g.drawString(getText(), 12, (getHeight() - medidas.getHeight()) / 2 + medidas.getAscent());
            g.setStroke(new BasicStroke(1f));
            g.dispose();
        }
    }
}
