package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.Equipamento;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Cadastro de equipamentos (tabela "equipamento" do Lucas): patrimonio, nome, categoria e estado.
 * O status (Disponivel / Emprestado / Indisponivel) NAO e digitado aqui: equipamento novo nasce
 * Disponivel e depois muda pelo emprestimo, pela devolucao e pela tela Equipamentos.
 */
public class CadastroEquipamentos extends JPanel {
    private final JTextField campoModelo = new JTextField();
    private final JTextField campoMarca = new JTextField();
    private final JTextField campoNumeroSerie = new JTextField();
    private final JTextField campoSistemaOperacional = new JTextField();
    private final JTextField campoDepartamento = new JTextField();
    private final JTextField campoPatrimonio = new JTextField();
    private final JTextField campoNome = new JTextField();
    private final JTextField campoCategoria = new JTextField();
    private final JComboBox<String> campoEstado = new JComboBox<>(Regras.rotulos(Regras.ESTADOS));
    private final JTable tabela = Estilo.criarTabela(
        new String[] {"ID", "Nome", "Categoria", "Marca", "Modelo", "Estado", "Status", "Sistema operacional", "Número de série", "Departamento", "Ações"});
    private final ArrayList<Equipamento> equipamentos = new ArrayList<>(); // mesma ordem das linhas da tabela

