package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Inventário operacional. Cadastro e manutenção dos dados continuam exclusivamente em Cadastros;
 * aqui o usuário acompanha disponibilidade, conservação e quem está com cada equipamento.
 */
public class TelaEquipamentos extends JPanel {
    private final JLabel total = numero();
    private final JLabel disponiveis = numero();
    private final JLabel emprestados = numero();
    private final JLabel indisponiveis = numero();
    private final JTextField busca = new JTextField();
    private final JComboBox<String> filtroStatus = new JComboBox<>();
    private final JComboBox<String> filtroCategoria = new JComboBox<>();
    private final JComboBox<String> filtroDepartamento = new JComboBox<>();
    private final JTable tabela = Estilo.criarTabela(new String[] {
        "Patrimônio", "Equipamento", "Categoria", "Departamento", "Estado",
        "Disponibilidade", "Com quem está", "Devolução prevista", "Ações"});
    private final JLabel resultado = new JLabel(" ");
    private List<Equipamento> equipamentos = List.of();
    private final List<Equipamento> linhas = new ArrayList<>();
    private Map<Integer, EmprestimoDetalhe> emprestimosAbertos = Map.of();

    public TelaEquipamentos() {
        super(new BorderLayout(0, 12));
        setOpaque(false);

        JPanel indicadores = new JPanel(new GridLayout(1, 4, 12, 0));
        indicadores.setOpaque(false);
        indicadores.add(indicador("Total no inventário", total, Estilo.AZUL));
        indicadores.add(indicador("Disponíveis", disponiveis, Estilo.VERDE));
        indicadores.add(indicador("Emprestados", emprestados, new Color(124, 58, 237)));
        indicadores.add(indicador("Indisponíveis", indisponiveis, Estilo.LARANJA));
        add(indicadores, BorderLayout.NORTH);

        JPanel inventario = Estilo.cartao();
        JPanel cabecalho = new JPanel(new BorderLayout(12, 0));
        cabecalho.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(Estilo.subtitulo("Inventário de equipamentos"));
        JLabel ajuda = new JLabel("Acompanhe localização, conservação, disponibilidade e empréstimos em aberto.");
        ajuda.setForeground(Estilo.TEXTO_SUAVE);
        textos.add(ajuda);
        JButton atualizar = Estilo.botao("Atualizar inventário");
        atualizar.setIcon(new IconeAcao("equipamento", Estilo.AZUL));
        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(atualizar, BorderLayout.EAST);
        inventario.add(cabecalho, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 9));
        centro.setOpaque(false);
        busca.putClientProperty("JTextField.placeholderText", "Buscar por patrimônio, equipamento, pessoa ou departamento");
        busca.putClientProperty("JTextField.leadingIcon", new IconeAcao("busca", Estilo.TEXTO_SUAVE));
        JPanel filtros = new JPanel(new GridLayout(1, 4, 10, 0));
        filtros.setOpaque(false);
        filtros.add(Estilo.campo("Buscar", busca));
        filtros.add(Estilo.campo("Disponibilidade", filtroStatus));
        filtros.add(Estilo.campo("Categoria", filtroCategoria));
        filtros.add(Estilo.campo("Departamento", filtroDepartamento));
        centro.add(filtros, BorderLayout.NORTH);

        Estilo.larguras(tabela, 90, 190, 105, 105, 90, 115, 150, 120, 65);
        Estilo.colorirSituacao(tabela, 4);
        Estilo.colorirSituacao(tabela, 5);
        new AcaoTabela(tabela, 8, "equipamento", "Ver detalhes do equipamento", this::detalhes);
        centro.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        resultado.setForeground(Estilo.TEXTO_SUAVE);
        centro.add(resultado, BorderLayout.SOUTH);
        inventario.add(centro, BorderLayout.CENTER);
        add(inventario, BorderLayout.CENTER);

