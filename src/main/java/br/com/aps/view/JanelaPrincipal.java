package br.com.aps.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Janela principal e navegacao do sistema.
 * Inicio -> Cadastros / Emprestimos / Consultas.
 * Cada area agrupa telas relacionadas, sem duplicar funcionalidades.
 */
public class JanelaPrincipal extends JFrame {
    private final JPanel areaConteudo = new JPanel(new BorderLayout());
    private final JButton botaoVoltar = Estilo.botao("Sair");
    private int areaAtual;

    public JanelaPrincipal() {
        this(0);
    }

    /** 0 = Inicio; 1 = Cadastros; 2 = Emprestimos; 3 = Consultas. */
    public JanelaPrincipal(int areaInicial) {
        super("Gestao de Emprestimos de Equipamentos - TI Facil");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                sair();
            }
        });

        setLayout(new BorderLayout());
        add(criarBarraSuperior(), BorderLayout.NORTH);
        areaConteudo.setBackground(Estilo.FUNDO);
        areaConteudo.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        add(areaConteudo, BorderLayout.CENTER);

        setSize(1150, 730);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        mostrar(areaInicial);
    }

    private JPanel criarBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDA),
            BorderFactory.createEmptyBorder(10, 22, 10, 22)));

        JLabel marca = new JLabel("TI Facil  |  Gestao de equipamentos");
        marca.setForeground(Estilo.BARRA_LATERAL);
        marca.setFont(marca.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        barra.add(marca, BorderLayout.WEST);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        direita.setOpaque(false);
        botaoVoltar.addActionListener(evento -> {
            if (areaAtual == 0) {
                sair();
            } else {
                mostrar(0);
            }
        });
        direita.add(botaoVoltar);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    /** Troca somente o painel central, mantendo uma janela unica. */
    public void mostrar(int area) {
        if (area < 0 || area > 3) {
            throw new IllegalArgumentException("Area de navegacao invalida.");
        }
        areaAtual = area;
        botaoVoltar.setText(area == 0 ? "Sair" : "Voltar ao inicio");

        JPanel conteudo = switch (area) {
            case 1 -> new TelaCadastros();
            case 2 -> criarAbas(
                new String[]{"Registrar emprestimo", "Registrar devolucao"},
                new JPanel[]{new TelaEmprestimos(), new TelaDevolucao()});
            case 3 -> criarAbas(
                new String[]{"Inventario", "Situacao dos emprestimos", "Relatorios"},
                new JPanel[]{new TelaEquipamentos(), new TelaConsultar(), new TelaRelatorios()});
            default -> new TelaInicio(this);
        };

        areaConteudo.removeAll();
        areaConteudo.add(conteudo, BorderLayout.CENTER);
        areaConteudo.revalidate();
        areaConteudo.repaint();
    }

    /** JTabbedPane faz parte do Swing: nao precisamos de uma tela de navegacao extra. */
    private JPanel criarAbas(String[] titulos, JPanel[] telas) {
        JTabbedPane abas = new JTabbedPane();
        for (int i = 0; i < titulos.length; i++) {
            abas.addTab(titulos[i], telas[i]);
        }

        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        painel.add(abas, BorderLayout.CENTER);
        return painel;
    }

    private void sair() {
        int resposta = JOptionPane.showConfirmDialog(
            this, "Deseja realmente sair do sistema?", "Sair", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            dispose();
        }
    }
}
