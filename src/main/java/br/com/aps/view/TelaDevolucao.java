package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Devolucao: mostra os emprestimos ABERTOS, com busca e filtros (pessoa, modelo, situacao e
 * data do emprestimo). A View apenas escolhe o emprestimo: o DAO garante a devolucao atomica.
 */
public class TelaDevolucao extends JPanel {
    private final JTable tabela = Estilo.criarTabela(new String[]{
        "ID", "Pessoa", "Equipamento", "Patrimônio", "Retirada", "Devolução prevista", "Situação"});
    private final List<EmprestimoDetalhe> linhas = new ArrayList<>();
    private final FiltroTabela filtro = new FiltroTabela(tabela);

    public TelaDevolucao() {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        // ---------- cabecalho
        JPanel titulo = Estilo.cartao();
        titulo.add(Estilo.subtitulo("Registrar devolução"), BorderLayout.NORTH);
        JLabel dica = new JLabel("Encontre o empréstimo pelos filtros, selecione a linha e clique em Devolver.");
        dica.setForeground(Estilo.TEXTO_SUAVE);
        titulo.add(dica, BorderLayout.CENTER);
        JButton atualizar = Estilo.botao("Atualizar");
        titulo.add(atualizar, BorderLayout.EAST);
        add(titulo, BorderLayout.NORTH);

        // ---------- lista com busca e filtros
        filtro.adicionarFiltro("Situação", 6);
        filtro.adicionarFiltro("Pessoa", 1);
        filtro.adicionarFiltro("Modelo / equipamento", 2);
        filtro.adicionarPeriodo("Data do empréstimo", 4);
        Estilo.larguras(tabela, 50, 150, 280, 110, 150, 150, 110);
        Estilo.colorirSituacao(tabela, 6);
        JPanel lista = Estilo.cartao();
        lista.add(filtro, BorderLayout.NORTH);
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
                Emprestimo e = d.emprestimo();
                Estilo.modelo(tabela).addRow(new Object[]{
                    e.getId(), d.usuarioNome(), d.equipamentoNome(), d.equipamentoPatrimonio(),
                    Estilo.dataHora(e.getDataRetirada()), Estilo.dia(e.getDataPrevista()), Regras.situacao(e)
                });
            }
            filtro.atualizar();
        } catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }

    private void devolver() {
        // linhaSelecionada considera a ordenacao e o filtro: devolve SEMPRE o emprestimo que o usuario viu
        int i = Estilo.linhaSelecionada(tabela);
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
