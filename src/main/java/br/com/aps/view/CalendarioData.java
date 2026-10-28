package br.com.aps.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * Campo Swing para escolher a data de devolucao prevista.
 * Aceita digitacao direta e calendario. Datas invalidas sao rejeitadas pelo Controller.
 */
public final class CalendarioData extends JPanel {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter
        .ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT);

    private static final String[] MESES = {
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };
    private static final String[] DIAS_SEMANA = {"D", "S", "T", "Q", "Q", "S", "S"};

    private final JTextField campo;
    private final JPopupMenu popup = new JPopupMenu();
    private final JPanel calendario = new JPanel(new BorderLayout(6, 8));
    private final JPanel dias = new JPanel(new GridLayout(0, 7, 3, 3));
    private final JComboBox<String> meses = new JComboBox<>(MESES);
    private final JSpinner ano = new JSpinner(
        new SpinnerNumberModel(LocalDate.now().getYear(), 1, 9999, 1));

    private YearMonth exibido = YearMonth.now();
    private boolean ajustando;

    public CalendarioData(JTextField campo) {
        super(new BorderLayout(4, 0));
        this.campo = campo;
        setOpaque(false);

        JButton abrir = Estilo.botao("");
        abrir.setIcon(new IconeAcao("calendario", Estilo.TEXTO_SUAVE));
        abrir.setToolTipText("Escolher data no calendário");
        abrir.getAccessibleContext().setAccessibleName("Abrir calendário");
        add(campo, BorderLayout.CENTER);
        add(abrir, BorderLayout.EAST);

        JPanel cabecalho = new JPanel(new BorderLayout(5, 0));
        JButton anterior = Estilo.botao("‹");
        JButton proximo = Estilo.botao("›");
        anterior.setToolTipText("Mês anterior");
        proximo.setToolTipText("Próximo mês");

        JPanel seletores = new JPanel(new BorderLayout(4, 0));
        seletores.add(meses, BorderLayout.CENTER);
        seletores.add(ano, BorderLayout.EAST);
        ano.setEditor(new JSpinner.NumberEditor(ano, "#"));

        cabecalho.add(anterior, BorderLayout.WEST);
        cabecalho.add(seletores, BorderLayout.CENTER);
        cabecalho.add(proximo, BorderLayout.EAST);

        calendario.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        calendario.add(cabecalho, BorderLayout.NORTH);
        calendario.add(dias, BorderLayout.CENTER);

        JButton hoje = Estilo.botao("Hoje");
        hoje.addActionListener(evento -> selecionar(LocalDate.now()));
        calendario.add(hoje, BorderLayout.SOUTH);
        popup.add(calendario);

        anterior.addActionListener(evento -> {
            if (exibido.getYear() > 1 || exibido.getMonthValue() > 1) {
                exibido = exibido.minusMonths(1);
                atualizar();
            }
        });
        proximo.addActionListener(evento -> {
            if (exibido.getYear() < 9999 || exibido.getMonthValue() < 12) {
                exibido = exibido.plusMonths(1);
                atualizar();
            }
        });

        meses.addActionListener(evento -> alterarMes());
        ano.addChangeListener(evento -> alterarMes());
        abrir.addActionListener(evento -> abrirCalendario());
        atualizar();
    }

    private void abrirCalendario() {
        try {
            exibido = YearMonth.from(LocalDate.parse(campo.getText(), FORMATO));
        } catch (RuntimeException dataInvalida) {
            exibido = YearMonth.now();
        }
        atualizar();
        popup.show(this, 0, getHeight());
    }

    private void alterarMes() {
        // Alteracoes feitas pelo proprio metodo atualizar() nao devem gerar recursao.
        if (!ajustando) {
            exibido = YearMonth.of((int) ano.getValue(), meses.getSelectedIndex() + 1);
            atualizar();
        }
    }

    private void selecionar(LocalDate data) {
        campo.setText(data.format(FORMATO));
        popup.setVisible(false);
        campo.requestFocusInWindow();
    }

    private void atualizar() {
        ajustando = true;
        meses.setSelectedIndex(exibido.getMonthValue() - 1);
        ano.setValue(exibido.getYear());
        ajustando = false;

        dias.removeAll();
        for (String dia : DIAS_SEMANA) {
            dias.add(new JLabel(dia, SwingConstants.CENTER));
        }

        LocalDate primeiro = exibido.atDay(1);
        // DayOfWeek usa 1=segunda; na tabela o domingo deve ser a primeira coluna.
        int deslocamento = primeiro.getDayOfWeek().getValue() % 7;

        LocalDate selecionada = null;
        try {
            selecionada = LocalDate.parse(campo.getText(), FORMATO);
        } catch (RuntimeException dataInvalida) {
            // O campo pode estar vazio enquanto o usuario digita; nenhuma data selecionada.
        }

        for (int i = 0; i < 42; i++) {
            LocalDate data = primeiro.minusDays(deslocamento).plusDays(i);
            JButton botao = data.equals(selecionada)
                ? Estilo.botaoPrincipal(String.valueOf(data.getDayOfMonth()))
                : Estilo.botao(String.valueOf(data.getDayOfMonth()));

            botao.putClientProperty("FlatLaf.style",
                botao.getClientProperty("FlatLaf.style") + "; margin: 5,5,5,5");
            botao.setPreferredSize(new Dimension(36, 32));
            botao.setEnabled(!data.isBefore(LocalDate.now()));
            if (data.getMonth() != exibido.getMonth() && botao.isEnabled()) {
                botao.setForeground(Estilo.TEXTO_SUAVE);
            }
            botao.setToolTipText(data.format(FORMATO));
            botao.addActionListener(evento -> selecionar(data));
            dias.add(botao);
        }
        calendario.revalidate();
        calendario.repaint();
    }
}
