package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.EmprestimoController.Evento;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
 * Pagina inicial:
 *   em cima   -> caixa azul esticada com a mensagem de boas-vindas e a mao acenando
 *   esquerda  -> os 6 botoes (GridLayout 2 linhas x 3 colunas)
 *   direita   -> historico de emprestimos e devolucoes, lido do banco e atualizado sozinho
 */
public class TelaInicio extends JPanel {
    private final JTable tabela = Estilo.criarTabela(new String[] {"Data e hora", "Ação", "Pessoa", "Equipamento"});
    private final JLabel aoVivo = new JLabel();
    private final Timer relogio;
    private List<Evento> ultimosEventos;
    private boolean buscando = false;
    private int batida = 0;

    public TelaInicio(JanelaPrincipal janela) {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        add(criarCaixaDeBoasVindas(), BorderLayout.NORTH);

        JPanel meio = new JPanel(new BorderLayout(14, 0));
        meio.setOpaque(false);
        meio.add(criarBotoes(janela), BorderLayout.CENTER);  // esquerda (ocupa o que sobrar)
        meio.add(criarHistorico(), BorderLayout.EAST);       // direita (largura fixa, mais estreita)
        add(meio, BorderLayout.CENTER);

        atualizar();

        // "Tempo real": o Timer bate a cada 1 segundo; a cada 2 batidas consulta o banco de novo
        relogio = new Timer(1000, e -> atualizar());
        relogio.start();
    }

    /** Quando a tela e trocada, o Timer para (senao continuaria rodando escondido). */
    @Override
    public void removeNotify() {
        relogio.stop();
        super.removeNotify();
    }

    // ------------------------------------------------------------ caixa de boas-vindas

    private JPanel criarCaixaDeBoasVindas() {
        JPanel caixa = new JPanel(new BorderLayout(18, 0));
        caixa.setBackground(Estilo.AZUL);
        caixa.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        caixa.putClientProperty("FlatLaf.style", "arc: 24"); // cantos arredondados

        // A imagem fica em src/main/resources/imagens/mao.png
        java.net.URL recurso = TelaInicio.class.getResource("/imagens/mao.png");
        JLabel mao = new JLabel(recurso == null ? null : new ImageIcon(recurso));

        JLabel mensagem = new JLabel("<html>"
            + "<span style='font-size:25px'><b>Olá, seja bem-vindo!</b></span><br>"
            + "<span style='font-size:13px'>Sistema de Empréstimo de Equipamentos de T.I.</span>"
            + "</html>");
        mensagem.setForeground(Color.WHITE);

        caixa.add(mao, BorderLayout.WEST);
        caixa.add(mensagem, BorderLayout.CENTER); // CENTER: a caixa fica esticada ate o fim
        return caixa;
    }

    // ------------------------------------------------------------ 6 botoes (esquerda)

    private JPanel criarBotoes(JanelaPrincipal janela) {
        JPanel grade = new JPanel(new GridLayout(2, 3, 12, 12)); // 2 linhas x 3 colunas
        grade.setOpaque(false);

        grade.add(criarBotao(janela, 1, "Equipamentos", "Situação e disponibilidade", Icone.COMPUTADOR, Estilo.AZUL));
        grade.add(criarBotao(janela, 2, "Cadastros", "Pessoas e equipamentos", Icone.MAIS, Estilo.VERDE));
        grade.add(criarBotao(janela, 3, "Consultar", "Emprestados e pessoas", Icone.LUPA, new Color(124, 58, 237)));
        grade.add(criarBotao(janela, 4, "Devolução", "Registrar e avaliar o estado", Icone.DEVOLVER, Estilo.LARANJA));
        grade.add(criarBotao(janela, 5, "Relatórios", "Estado na devolução", Icone.RELATORIO, new Color(13, 148, 136)));
        grade.add(criarBotao(janela, 6, "Empréstimos", "Emprestar equipamentos", Icone.PRANCHETA, new Color(219, 39, 119)));
        return grade;
    }

