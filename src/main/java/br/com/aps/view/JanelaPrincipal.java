package br.com.aps.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Janela unica do sistema, que ocupa a tela inteira. Em vez de abrir varias janelinhas,
 * o conteudo do centro troca conforme a tela escolhida. Estrutura (BorderLayout):
 *
 *   NORTH  -> barra superior (nome do sistema + botao Sair no canto direito)
 *   CENTER -> a tela atual (Inicio ou uma das 6 areas)
 */
public class JanelaPrincipal extends JFrame {
    private final String[] nomes = {"Início", "Equipamentos", "Cadastros", "Consultar", "Devolução", "Relatórios", "Empréstimos"};
    private final JPanel esquerdaBarra = new JPanel(new BorderLayout());
    private final JButton botaoSaida = Estilo.botao("Sair");
    private JLabel marca;
    private int telaAtual;
    private final JPanel areaConteudo = new JPanel(new BorderLayout());

    public JanelaPrincipal() {
        this(0); // abre no Inicio
    }

    /** Abre ja numa tela: 0 = Inicio, 1 = Equipamentos, 2 = Cadastros, 3 = Consultar, 4 = Devolucao, 5 = Relatorios, 6 = Emprestimos. */
    public JanelaPrincipal(int telaInicial) {
        super("Sistema de Empréstimo de Equipamentos de T.I.");

        // Clicar no X da janela NAO fecha direto: chama o metodo sair(), que pergunta antes.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sair();
            }

            // Ao abrir, o foco vai para o conteudo (senao o botao Sair aparece "selecionado")
            @Override
            public void windowOpened(WindowEvent e) {
                areaConteudo.requestFocusInWindow();
            }
        });

        setLayout(new BorderLayout());
        add(criarBarraSuperior(), BorderLayout.NORTH);

        areaConteudo.setBackground(Estilo.FUNDO);
        areaConteudo.setFocusable(true); // o foco inicial fica aqui, e nao no botao Sair
        areaConteudo.setBorder(BorderFactory.createEmptyBorder(16, 22, 18, 22));
        add(areaConteudo, BorderLayout.CENTER);

        setSize(1280, 760);                        // tamanho caso o usuario "restaure" a janela
        setMinimumSize(new Dimension(1200, 660));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // abre ocupando a tela inteira

        mostrar(telaInicial);
    }

    private JPanel criarBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDA),
            BorderFactory.createEmptyBorder(8, 22, 8, 22)));

        marca = new JLabel("<html><b style='font-size:16px'>TI Fácil</b>"
            + "<span style='color:#64748B'>&nbsp;&nbsp;Empréstimo de equipamentos</span></html>");
        esquerdaBarra.setOpaque(false);
        esquerdaBarra.add(marca, BorderLayout.CENTER);
        barra.add(esquerdaBarra, BorderLayout.WEST);

        // Canto direito: somente a navegação. Não exibimos identidade sem autenticação real.
        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        direita.setOpaque(false);
        JButton sair = botaoSaida;
        sair.setIcon(new Icone(Icone.SAIR, Estilo.VERMELHO, Estilo.BARRA_LATERAL, 18, false));
        sair.setIconTextGap(8);
        sair.addActionListener(e -> { if (telaAtual != 0) mostrar(0); else sair(); });
        direita.add(sair);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    /** Troca a tela do centro. Tudo, menos o Inicio, ganha um cabecalho com o botao "Inicio". */
    public void mostrar(int numero) {
        telaAtual = numero;
        esquerdaBarra.removeAll();
        esquerdaBarra.add(marca, BorderLayout.CENTER);
        botaoSaida.setText(numero != 0 ? "Voltar" : "Sair");
        botaoSaida.setIcon(numero != 0 ? new IconeAcao("voltar", Estilo.VERMELHO) : new Icone(Icone.SAIR, Estilo.VERMELHO, Estilo.BARRA_LATERAL, 18, false));
        areaConteudo.setBorder(BorderFactory.createEmptyBorder(numero == 2 ? 8 : 16, 22, 14, 22));
        JPanel tela;
        if (numero == 0) {
            tela = new TelaInicio(this);
        } else {
            JPanel conteudo;
            if (numero == 1) {
                conteudo = new TelaEquipamentos();
            } else if (numero == 2) {
                conteudo = new TelaCadastros();
            } else if (numero == 3) {
                conteudo = new TelaConsultar();
            } else if (numero == 4) {
                conteudo = new TelaDevolucao();
            } else if (numero == 5) {
                conteudo = new TelaRelatorios();
            } else {
                conteudo = new TelaEmprestimos();
            }
            tela = numero == 1 || numero == 2 || numero == 5 ? conteudo : criarPaginaInterna(nomes[numero], conteudo, false);
        }

        esquerdaBarra.revalidate();
        esquerdaBarra.repaint();
        areaConteudo.removeAll();
        areaConteudo.add(tela, BorderLayout.CENTER);
        areaConteudo.revalidate();
        areaConteudo.repaint();
        areaConteudo.requestFocusInWindow();
    }

    /** Pagina de uma area: botao "Inicio" e titulo em cima, a tela embaixo. */
    private JPanel criarPaginaInterna(String titulo, JPanel conteudo, boolean mostrarInicio) {
        JPanel cabecalho = new JPanel(new BorderLayout(18, 0));
        cabecalho.setOpaque(false);

        JButton inicio = Estilo.botao("Início");
        inicio.setIcon(new Icone(Icone.CASA, Estilo.BARRA_LATERAL, Color.WHITE, 18, false));
        inicio.setIconTextGap(8);
        inicio.addActionListener(e -> mostrar(0));

        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, mostrarInicio ? 20f : 17f));

        if (mostrarInicio) cabecalho.add(inicio, BorderLayout.WEST);
        cabecalho.add(rotulo, BorderLayout.CENTER);

        JPanel pagina = new JPanel(new BorderLayout(0, mostrarInicio ? 18 : 10));
        pagina.setOpaque(false);
        pagina.add(cabecalho, BorderLayout.NORTH);
        pagina.add(conteudo, BorderLayout.CENTER);
        return pagina;
    }

    private void sair() {
        int resposta = JOptionPane.showConfirmDialog(
            this, "Deseja realmente sair do sistema?", "Sair", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
