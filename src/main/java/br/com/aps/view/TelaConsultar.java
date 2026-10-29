package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Situacao dos emprestimos (inclusive os devolvidos), com busca em todos os campos, filtros
 * por situacao, pessoa e equipamento e periodo da retirada. Sem comandos SQL na interface.
 */
public class TelaConsultar extends JPanel {
    private final JTable tabela = Estilo.criarTabela(new String[]{
        "ID", "Pessoa", "Equipamento", "Retirada", "Devolução prevista", "Devolvido em", "Situação"});
    private final FiltroTabela filtro = new FiltroTabela(tabela);

    public TelaConsultar() {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        JButton atualizar = Estilo.botao("Atualizar");
        filtro.adicionarFiltro("Situação", 6);
        filtro.adicionarFiltro("Pessoa", 1);
        filtro.adicionarFiltro("Equipamento", 2);
        filtro.adicionarPeriodo("Data do empréstimo", 3);
        filtro.adicionarCampo(" ", atualizar);

        Estilo.larguras(tabela, 50, 170, 320, 120, 140, 140, 110);
        Estilo.colorirSituacao(tabela, 6);

        JPanel lista = Estilo.cartao();
        lista.add(filtro, BorderLayout.NORTH);
        lista.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(lista, BorderLayout.CENTER);

        atualizar.addActionListener(e -> carregar());
        carregar();
    }

    /** Carrega os objetos devolvidos pelo Controller; nao acessa JDBC. */
    private void carregar() {
        try {
            Estilo.modelo(tabela).setRowCount(0);
            for (EmprestimoDetalhe d : Aplicacao.emprestimos().listarDetalhado()) {
                Emprestimo e = d.emprestimo();
                Estilo.modelo(tabela).addRow(new Object[]{
                    e.getId(), d.usuarioNome(),
                    d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")",
                    Estilo.dia(e.getDataRetirada()), Estilo.dia(e.getDataPrevista()),
                    Estilo.dia(e.getDataDevolucao()), Regras.situacao(e)
                });
            }
            filtro.atualizar();
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }
}
