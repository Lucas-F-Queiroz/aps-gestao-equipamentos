package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.model.*;
import br.com.aps.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import javax.swing.*;

/** Integração dos controles visuais com um banco de desenvolvimento configurado. */
public class ViewsSmokeCheck {
    static Object campo(Object objeto,String nome)throws Exception{var f=objeto.getClass().getDeclaredField(nome);f.setAccessible(true);return f.get(objeto);}
    static void chamar(Object objeto,String nome)throws Exception{var m=objeto.getClass().getDeclaredMethod(nome);m.setAccessible(true);m.invoke(objeto);}
    static void verificar(boolean condicao,String mensagem){if(!condicao)throw new AssertionError(mensagem);System.out.println("OK: "+mensagem);}
    static void layout(Container c){if(c instanceof JScrollPane sc&&sc.getViewport().getView() instanceof JTable t)sc.setColumnHeaderView(t.getTableHeader());c.doLayout();for(var x:c.getComponents())if(x instanceof Container d)layout(d);}
    static void foto(JPanel p,String nome)throws Exception{
        String pasta=System.getProperty("smoke.shots");if(pasta==null)return;
        int w=nome.equals("calendario")?420:1280,h=nome.equals("calendario")?380:720;
        p.setSize(w,h);layout(p);var img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();g.setColor(Estilo.FUNDO);g.fillRect(0,0,w,h);p.printAll(g);g.dispose();Files.createDirectories(Path.of(pasta));javax.imageio.ImageIO.write(img,"png",Path.of(pasta,nome+".png").toFile());
    }
    public static void main(String[] args)throws Exception{
        DatabaseMigrator.migrate();String tag="VIS-"+UUID.randomUUID().toString().substring(0,8);
        var u=Aplicacao.usuarios().cadastrar("Pessoa "+tag,tag+"@teste.com","Suporte","MAT-"+tag);
        var eq=Aplicacao.equipamentos().cadastrar(tag,"Notebook "+tag,"Notebook","Bom","Latitude","Dell","SN-"+tag,"Windows 11","TI");
        var emp=Aplicacao.emprestimos().emprestar(u.getId(),eq.getId(),LocalDate.now().plusDays(7));
        try{
            SwingUtilities.invokeAndWait(()->{try{
                Estilo.instalar();
                var cad=new CadastroEquipamentos();var busca=(JTextField)campo(cad,"busca");busca.setText(tag);
                var tabela=(JTable)campo(cad,"tabela");verificar(tabela.getRowCount()==1,"Filtro de equipamento");
                tabela.setRowSelectionInterval(0,0);verificar(((JTextField)campo(cad,"campoPatrimonio")).getText().equals(tag),"Seleção filtrada preenche equipamento correto");
                var consulta=new TelaConsultar();((JTextField)campo(consulta,"busca")).setText(tag);((JComboBox<?>)campo(consulta,"filtro")).setSelectedItem("Em uso");
                verificar(((JTable)campo(consulta,"tabela")).getRowCount()==1,"Consulta inclui empréstimo em uso");
                var devolucao=new TelaDevolucao();var abertos=(JTable)campo(devolucao,"tabelaAbertos");int aberta=-1;
                for(int i=0;i<abertos.getRowCount();i++)if(abertos.getValueAt(i,2).toString().contains(tag))aberta=i;
                verificar(aberta>=0,"Devolução lista o empréstimo em aberto");abertos.setRowSelectionInterval(aberta,aberta);
                ((JComboBox<?>)campo(devolucao,"comboEstado")).setSelectedItem("Bom");
                ((JTextField)campo(devolucao,"campoObservacao")).setText("Inspeção completa "+tag);
                ((JTextField)campo(devolucao,"campoResponsavel")).setText("Operador de teste");devolucao.registrarDados();
                var registros=Aplicacao.emprestimos().devolucoes();var d=registros.stream().filter(x->x.emprestimoId()==emp.getId()).findFirst().orElseThrow();
                verificar("Operador de teste".equals(d.registradoPor()),"Devolução salva responsável no mesmo registro");
                var rel=new TelaRelatorios();var relt=(JTable)campo(rel,"tabela");int linha=-1;for(int i=0;i<relt.getRowCount();i++)if(relt.getValueAt(i,2).toString().contains(tag))linha=i;
                Path csv=Files.createTempFile("relatorio-visual-",".csv");rel.exportar(linha,csv);String conteudo=Files.readString(csv);Files.delete(csv);
                verificar(conteudo.contains("Operador de teste")&&conteudo.contains(tag)&&conteudo.split("\\r\\n").length==2,"Download individual contém apenas relatório escolhido");
                ((JComboBox<?>)campo(consulta,"filtro")).setSelectedItem("Devolvido");verificar(((JTable)campo(consulta,"tabela")).getRowCount()==1,"Consulta inclui devolvidos");
                var texto=new JTextField();var calendario=new CalendarioData(texto);var ano=(JSpinner)campo(calendario,"ano");var meses=(JComboBox<?>)campo(calendario,"meses");ano.setValue(2028);meses.setSelectedIndex(1);
                JPanel dias=(JPanel)campo(calendario,"dias");boolean achou=false;
                for(var c:dias.getComponents())if(c instanceof JButton b&&"29/02/2028".equals(b.getToolTipText())){b.doClick();achou=true;}
                verificar(achou&&"29/02/2028".equals(texto.getText()),"Calendário seleciona 29 de fevereiro em ano bissexto");
                ano.setValue(2027);dias=(JPanel)campo(calendario,"dias");boolean invalida=false;
                for(var c:dias.getComponents())if(c instanceof JButton b&&"29/02/2027".equals(b.getToolTipText()))invalida=true;
                verificar(!invalida,"Calendário não oferece 29/02 em ano comum");
                texto.setText(LocalDate.now().plusDays(7).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));ano.setValue(LocalDate.now().plusDays(7).getYear());meses.setSelectedIndex(LocalDate.now().plusDays(7).getMonthValue()-1);
                foto(new TelaCadastros(),"cadastros");foto(new TelaEquipamentos(),"equipamentos");foto(consulta,"consulta");foto(devolucao,"devolucao");foto(rel,"relatorios");foto(new TelaEmprestimos(),"emprestimos");
                JPanel cal=(JPanel)campo(calendario,"calendario");foto(cal,"calendario");
            }catch(Exception e){throw new RuntimeException(e);}});
            System.out.println("TELAS E CALENDARIO: OK");
        }finally{
            try(var c=ConnectionFactory.getConnection()){
                for(String sql:new String[]{"DELETE FROM devolucao WHERE emprestimo_id=?","DELETE FROM emprestimo WHERE id=?"})try(var p=c.prepareStatement(sql)){p.setInt(1,emp.getId());p.executeUpdate();}
                try(var p=c.prepareStatement("DELETE FROM equipamento WHERE id=?")){p.setInt(1,eq.getId());p.executeUpdate();}
                try(var p=c.prepareStatement("DELETE FROM usuario WHERE id=?")){p.setInt(1,u.getId());p.executeUpdate();}
            }
        }
        System.exit(0);
    }
}
