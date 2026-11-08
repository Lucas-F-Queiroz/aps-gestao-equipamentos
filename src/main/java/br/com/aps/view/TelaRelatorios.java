package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.DevolucaoDetalhe;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Consulta e exportação das devoluções já registradas. Não encerra empréstimos. */
public class TelaRelatorios extends JPanel {
    private final JTextField busca = new JTextField();
    private final JComboBox<String> filtroEstado = new JComboBox<>(
        new String[] {"Todos", "Novo", "Bom", "Regular", "Danificado"});
    private final JTable tabela = Estilo.criarTabela(new String[] {
        "Data da devolução", "Pessoa", "Equipamento", "Estado do produto",
        "Descrição", "Registrado por", "Ações"});
    private final JLabel resultado = new JLabel(" ");
    private List<DevolucaoDetalhe> relatorios = List.of();
    private final List<DevolucaoDetalhe> exibidos = new ArrayList<>();

    public TelaRelatorios() {
        super(new BorderLayout(0, 12));
        setOpaque(false);

        JPanel orientacao = Estilo.cartao();
        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 4));
        titulos.setOpaque(false);
        titulos.add(Estilo.subtitulo("Relatórios de devolução"));
        JLabel explicacao = new JLabel(
            "Consulte e baixe as devoluções registradas. Novas devoluções são feitas somente na área Devolução.");
        explicacao.setForeground(Estilo.TEXTO_SUAVE);
        titulos.add(explicacao);
        orientacao.add(titulos, BorderLayout.CENTER);
        add(orientacao, BorderLayout.NORTH);

        JPanel lista = Estilo.cartao();
        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);
        busca.putClientProperty("JTextField.placeholderText", "Buscar por pessoa, equipamento, descrição ou responsável");
        busca.putClientProperty("JTextField.leadingIcon", new IconeAcao("busca", Estilo.TEXTO_SUAVE));
        JPanel filtros = new JPanel(new GridLayout(1, 2, 12, 0));
        filtros.setOpaque(false);
        filtros.add(Estilo.campo("Buscar nos relatórios", busca));
        filtros.add(Estilo.campo("Estado do produto", filtroEstado));
        JButton atualizar = Estilo.botao("Atualizar");
        JButton baixarTodos = Estilo.botaoPrincipal("Baixar resultados");
        baixarTodos.setIcon(new IconeAcao("download", java.awt.Color.WHITE));
        topo.add(filtros, BorderLayout.CENTER);
        topo.add(Estilo.campo(" ", Estilo.linhaBotoes(atualizar, baixarTodos)), BorderLayout.EAST);
        lista.add(topo, BorderLayout.NORTH);

        Estilo.larguras(tabela, 145, 155, 220, 125, 260, 150, 65);
        Estilo.colorirSituacao(tabela, 3);
        new AcaoTabela(tabela, 6, "download", "Baixar este relatório em CSV", this::baixar);
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        resultado.setForeground(Estilo.TEXTO_SUAVE);
        lista.add(resultado, BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);

        atualizar.addActionListener(e -> atualizar());
        baixarTodos.addActionListener(e -> baixar(-1));
        busca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });
        filtroEstado.addActionListener(e -> filtrar());
        atualizar();
    }

    private void atualizar() {
        try {
            relatorios = Aplicacao.emprestimos().devolucoes();
            filtrar();
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void filtrar() {
        if (relatorios == null) return;
        String termo = busca.getText().trim().toLowerCase(Locale.ROOT);
        String estado = filtroEstado.getSelectedItem() == null ? "Todos" : filtroEstado.getSelectedItem().toString();
        exibidos.clear();
        Estilo.modelo(tabela).setRowCount(0);
        for (DevolucaoDetalhe d : relatorios) {
            String estadoTela = Regras.rotulo(d.estado());
            String conjunto = String.join(" ", texto(d.usuarioNome()), texto(d.equipamentoNome()),
                texto(d.equipamentoPatrimonio()), texto(d.observacao()), texto(d.registradoPor()))
                .toLowerCase(Locale.ROOT);
            if (!termo.isEmpty() && !conjunto.contains(termo)) continue;
            if (!"Todos".equals(estado) && !estado.equals(estadoTela)) continue;
            exibidos.add(d);
            Estilo.modelo(tabela).addRow(new Object[] {Estilo.dataHoraSegundos(d.dataDevolucao()),
                d.usuarioNome(), d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")",
                estadoTela, texto(d.observacao()), texto(d.registradoPor()), ""});
        }
        resultado.setText(exibidos.size() + " relatório(s) encontrado(s). Os dados permanecem salvos no MySQL.");
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "Não informado" : valor;
    }

    private void baixar(int linha) {
        if (linha >= exibidos.size()) return;
        String nome = linha < 0 ? "relatorios-devolucao.csv"
            : "relatorio-devolucao-" + exibidos.get(linha).emprestimoId() + ".csv";
        JFileChooser escolha = new JFileChooser();
        escolha.setSelectedFile(new java.io.File(nome));
        if (escolha.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path destino = escolha.getSelectedFile().toPath();
        if (Files.exists(destino) && !Mensagens.confirmar(this, "Substituir o arquivo existente?")) return;
        try {
            exportar(linha, destino);
            Mensagens.info(this, "Relatório salvo em " + destino);
        } catch (java.io.IOException erro) {
            Mensagens.aviso(this, "Não foi possível salvar: " + erro.getMessage());
        }
    }

    void exportar(int linha, Path destino) throws java.io.IOException {
        DefaultTableModel dados = new DefaultTableModel(new String[] {
            "Empréstimo", "Data da devolução", "Pessoa", "Equipamento", "Patrimônio",
            "Estado", "Descrição", "Registrado por"}, 0);
        List<DevolucaoDetalhe> selecionados = linha < 0 ? exibidos : List.of(exibidos.get(linha));
        for (DevolucaoDetalhe d : selecionados) {
            dados.addRow(new Object[] {d.emprestimoId(), Estilo.dataHoraSegundos(d.dataDevolucao()),
                d.usuarioNome(), d.equipamentoNome(), d.equipamentoPatrimonio(), Regras.rotulo(d.estado()),
                texto(d.observacao()), texto(d.registradoPor())});
        }
        ExportadorRelatorio.salvar(dados, destino);
    }
}
