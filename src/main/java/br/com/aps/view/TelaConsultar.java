package br.com.aps.view;
import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import java.awt.*;
import javax.swing.*;
/** Consulta todos os empréstimos, incluindo devolvidos. Pessoas são consultadas em Cadastros. */
public class TelaConsultar extends JPanel {
    private final JTextField busca=new JTextField();
    private final JComboBox<String> filtro=new JComboBox<>(new String[]{"Todos","Em uso","Atrasado","Devolvido"});
    private final JTable tabela=Estilo.criarTabela(new String[]{"Equipamento","Pessoa","Retirada","Devolução prevista","Situação"});
    private final JLabel resultado=new JLabel(" ");
    public TelaConsultar(){
        super(new BorderLayout(0,18));setOpaque(false);
        busca.putClientProperty("JTextField.placeholderText","Digite para filtrar a tabela");
        busca.putClientProperty("JTextField.leadingIcon",new IconeAcao("busca",Estilo.TEXTO_SUAVE));
        JPanel topo=Estilo.cartao();JPanel linha=new JPanel(new BorderLayout(16,0));linha.setOpaque(false);
        JPanel campos=new JPanel(new GridLayout(1,2,16,0));campos.setOpaque(false);
        campos.add(Estilo.campo("Buscar",busca));campos.add(Estilo.campo("Situação",filtro));
        JButton pesquisar=Estilo.botaoPrincipal("Pesquisar");linha.add(campos,BorderLayout.CENTER);linha.add(Estilo.campo(" ",pesquisar),BorderLayout.EAST);
        topo.add(linha);add(topo,BorderLayout.NORTH);
        JPanel lista=Estilo.cartao();lista.add(Estilo.rolagem(tabela));lista.add(resultado,BorderLayout.SOUTH);add(lista);
        Estilo.larguras(tabela,340,220,160,190,140);Estilo.colorirSituacao(tabela,4);
        pesquisar.addActionListener(e->pesquisar());busca.addActionListener(e->pesquisar());filtro.addActionListener(e->pesquisar());
        pesquisar();
    }
    private void pesquisar(){try{
        Estilo.modelo(tabela).setRowCount(0);String termo=busca.getText().trim().toLowerCase(java.util.Locale.ROOT);
        for(var d:Aplicacao.emprestimos().listarDetalhado()){
            String situacao=Regras.situacao(d.emprestimo());if(situacao.equals("Aberto"))situacao="Em uso";
            if(!filtro.getSelectedItem().equals("Todos")&&!filtro.getSelectedItem().equals(situacao))continue;
            String[] linha={d.equipamentoNome()+" ("+d.equipamentoPatrimonio()+")",d.usuarioNome(),Estilo.dia(d.emprestimo().getDataRetirada()),Estilo.dia(d.emprestimo().getDataPrevista()),situacao};
            if(String.join(" ",linha).toLowerCase(java.util.Locale.ROOT).contains(termo))Estilo.modelo(tabela).addRow(linha);
        }resultado.setText(tabela.getRowCount()+" resultado(s) encontrado(s)");
    }catch(RuntimeException erro){Mensagens.erro(this,erro);}}
}