        atualizar.addActionListener(e -> atualizar());
        busca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });
        filtroStatus.addActionListener(e -> filtrar());
        filtroCategoria.addActionListener(e -> filtrar());
        filtroDepartamento.addActionListener(e -> filtrar());
        atualizar();
    }

    private static JLabel numero() {
        JLabel valor = new JLabel("0");
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 24f));
        return valor;
    }

    private JPanel indicador(String titulo, JLabel valor, Color cor) {
        JPanel painel = Estilo.cartao();
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, cor),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel texto = new JLabel(titulo);
        texto.setForeground(Estilo.TEXTO_SUAVE);
        painel.add(texto, BorderLayout.NORTH);
        painel.add(valor, BorderLayout.CENTER);
        return painel;
    }

    private void atualizar() {
        try {
            equipamentos = Aplicacao.equipamentos().listar();
            Map<Integer, EmprestimoDetalhe> mapa = new HashMap<>();
            for (EmprestimoDetalhe detalhe : Aplicacao.emprestimos().abertos()) {
                mapa.put(detalhe.emprestimo().getEquipamentoId(), detalhe);
            }
            emprestimosAbertos = mapa;
            total.setText(String.valueOf(equipamentos.size()));
            disponiveis.setText(String.valueOf(contar(Equipamento.STATUS_DISPONIVEL)));
            emprestados.setText(String.valueOf(contar(Equipamento.STATUS_EMPRESTADO)));
            indisponiveis.setText(String.valueOf(contar(Equipamento.STATUS_INDISPONIVEL)));
            preencherFiltros();
            filtrar();
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private long contar(String status) {
        return equipamentos.stream().filter(e -> status.equals(e.getStatus())).count();
    }

    private void preencherFiltros() {
        String statusAtual = (String) filtroStatus.getSelectedItem();
        String categoriaAtual = (String) filtroCategoria.getSelectedItem();
        String departamentoAtual = (String) filtroDepartamento.getSelectedItem();
        preencher(filtroStatus,
            new LinkedHashSet<>(List.of("Todos", "Disponível", "Emprestado", "Indisponível")), statusAtual);
        Set<String> categorias = new LinkedHashSet<>();
        Set<String> departamentos = new LinkedHashSet<>();
        categorias.add("Todos"); departamentos.add("Todos");
        equipamentos.stream().map(Equipamento::getCategoria).filter(this::temTexto).sorted().forEach(categorias::add);
        equipamentos.stream().map(Equipamento::getDepartamento).filter(this::temTexto).sorted().forEach(departamentos::add);
        preencher(filtroCategoria, categorias, categoriaAtual);
        preencher(filtroDepartamento, departamentos, departamentoAtual);
    }

    private void preencher(JComboBox<String> combo, Set<String> itens, String manter) {
        combo.removeAllItems();
        itens.forEach(combo::addItem);
        if (manter != null) combo.setSelectedItem(manter);
        if (combo.getSelectedIndex() < 0 && combo.getItemCount() > 0) combo.setSelectedIndex(0);
    }

    private boolean temTexto(String valor) { return valor != null && !valor.isBlank(); }

    private void filtrar() {
        if (equipamentos == null) return;
        String termo = busca.getText().trim().toLowerCase(Locale.ROOT);
        String status = item(filtroStatus);
        String categoria = item(filtroCategoria);
        String departamento = item(filtroDepartamento);
        linhas.clear();
        Estilo.modelo(tabela).setRowCount(0);
        for (Equipamento equipamento : equipamentos) {
            EmprestimoDetalhe aberto = emprestimosAbertos.get(equipamento.getId());
            String statusTela = Regras.rotulo(equipamento.getStatus());
            String pessoa = aberto == null ? "—" : aberto.usuarioNome();
            String prevista = aberto == null ? "—" : Estilo.dia(aberto.emprestimo().getDataPrevista());
            String tudo = String.join(" ", texto(equipamento.getPatrimonio()), texto(equipamento.getNome()),
                texto(equipamento.getCategoria()), texto(equipamento.getDepartamento()), pessoa).toLowerCase(Locale.ROOT);
            if (!termo.isEmpty() && !tudo.contains(termo)) continue;
            if (!"Todos".equals(status) && !status.equals(statusTela)) continue;
            if (!"Todos".equals(categoria) && !categoria.equals(equipamento.getCategoria())) continue;
            if (!"Todos".equals(departamento) && !departamento.equals(equipamento.getDepartamento())) continue;
            linhas.add(equipamento);
            Estilo.modelo(tabela).addRow(new Object[] {equipamento.getPatrimonio(), equipamento.getNome(),
                equipamento.getCategoria(), texto(equipamento.getDepartamento()), Regras.rotulo(equipamento.getEstado()),
                statusTela, pessoa, prevista, ""});
        }
        resultado.setText(linhas.size() + " equipamento(s) exibido(s) de " + equipamentos.size()
            + " no inventário. Cadastros e alterações são feitos na área Cadastros.");
    }

    private String item(JComboBox<String> combo) {
        return combo.getSelectedItem() == null ? "Todos" : combo.getSelectedItem().toString();
    }

    private String texto(String valor) { return temTexto(valor) ? valor : "Não informado"; }

    private void detalhes(int linha) {
        if (linha < 0 || linha >= linhas.size()) return;
        Equipamento e = linhas.get(linha);
        EmprestimoDetalhe aberto = emprestimosAbertos.get(e.getId());
        String emprestimo = aberto == null ? "Sem empréstimo em aberto"
            : "Com " + aberto.usuarioNome() + " até " + Estilo.dia(aberto.emprestimo().getDataPrevista());
        JOptionPane.showMessageDialog(this,
            "Patrimônio: " + e.getPatrimonio() + "\nNome: " + e.getNome()
            + "\nMarca / modelo: " + texto(e.getMarca()) + " / " + texto(e.getModelo())
            + "\nNúmero de série: " + texto(e.getNumeroSerie())
            + "\nDepartamento: " + texto(e.getDepartamento())
            + "\nEstado: " + Regras.rotulo(e.getEstado())
            + "\nDisponibilidade: " + Regras.rotulo(e.getStatus()) + "\n" + emprestimo,
            "Detalhes do equipamento", JOptionPane.INFORMATION_MESSAGE);
    }
}
