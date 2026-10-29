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
import javax.swing.JTabbedPane;

/**
 * Janela principal e navegacao do sistema.
 * Inicio -> Cadastros / Emprestimos / Consultas.
 * Cada area agrupa telas relacionadas em abas, sem duplicar funcionalidades.
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
        super("Gestão de Empréstimos de Equipamentos - TI Fácil");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                sair();
            }

            // Ao abrir, o foco vai para o conteudo (senao o botao do canto aparece "selecionado")
            @Override
            public void windowOpened(WindowEvent evento) {
                areaConteudo.requestFocusInWindow();
            }
        });

        setLayout(new BorderLayout());
        add(criarBarraSuperior(), BorderLayout.NORTH);
        areaConteudo.setBackground(Estilo.FUNDO);
        areaConteudo.setFocusable(true);
        areaConteudo.setBorder(BorderFactory.createEmptyBorder(18, 24, 20, 24));
        add(areaConteudo, BorderLayout.CENTER);

        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // janela unica, ocupando a tela inteira
        mostrar(areaInicial);
    }

    private JPanel criarBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDA),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)));

        // Marca: icone num quadradinho azul claro + nome do sistema
        JLabel marca = new JLabel("<html><b style='font-size:15px'>TI Fácil</b>"
            + "<span style='color:#64748B'>&nbsp;&nbsp;Gestão de empréstimos de equipamentos</span></html>");
        marca.setIcon(new Icone(Icone.COMPUTADOR, Estilo.AZUL, Color.WHITE, 36, true));
        marca.setIconTextGap(12);
        barra.add(marca, BorderLayout.WEST);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        direita.setOpaque(false);
        botaoVoltar.setIconTextGap(8);
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
        mostrar(area, 0);
    }

    /** Igual a mostrar(area), ja abrindo a aba escolhida (ex.: mostrar(3, 1) = Consultas > Situacao dos emprestimos). */
    public void mostrar(int area, int aba) {
        if (area < 0 || area > 3) {
            throw new IllegalArgumentException("Area de navegacao invalida.");
        }
        areaAtual = area;
        if (area == 0) {
            botaoVoltar.setText("Sair");
            botaoVoltar.setIcon(new Icone(Icone.SAIR, Estilo.VERMELHO, Estilo.BARRA_LATERAL, 18, false));
        } else {
            botaoVoltar.setText("Voltar ao início");
            botaoVoltar.setIcon(new Icone(Icone.CASA, Estilo.AZUL, Estilo.BARRA_LATERAL, 18, false));
        }

        JPanel conteudo = switch (area) {
            case 1 -> new TelaCadastros();
            case 2 -> Estilo.criarAbas(
                new String[]{"Registrar empréstimo", "Registrar devolução"},
                indice -> indice == 0 ? new TelaEmprestimos() : new TelaDevolucao());
            case 3 -> Estilo.criarAbas(
                new String[]{"Inventário", "Situação dos empréstimos", "Relatórios"},
                indice -> switch (indice) {
                    case 0 -> new TelaEquipamentos();
                    case 1 -> new TelaConsultar();
                    default -> new TelaRelatorios();
                });
            default -> new TelaInicio(this);
        };

        if (aba > 0 && conteudo.getComponentCount() > 0 && conteudo.getComponent(0) instanceof JTabbedPane abas) {
            abas.setSelectedIndex(aba);
        }

        areaConteudo.removeAll();
        areaConteudo.add(conteudo, BorderLayout.CENTER);
        areaConteudo.revalidate();
        areaConteudo.repaint();
    }

    private void sair() {
        int resposta = JOptionPane.showConfirmDialog(
            this, "Deseja realmente sair do sistema?", "Sair", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            dispose();
        }
    }
}
