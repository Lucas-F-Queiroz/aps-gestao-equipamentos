package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Pagina inicial. Em cima, a caixa de boas-vindas. Embaixo, a esquerda, os tres atalhos
 * (Cadastros, Emprestimos e Consultas) e, a direita, o "Historico ao vivo": os emprestimos mais
 * recentes, relidos do banco a cada 5 segundos (Timer + SwingWorker, para a tela nao travar).
 */
public class TelaInicio extends JPanel {
    private static final int INTERVALO_MS = 5000;
    private static final int LINHAS_NO_HISTORICO = 12;

    private final JTable tabela = Estilo.criarTabela(
        new String[]{"Pessoa", "Equipamento", "Retirada", "Devolução prevista", "Situação"});
    private final Timer relogio = new Timer(INTERVALO_MS, evento -> atualizar());
    private boolean buscando = false;

    public TelaInicio(JanelaPrincipal janela) {
        super(new BorderLayout(0, 16));
        setOpaque(false);

        add(criarBoasVindas(), BorderLayout.NORTH);

        JPanel meio = new JPanel(new BorderLayout(16, 0));
        meio.setOpaque(false);
        meio.add(criarAtalhos(janela), BorderLayout.WEST);   // largura fixa
        meio.add(criarHistorico(janela), BorderLayout.CENTER); // ocupa o que sobrar
        add(meio, BorderLayout.CENTER);

        atualizar();
        relogio.start();
    }

    /** Quando a tela e trocada, o Timer para (senao continuaria rodando escondido). */
    @Override
    public void removeNotify() {
        relogio.stop();
        super.removeNotify();
    }

    // ------------------------------------------------------------ boas-vindas

    private JPanel criarBoasVindas() {
        JPanel caixa = new JPanel(new BorderLayout(22, 0));
        caixa.setBackground(Estilo.AZUL);
        caixa.setBorder(BorderFactory.createEmptyBorder(18, 26, 18, 26));
        caixa.putClientProperty("FlatLaf.style", "arc: 24");

        // A imagem fica em src/main/resources/imagens/mao.png (se faltar, so nao aparece)
        URL arquivo = TelaInicio.class.getResource("/imagens/mao.png");
        if (arquivo != null) {
            caixa.add(new JLabel(new ImageIcon(arquivo)), BorderLayout.WEST);
        }

        String data = LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("pt", "BR")));
        data = data.substring(0, 1).toUpperCase() + data.substring(1);
        JLabel mensagem = new JLabel("<html>"
            + "<span style='font-size:28px'><b>Olá, seja bem-vindo!</b></span><br>"
            + "<span style='font-size:14px'><b>Sistema de Empréstimo de Equipamentos de T.I.</b></span><br>"
            + "<span style='font-size:12px;color:#BFDBFE'>" + data + "</span></html>");
        mensagem.setForeground(Color.WHITE);
        caixa.add(mensagem, BorderLayout.CENTER);
        return caixa;
    }

    // ------------------------------------------------------------ atalhos (esquerda)

    private JPanel criarAtalhos(JanelaPrincipal janela) {
        JPanel coluna = new JPanel(new GridLayout(3, 1, 0, 14)); // 3 linhas, 1 coluna
        coluna.setOpaque(false);
        coluna.setPreferredSize(new Dimension(520, 0));
        coluna.add(criarAtalho(janela, 1, "Cadastros", "Pessoas e equipamentos", Icone.USUARIOS,
            Estilo.VERDE, "#F0FDF4", "#BBF7D0"));
        coluna.add(criarAtalho(janela, 2, "Empréstimos", "Registrar empréstimo e devolução", Icone.PRANCHETA,
            Estilo.AZUL, "#EFF6FF", "#BFDBFE"));
        coluna.add(criarAtalho(janela, 3, "Consultas", "Inventário, situação e relatórios", Icone.LUPA,
            new Color(124, 58, 237), "#FAF5FF", "#E9D5FF"));
        return coluna;
    }

    /**
     * Cartao-botao: icone e texto a esquerda, seta a direita. O botao tem BorderLayout proprio
     * (os rotulos dentro dele deixam o clique passar para o botao).
     */
    private JButton criarAtalho(JanelaPrincipal janela, int area, String titulo, String descricao,
                                int icone, Color cor, String fundo, String borda) {
        JButton botao = new JButton();
        botao.setLayout(new BorderLayout(12, 0));

        JLabel texto = new JLabel("<html><b style='font-size:17px'>" + titulo + "</b><br>"
            + "<span style='color:#64748B'>" + descricao + "</span></html>",
            new Icone(icone, cor, Color.WHITE, 64, true), SwingConstants.LEFT);
        texto.setIconTextGap(18);
        botao.add(texto, BorderLayout.CENTER);
        botao.add(new JLabel(new IconeAcao("seta", cor)), BorderLayout.EAST);

        botao.putClientProperty("FlatLaf.style",
            "arc: 20; margin: 12,18,12,18; background: " + fundo + "; foreground: #0F172A;"
            + " borderWidth: 1; borderColor: " + borda + "; hoverBorderColor: "
            + String.format("#%06X", cor.getRGB() & 0xFFFFFF) + "; focusWidth: 0");
        botao.addActionListener(evento -> janela.mostrar(area));
        return botao;
    }

    // ------------------------------------------------------------ historico ao vivo (direita)

    private JPanel criarHistorico(JanelaPrincipal janela) {
        JPanel cartao = Estilo.cartao();

        JLabel titulo = new JLabel("Histórico ao vivo", new IconeAcao("relogio", Estilo.AZUL), SwingConstants.LEFT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setIconTextGap(10);
        JButton verTodos = Estilo.botao("Ver todos");
        verTodos.setIcon(new IconeAcao("seta", Estilo.BARRA_LATERAL));
        verTodos.setHorizontalTextPosition(SwingConstants.LEFT); // seta depois do texto
        verTodos.addActionListener(evento -> janela.mostrar(3, 1)); // Consultas > Situacao dos emprestimos

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(verTodos, BorderLayout.EAST);
        cartao.add(cabecalho, BorderLayout.NORTH);

        Estilo.larguras(tabela, 140, 250, 140, 140, 110);
        Estilo.colorirSituacao(tabela, 4);
        cartao.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        return cartao;
    }

    /** Le os emprestimos mais recentes em outra thread (o SwingWorker) e redesenha a tabela. */
    private void atualizar() {
        if (buscando) {
            return; // a consulta anterior ainda nao terminou
        }
        buscando = true;
        new SwingWorker<List<EmprestimoDetalhe>, Void>() {
            @Override
            protected List<EmprestimoDetalhe> doInBackground() {
                return Aplicacao.emprestimos().listarDetalhado(); // ja vem do mais novo para o mais antigo
            }

            @Override
            protected void done() {
                buscando = false;
                try {
                    mostrar(get());
                } catch (Exception erro) {
                    // sem conexao agora: mantem o que ja esta na tela e tenta de novo no proximo ciclo
                }
            }
        }.execute();
    }

    private void mostrar(List<EmprestimoDetalhe> emprestimos) {
        Estilo.modelo(tabela).setRowCount(0);
        for (EmprestimoDetalhe d : emprestimos.subList(0, Math.min(LINHAS_NO_HISTORICO, emprestimos.size()))) {
            Emprestimo e = d.emprestimo();
            Estilo.modelo(tabela).addRow(new Object[]{d.usuarioNome(),
                d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")",
                Estilo.dataHora(e.getDataRetirada()), Estilo.dia(e.getDataPrevista()), Regras.situacao(e)});
        }
    }
}
