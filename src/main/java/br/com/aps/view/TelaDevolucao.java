package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.EmprestimoDetalhe;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/** A View apenas escolhe o emprestimo: DAO garante devolucao atomica. */
public class TelaDevolucao extends JPanel {
    private final JTable tabela = Estilo.criarTabela(
        new String[]{"ID", "Pessoa", "Equipamento", "Retirada", "Prazo", "Situação"});
    private final List<EmprestimoDetalhe> linhas = new ArrayList<>();

    public TelaDevolucao() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        JPanel titulo = Estilo.cartao();
        titulo.add(Estilo.subtitulo("Registrar devolução"), BorderLayout.NORTH);
        titulo.add(new JLabel("Selecione um empréstimo aberto para devolver o equipamento."),
            BorderLayout.CENTER);
        JButton atualizar = Estilo.botao("Atualizar");
        titulo.add(atualizar, BorderLayout.EAST);
        add(titulo, BorderLayout.NORTH);
        JPanel lista = Estilo.cartao();
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        JButton devolver = Estilo.botaoPrincipal("Devolver selecionado");
        lista.add(Estilo.linhaBotoes(devolver), BorderLayout.SOUTH);
        add(lista, BorderLayout.CENTER);
        atualizar.addActionListener(e -> carregar());
        devolver.addActionListener(e -> devolver());
        carregar();
    }

    private void carregar() {
        try {
            linhas.clear();
            linhas.addAll(Aplicacao.emprestimos().abertos());
            Estilo.modelo(tabela).setRowCount(0);
            for (EmprestimoDetalhe d : linhas) {
                var e = d.emprestimo();
                Estilo.modelo(tabela).addRow(new Object[]{
                    e.getId(), d.usuarioNome(), d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")",
                    Estilo.dataHora(e.getDataRetirada()), Estilo.dia(e.getDataPrevista()), Regras.situacao(e)
                });
            }
        } catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }

    private void devolver() {
        int i = tabela.getSelectedRow();
        if (i < 0 || i >= linhas.size()) {
            Mensagens.aviso(this, "Selecione um empréstimo.");
            return;
        }
        EmprestimoDetalhe d = linhas.get(i);
        if (!Mensagens.confirmar(this,
                "Registrar devolução de " + d.equipamentoNome() + " por " + d.usuarioNome() + "?")) {
            return;
        }
        try {
            Aplicacao.emprestimos().devolver(d.emprestimo().getId());
            carregar();
            Mensagens.info(this, "Devolução registrada. O equipamento está disponível.");
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
            carregar();
        }
    }
}