    /** Um botao grande: icone em cima e texto embaixo. Ao clicar, abre a tela escolhida. */
    private JButton criarBotao(JanelaPrincipal janela, int tela, String titulo, String descricao,
                               int tipoIcone, Color cor) {
        String corHex = String.format("#%06X", cor.getRGB() & 0xFFFFFF);
        JButton botao = new JButton("<html><div style='text-align:center'>"
            + "<b style='font-size:14px'>" + titulo + "</b><br>"
            + "<span style='font-size:11px;color:#64748B'>" + descricao + "</span></div></html>");
        botao.setIcon(new Icone(tipoIcone, cor, Color.WHITE, 60, true));
        botao.setHorizontalTextPosition(SwingConstants.CENTER); // texto centralizado...
        botao.setVerticalTextPosition(SwingConstants.BOTTOM);   // ...embaixo do icone
        botao.setIconTextGap(7);
        botao.putClientProperty("FlatLaf.style",
            "arc: 28; background: #FFFFFF; foreground: #0F172A; borderWidth: 1;"
            + " borderColor: #E2E8F0; hoverBorderColor: " + corHex + ";"
            + " focusedBorderColor: #CBD5E1; focusWidth: 0;"
            + " hoverBackground: #F8FAFC; focusedBackground: #FFFFFF; pressedBackground: #F1F5F9");
        botao.addActionListener(e -> janela.mostrar(tela));
        return botao;
    }

    // ------------------------------------------------------------ historico (direita)

    private JPanel criarHistorico() {
        JPanel cartao = Estilo.cartao();
        cartao.setPreferredSize(new Dimension(460, 0)); // largura fixa; a altura acompanha a janela

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(Estilo.subtitulo("Empréstimos e devoluções"), BorderLayout.WEST);
        cabecalho.add(aoVivo, BorderLayout.EAST);
        cartao.add(cabecalho, BorderLayout.NORTH);

        Estilo.colorirSituacao(tabela, 1);
        Estilo.larguras(tabela, 125, 100, 100, 135);
        cartao.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        return cartao;
    }

    /** Chamado a cada segundo: atualiza o relogio e, de 2 em 2 segundos, consulta o banco. */
    private void atualizar() {
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        aoVivo.setText("<html><span style='color:#16A34A'>●</span> "
            + "<span style='color:#64748B'>Ao vivo  " + hora + "</span></html>");

        batida++;
        if (batida % 2 == 0 || buscando) { // consulta na 1a batida, 3a, 5a... e nunca duas ao mesmo tempo
            return;
        }
        buscando = true;

        // O SwingWorker consulta o banco em outra thread: se o MySQL demorar, a tela nao trava.
        new SwingWorker<List<Evento>, Void>() {
            @Override
            protected List<Evento> doInBackground() {
                return Aplicacao.emprestimos().historico();
            }

            @Override
            protected void done() {
                buscando = false;
                try {
                    mostrar(get());
                } catch (Exception erro) {
                    aoVivo.setText("<html><span style='color:#DC2626'>●</span> "
                        + "<span style='color:#64748B'>Sem conexão com o banco</span></html>");
                }
            }
        }.execute();
    }

    /** "27/10/2026 14:30:05" vira "27/10 14:30:05" (cabe melhor na tabela estreita). */
    private String dataCurta(java.time.LocalDateTime quando) {
        String completa = Estilo.dataHoraSegundos(quando);
        return completa.substring(0, 5) + " " + completa.substring(11);
    }

    /** So refaz a tabela se houve novidade. */
    private void mostrar(List<Evento> eventos) {
        if (eventos.equals(ultimosEventos)) {
            return;
        }
        ultimosEventos = eventos;
        Estilo.modelo(tabela).setRowCount(0);
        for (Evento evento : eventos) {
            Estilo.modelo(tabela).addRow(new String[] {
                dataCurta(evento.quando()), evento.acao(), evento.pessoa(), evento.equipamento()});
        }
    }
}
