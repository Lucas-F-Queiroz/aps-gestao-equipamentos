package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.DevolucaoDetalhe;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Devolucao (regra 5 do Lucas): o emprestimo vira DEVOLVIDO, a data de devolucao e preenchida e o
 * equipamento volta para DISPONIVEL. A pessoa informa COMO o equipamento voltou (Novo, Bom,
 * Regular ou Danificado): esse e o estado do equipamento e aparece na tela Relatorios.
 */
public class TelaDevolucao extends JPanel {
    private final JComboBox<String> comboEstado = new JComboBox<>(Regras.rotulos(Regras.ESTADOS));
    private final JTextField campoObservacao = new JTextField();
    private final JTextField campoResponsavel = new JTextField();
    private final JTable tabelaAbertos = Estilo.criarTabela(
        new String[] {"ID", "Pessoa", "Equipamento", "Retirada", "Devolução prevista", "Situação"});
    private final JTable tabelaRecentes = Estilo.criarTabela(
        new String[] {"Data e hora", "Pessoa", "Equipamento", "Estado", "Observações", "Registrado por"});
    private final ArrayList<EmprestimoDetalhe> abertos = new ArrayList<>(); // mesma ordem das linhas da tabela

    public TelaDevolucao() {
        super(new GridLayout(2, 1, 0, 14)); // dois cartoes, um em cima do outro
        setOpaque(false);

        comboEstado.setSelectedItem("Bom");
        campoObservacao.putClientProperty("JTextField.placeholderText",
            "Ex.: arranhão na tampa (opcional, até " + Regras.OBSERVACAO_MAX + " letras)");
        campoResponsavel.putClientProperty("JTextField.placeholderText", "Nome de quem conferiu a devolução");

        // ---------- em cima: o que ainda esta emprestado + como ele voltou
        JPanel cartaoAbertos = Estilo.cartao();
        cartaoAbertos.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18));
        cartaoAbertos.add(Estilo.subtitulo("Empréstimos em aberto (selecione um e informe o estado)"), BorderLayout.NORTH);
        Estilo.colorirSituacao(tabelaAbertos, 5);
        Estilo.larguras(tabelaAbertos, 50, 150, 340, 140, 140, 110);
        cartaoAbertos.add(Estilo.rolagem(tabelaAbertos), BorderLayout.CENTER);

        JButton devolver = Estilo.botaoPrincipal("Registrar devolução");
        JPanel formulario = new JPanel(new BorderLayout(12, 0));
        formulario.setOpaque(false);
        JPanel identificacao = new JPanel(new GridLayout(1, 2, 12, 0));
        identificacao.setOpaque(false);
        identificacao.setPreferredSize(new java.awt.Dimension(390, 0));
        identificacao.add(Estilo.campo("Estado do equipamento", comboEstado));
        identificacao.add(Estilo.campo("Responsável pelo registro", campoResponsavel));
        formulario.add(identificacao, BorderLayout.WEST);
        formulario.add(Estilo.campo("Observações", campoObservacao), BorderLayout.CENTER);
        formulario.add(Estilo.campo(" ", Estilo.linhaBotoes(devolver)), BorderLayout.EAST);
        cartaoAbertos.add(formulario, BorderLayout.SOUTH);
        add(cartaoAbertos);

        // ---------- embaixo: devolucoes feitas recentemente
        JPanel cartaoRecentes = Estilo.cartao();
        cartaoRecentes.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18));
        cartaoRecentes.add(Estilo.subtitulo("Devoluções recentes"), BorderLayout.NORTH);
        Estilo.larguras(tabelaRecentes, 170, 140, 255, 100, 245, 150);
        Estilo.colorirSituacao(tabelaRecentes, 3);
        cartaoRecentes.add(Estilo.rolagem(tabelaRecentes), BorderLayout.CENTER);
        add(cartaoRecentes);

        devolver.addActionListener(e -> devolver());
        ampliar(this);
        tabelaAbertos.setRowHeight(34);
        tabelaRecentes.setRowHeight(34);
        tabelaAbertos.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        tabelaRecentes.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        devolver.putClientProperty("FlatLaf.style",
            (String) devolver.getClientProperty("FlatLaf.style") + "; margin: 7,16,7,16");
        atualizar();
    }

    /** A Devolução usa uma escala intermediária entre a versão original e a compacta. */
    private void ampliar(java.awt.Component componente) {
        if (componente.getFont() != null) {
            componente.setFont(componente.getFont().deriveFont(componente.getFont().getSize2D() + 1f));
        }
        if (componente instanceof java.awt.Container conteiner) {
            for (java.awt.Component filho : conteiner.getComponents()) ampliar(filho);
        }
    }

    private void atualizar() {
        try {
            abertos.clear();
            Estilo.modelo(tabelaAbertos).setRowCount(0);
            for (EmprestimoDetalhe d : Aplicacao.emprestimos().abertos()) {
                Emprestimo m = d.emprestimo();
                abertos.add(d);
                Estilo.modelo(tabelaAbertos).addRow(new String[] {String.valueOf(m.getId()), d.usuarioNome(),
                    d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")", Estilo.dataHora(m.getDataRetirada()),
                    Estilo.dia(m.getDataPrevista()), Regras.situacao(m)});
            }
            Estilo.modelo(tabelaRecentes).setRowCount(0);
            for (DevolucaoDetalhe d : Aplicacao.emprestimos().devolucoes()) {
                Estilo.modelo(tabelaRecentes).addRow(new String[] {Estilo.dataHoraSegundos(d.dataDevolucao()),
                    d.usuarioNome(), d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")",
                    Regras.rotulo(d.estado()), texto(d.observacao()), texto(d.registradoPor())});
            }
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void devolver() {
        try {
            Mensagens.info(this, registrarDados());
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
            atualizar();
        }
    }

    /** Operação separada da mensagem visual para permitir teste automatizado do fluxo correto. */
    String registrarDados() {
        int linha = tabelaAbertos.getSelectedRow();
        if (linha < 0 || linha >= abertos.size()) {
            throw new br.com.aps.controller.RegraNegocioException(
                "Selecione na tabela o empréstimo a devolver.");
        }
        EmprestimoDetalhe escolhido = abertos.get(linha);
        String estado = Regras.codigo((String) comboEstado.getSelectedItem());
        Aplicacao.emprestimos().devolver(escolhido.emprestimo().getId(), estado,
            campoObservacao.getText(), campoResponsavel.getText());
        campoObservacao.setText("");
        campoResponsavel.setText("");
        comboEstado.setSelectedItem("Bom");

        String mensagem = "Devolução registrada. O equipamento voltou a ficar Disponível.";
        if (Equipamento.ESTADO_DANIFICADO.equals(estado)) {
            boolean manutencao = Mensagens.confirmar(this,
                "O equipamento voltou DANIFICADO.\nDeseja marcá-lo como INDISPONIVEL (manutenção)?");
            if (manutencao) {
                Aplicacao.equipamentos().alterarStatus(
                    escolhido.emprestimo().getEquipamentoId(), Equipamento.STATUS_INDISPONIVEL);
                mensagem = "Devolução registrada. O equipamento foi marcado como Indisponível.";
            }
        }
        atualizar();
        return mensagem;
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "Não informado" : valor;
    }
}
