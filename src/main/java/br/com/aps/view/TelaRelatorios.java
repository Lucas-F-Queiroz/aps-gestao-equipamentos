package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.EmprestimoDetalhe;
import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.*;

/** Historico dos emprestimos; exportacao CSV sem tabela adicional de relatorios. */
public class TelaRelatorios extends JPanel {
    private final JTextField filtro = new JTextField();
    private final JTable tabela = Estilo.criarTabela(
        new String[]{"ID", "Pessoa", "Equipamento", "Retirada", "Prevista", "Devolução", "Situação"});
    private final JLabel total = new JLabel();

    public TelaRelatorios() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        JPanel topo = Estilo.cartao();
        topo.add(Estilo.subtitulo("Relatório de empréstimos"), BorderLayout.NORTH);
        topo.add(Estilo.campo("Buscar por pessoa ou equipamento", filtro), BorderLayout.CENTER);
        JButton consultar = Estilo.botao("Consultar");
        JButton exportar = Estilo.botaoPrincipal("Exportar CSV");
        topo.add(Estilo.linhaBotoes(consultar, exportar), BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);
        JPanel lista = Estilo.cartao();
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        lista.add(total, BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);
        consultar.addActionListener(e -> carregar());
        filtro.addActionListener(e -> carregar());
        exportar.addActionListener(e -> exportar());
        carregar();
    }

    private void carregar() {
        try {
            Estilo.modelo(tabela).setRowCount(0);
            String busca = filtro.getText().trim().toLowerCase(Locale.ROOT);
            for (EmprestimoDetalhe d : Aplicacao.emprestimos().listarDetalhado()) {
                var emprestimo = d.emprestimo();
                String texto = (d.usuarioNome() + " " + d.equipamentoNome() + " "
                        + d.equipamentoPatrimonio()).toLowerCase(Locale.ROOT);
                if (!texto.contains(busca)) continue;
                Estilo.modelo(tabela).addRow(new Object[]{
                    emprestimo.getId(), d.usuarioNome(),
                    d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")",
                    Estilo.dataHora(emprestimo.getDataRetirada()),
                    Estilo.dia(emprestimo.getDataPrevista()),
                    Estilo.dataHora(emprestimo.getDataDevolucao()),
                    Regras.situacao(emprestimo)
                });
            }
            total.setText(tabela.getRowCount() + " registro(s). Nenhum dado é alterado pelo relatório.");
        } catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }

    private void exportar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("relatorio-emprestimos.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path arquivo = chooser.getSelectedFile().toPath();
        if (java.nio.file.Files.exists(arquivo) &&
            !Mensagens.confirmar(this, "Substituir arquivo existente?")) return;
        try {
            ExportadorRelatorio.salvar(tabela.getModel(), arquivo);
            Mensagens.info(this, "Relatório salvo em " + arquivo.toAbsolutePath());
        } catch (java.io.IOException erro) {
            Mensagens.aviso(this, "Falha ao exportar: " + erro.getMessage());
        }
    }
}