    private final JTextField busca = new JTextField();
    private final JComboBox<String> categoriaFiltro = new JComboBox<>();
    private final javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> ordenador = new javax.swing.table.TableRowSorter<>(Estilo.modelo(tabela));
    public CadastroEquipamentos() { this(false); }
    public CadastroEquipamentos(boolean paginaEquipamentos) {
        super(new BorderLayout(0, 12));
        setOpaque(false);

        campoPatrimonio.putClientProperty("JTextField.placeholderText", "Ex.: EQUIP-001");
        campoNome.putClientProperty("JTextField.placeholderText", "Ex.: Notebook Dell Latitude");
        campoCategoria.putClientProperty("JTextField.placeholderText", "Ex.: Notebook");

        campoModelo.putClientProperty("JTextField.placeholderText", "Ex.: Latitude 3520");
        campoMarca.putClientProperty("JTextField.placeholderText", "Ex.: Dell");
        campoNumeroSerie.putClientProperty("JTextField.placeholderText", "Ex.: ABC123456");
        campoSistemaOperacional.putClientProperty("JTextField.placeholderText", "Ex.: Windows 11");
        campoDepartamento.putClientProperty("JTextField.placeholderText", "Ex.: TI");
        JPanel cartaoForm = Estilo.cartao();
        cartaoForm.setBorder(javax.swing.BorderFactory.createEmptyBorder(9, 16, 9, 16));
        ((BorderLayout) cartaoForm.getLayout()).setVgap(8);
        cartaoForm.add(Estilo.subtitulo("Dados do equipamento"), BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(paginaEquipamentos ? 2 : 3, paginaEquipamentos ? 5 : 3, 12, 6));
        campos.setOpaque(false);
        campos.add(Estilo.campo("ID (patrimônio único)", campoPatrimonio));
        campos.add(Estilo.campo("Nome", campoNome));
        campos.add(Estilo.campo("Categoria", campoCategoria));
        campos.add(Estilo.campo("Estado", campoEstado));
        campos.add(Estilo.campo("Modelo", campoModelo));
        campos.add(Estilo.campo("Marca", campoMarca));
        campos.add(Estilo.campo("Número de série (único)", campoNumeroSerie));
        campos.add(Estilo.campo("Sistema operacional", campoSistemaOperacional));
        campos.add(Estilo.campo("Departamento", campoDepartamento));


        JButton cadastrar = Estilo.botaoPrincipal("Cadastrar");
        JButton modificar = Estilo.botao("Modificar");
        JButton excluir = Estilo.botao("Excluir");
        JButton limpar = Estilo.botao("Limpar");

        cadastrar.setIcon(new IconeAcao("mais", java.awt.Color.WHITE));
        modificar.setIcon(new IconeAcao("editar", Estilo.BARRA_LATERAL));
        excluir.setIcon(new IconeAcao("excluir", Estilo.BARRA_LATERAL));
        limpar.setIcon(new IconeAcao("limpar", Estilo.BARRA_LATERAL));
        JLabel dica = new JLabel("Equipamento novo nasce Disponível. Quem tem histórico de empréstimos não pode ser excluído.");
        dica.setForeground(Estilo.TEXTO_SUAVE);
        dica.setFont(dica.getFont().deriveFont(12f));
        for (JButton acao : new JButton[] {cadastrar, modificar, excluir, limpar}) {
            acao.putClientProperty("FlatLaf.style", (String) acao.getClientProperty("FlatLaf.style") + "; margin: 5,16,5,16");
        }

        JPanel miolo = new JPanel(new BorderLayout(0, 8));
        miolo.setOpaque(false);
        miolo.add(campos, BorderLayout.NORTH);
        miolo.add(Estilo.linhaBotoes(cadastrar, modificar, excluir, limpar), BorderLayout.CENTER);
        miolo.add(dica, BorderLayout.SOUTH);
        cartaoForm.add(miolo, BorderLayout.CENTER);
        add(cartaoForm, BorderLayout.NORTH);

        JPanel cartaoTabela = Estilo.cartao();
        JPanel topoLista = new JPanel(new BorderLayout(0,10));topoLista.setOpaque(false);
        topoLista.add(Estilo.subtitulo("Equipamentos cadastrados"),BorderLayout.NORTH);
        busca.putClientProperty("JTextField.placeholderText","Digite para filtrar a tabela");
        busca.putClientProperty("JTextField.leadingIcon",new IconeAcao("busca",Estilo.TEXTO_SUAVE));
        JButton pesquisar=Estilo.botaoPrincipal("Pesquisar");
        JPanel filtros=new JPanel(new BorderLayout(14,0));filtros.setOpaque(false);
        JPanel camposBusca=new JPanel(new GridLayout(1,2,14,0));camposBusca.setOpaque(false);
        camposBusca.add(Estilo.campo("Buscar",busca));camposBusca.add(Estilo.campo("Categoria",categoriaFiltro));
        filtros.add(camposBusca,BorderLayout.CENTER);filtros.add(Estilo.campo(" ",pesquisar),BorderLayout.EAST);
        topoLista.add(filtros,BorderLayout.CENTER);cartaoTabela.add(topoLista,BorderLayout.NORTH);
        tabela.setRowSorter(ordenador);
        pesquisar.addActionListener(e->filtrar());busca.addActionListener(e->filtrar());categoriaFiltro.addActionListener(e->filtrar());
        busca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){filtrar();}public void removeUpdate(javax.swing.event.DocumentEvent e){filtrar();}public void changedUpdate(javax.swing.event.DocumentEvent e){filtrar();}
        });
        Estilo.colorirSituacao(tabela, 5);Estilo.colorirSituacao(tabela, 6);
        new AcaoTabela(tabela,10,"editar","Editar / alterar disponibilidade",i->acoes(i));
        Estilo.larguras(tabela,110,180,90,85,100,100,110,110,120,100,70);
        cartaoTabela.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(cartaoTabela, BorderLayout.CENTER);

        cadastrar.addActionListener(e -> cadastrar());
        modificar.addActionListener(e -> modificar());
        excluir.addActionListener(e -> excluir());
        limpar.addActionListener(e -> limpar());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormulario();
            }
        });

        atualizarTabela();
    }

    private void atualizarTabela() {
        try {
            equipamentos.clear();
            equipamentos.addAll(Aplicacao.equipamentos().listar());
            Estilo.modelo(tabela).setRowCount(0);
            Object anterior = categoriaFiltro.getSelectedItem();
            categoriaFiltro.removeAllItems();categoriaFiltro.addItem("Todos");
            equipamentos.stream().map(Equipamento::getCategoria).distinct().sorted().forEach(categoriaFiltro::addItem);
            categoriaFiltro.setSelectedItem(anterior == null ? "Todos" : anterior);
            if(categoriaFiltro.getSelectedIndex()<0)categoriaFiltro.setSelectedIndex(0);
            for (Equipamento e : equipamentos) {
                Estilo.modelo(tabela).addRow(new String[] {e.getPatrimonio(), e.getNome(), e.getCategoria(), e.getMarca(), e.getModelo(), Regras.rotulo(e.getEstado()), Regras.rotulo(e.getStatus()), e.getSistemaOperacional(), e.getNumeroSerie(), e.getDepartamento(), ""});
            }
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private Equipamento equipamentoSelecionado() {
        int linha = tabela.getSelectedRow();
        return linha < 0 || linha >= equipamentos.size() ? null : equipamentos.get(tabela.convertRowIndexToModel(linha));
    }

    private void filtrar() {
        String texto=busca.getText().trim().toLowerCase(java.util.Locale.ROOT);
        Object cat=categoriaFiltro.getSelectedItem();
        ordenador.setRowFilter(new javax.swing.RowFilter<javax.swing.table.DefaultTableModel,Integer>() {
            public boolean include(Entry<? extends javax.swing.table.DefaultTableModel,? extends Integer> e) {
                if(cat != null && !cat.equals("Todos") && !cat.equals(e.getStringValue(2)))return false;
                for(int c=0;c<e.getValueCount();c++)if(e.getStringValue(c).toLowerCase(java.util.Locale.ROOT).contains(texto))return true;
                return texto.isEmpty();
            }
        });
    }
    private void acoes(int linha) {
        Equipamento equipamento=equipamentos.get(linha);
        tabela.setRowSelectionInterval(tabela.convertRowIndexToView(linha),tabela.convertRowIndexToView(linha));
        javax.swing.JPopupMenu menu=new javax.swing.JPopupMenu();
        javax.swing.JMenuItem editar=new javax.swing.JMenuItem("Modificar cadastro");editar.addActionListener(e->preencherFormulario());menu.add(editar);
        for(String status:new String[]{Equipamento.STATUS_DISPONIVEL,Equipamento.STATUS_INDISPONIVEL}){
            javax.swing.JMenuItem item=new javax.swing.JMenuItem("Marcar como "+Regras.rotulo(status));
            item.addActionListener(ev->{try{Aplicacao.equipamentos().alterarStatus(equipamento.getId(),status);atualizarTabela();}catch(RuntimeException erro){Mensagens.erro(this,erro);}});menu.add(item);
        }
        java.awt.Rectangle celula=tabela.getCellRect(tabela.convertRowIndexToView(linha),10,true);menu.show(tabela,celula.x,celula.y+celula.height);
    }
    private void preencherFormulario() {
        Equipamento e = equipamentoSelecionado();
        if (e == null) {
            return;
        }
        campoPatrimonio.setText(e.getPatrimonio());
        campoNome.setText(e.getNome());
        campoCategoria.setText(e.getCategoria());
        campoEstado.setSelectedItem(Regras.rotulo(e.getEstado()));
        campoModelo.setText(e.getModelo());
        campoMarca.setText(e.getMarca());
        campoNumeroSerie.setText(e.getNumeroSerie());
        campoSistemaOperacional.setText(e.getSistemaOperacional());
        campoDepartamento.setText(e.getDepartamento());

    }

    private void cadastrar() {
        try {
            Aplicacao.equipamentos().cadastrar(campoPatrimonio.getText(), campoNome.getText(),
                campoCategoria.getText(), (String) campoEstado.getSelectedItem(), campoModelo.getText(), campoMarca.getText(), campoNumeroSerie.getText(), campoSistemaOperacional.getText(), campoDepartamento.getText());
            atualizarTabela();
            limpar();
            Mensagens.info(this, "Equipamento cadastrado (status Disponível).");
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void modificar() {
        Equipamento e = equipamentoSelecionado();
        if (e == null) {
            Mensagens.aviso(this, "Selecione um equipamento na tabela.");
            return;
        }
        try {
            Aplicacao.equipamentos().atualizar(e.getId(), campoPatrimonio.getText(), campoNome.getText(),
                campoCategoria.getText(), (String) campoEstado.getSelectedItem(), campoModelo.getText(), campoMarca.getText(), campoNumeroSerie.getText(), campoSistemaOperacional.getText(), campoDepartamento.getText());
            atualizarTabela();
            Mensagens.info(this, "Equipamento atualizado. (O status não muda por aqui.)");
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void excluir() {
        Equipamento e = equipamentoSelecionado();
        if (e == null) {
            Mensagens.aviso(this, "Selecione um equipamento na tabela.");
            return;
        }
        if (!Mensagens.confirmar(this, "Excluir \"" + e.getNome() + "\"?")) {
            return;
        }
        try {
            Aplicacao.equipamentos().excluir(e.getId());
            atualizarTabela();
            limpar();
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void limpar() {
        campoPatrimonio.setText("");
        campoNome.setText("");
        campoCategoria.setText("");
        campoEstado.setSelectedIndex(0);
        campoModelo.setText("");
        campoMarca.setText("");
        campoNumeroSerie.setText("");
        campoSistemaOperacional.setText("");
        campoDepartamento.setText("");

        tabela.clearSelection();
    }
}
