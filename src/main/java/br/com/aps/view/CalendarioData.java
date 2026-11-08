package br.com.aps.view;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;
import javax.swing.*;

/** Campo editável com calendário real: dias, meses e anos. */
public final class CalendarioData extends JPanel {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final JTextField campo;
    private final JPopupMenu popup = new JPopupMenu();
    private final JPanel calendario = new JPanel(new BorderLayout(6, 8));
    private final JPanel dias = new JPanel(new GridLayout(0,7,3,3));
    private final JComboBox<String> meses = new JComboBox<>(new String[]{"Janeiro","Fevereiro","Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"});
    private final JSpinner ano = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 1, 9999, 1));
    private YearMonth exibido = YearMonth.now();
    private boolean ajustando;
    public CalendarioData(JTextField campo) {
        super(new BorderLayout(4,0)); this.campo=campo; setOpaque(false);
        JButton abrir = Estilo.botao(""); abrir.setIcon(new IconeAcao("calendario",Estilo.TEXTO_SUAVE));
        abrir.setToolTipText("Escolher data no calendário"); abrir.getAccessibleContext().setAccessibleName("Abrir calendário");
        add(campo,BorderLayout.CENTER); add(abrir,BorderLayout.EAST);
        JPanel cabecalho = new JPanel(new BorderLayout(5,0));
        JButton anterior = Estilo.botao("‹"), proximo = Estilo.botao("›");
        anterior.setToolTipText("Mês anterior");proximo.setToolTipText("Próximo mês");
        JPanel seletores = new JPanel(new BorderLayout(4,0)); seletores.add(meses,BorderLayout.CENTER);seletores.add(ano,BorderLayout.EAST);
        ano.setEditor(new JSpinner.NumberEditor(ano,"#"));
        cabecalho.add(anterior,BorderLayout.WEST);cabecalho.add(seletores,BorderLayout.CENTER);cabecalho.add(proximo,BorderLayout.EAST);
        calendario.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        calendario.add(cabecalho,BorderLayout.NORTH);calendario.add(dias,BorderLayout.CENTER);
        JButton hoje=Estilo.botao("Hoje");hoje.addActionListener(e->selecionar(LocalDate.now()));calendario.add(hoje,BorderLayout.SOUTH);
        popup.add(calendario);
        anterior.addActionListener(e->{if(exibido.getYear()>1 || exibido.getMonthValue()>1){exibido=exibido.minusMonths(1);atualizar();}});
        proximo.addActionListener(e->{if(exibido.getYear()<9999 || exibido.getMonthValue()<12){exibido=exibido.plusMonths(1);atualizar();}});
        meses.addActionListener(e->alterarMes());ano.addChangeListener(e->alterarMes());
        abrir.addActionListener(e->{ try{exibido=YearMonth.from(LocalDate.parse(campo.getText(),FORMATO));}catch(RuntimeException ignorado){exibido=YearMonth.now();}atualizar();popup.show(this,0,getHeight());});
        atualizar();
    }
    private void alterarMes(){if(!ajustando){exibido=YearMonth.of((int)ano.getValue(),meses.getSelectedIndex()+1);atualizar();}}
    private void selecionar(LocalDate data){campo.setText(data.format(FORMATO));popup.setVisible(false);campo.requestFocusInWindow();}
    private void atualizar(){
        ajustando=true;meses.setSelectedIndex(exibido.getMonthValue()-1);ano.setValue(exibido.getYear());ajustando=false;
        dias.removeAll();for(String dia:new String[]{"D","S","T","Q","Q","S","S"})dias.add(new JLabel(dia,SwingConstants.CENTER));
        LocalDate primeiro=exibido.atDay(1);int deslocamento=primeiro.getDayOfWeek().getValue()%7;
        LocalDate selecionada=null;try{selecionada=LocalDate.parse(campo.getText(),FORMATO);}catch(RuntimeException ignorado){}
        for(int i=0;i<42;i++){
            LocalDate data=primeiro.minusDays(deslocamento).plusDays(i);
            JButton b=data.equals(selecionada)?Estilo.botaoPrincipal(String.valueOf(data.getDayOfMonth())):Estilo.botao(String.valueOf(data.getDayOfMonth()));
            b.putClientProperty("FlatLaf.style", (String)b.getClientProperty("FlatLaf.style")+"; margin: 5,5,5,5");
            b.setPreferredSize(new Dimension(36,32));
            b.setEnabled(!data.isBefore(LocalDate.now()));
            if(data.getMonth()!=exibido.getMonth() && b.isEnabled())b.setForeground(Estilo.TEXTO_SUAVE);
            b.setToolTipText(data.format(FORMATO));b.addActionListener(e->selecionar(data));dias.add(b);
        }
        calendario.revalidate();calendario.repaint();
    }
}
