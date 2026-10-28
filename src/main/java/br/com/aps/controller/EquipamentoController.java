package br.com.aps.controller;

import br.com.aps.dao.EquipamentoDAO;
import br.com.aps.model.Equipamento;
import java.sql.SQLException;
import java.util.List;

/** Valida cadastro e impede troca concorrente de status durante emprestimos. */
public class EquipamentoController {
    private final EquipamentoDAO dao;

    public EquipamentoController() { this(new EquipamentoDAO()); }
    public EquipamentoController(EquipamentoDAO dao) { this.dao = dao; }

    public List<Equipamento> listar() {
        try { return dao.listar(); }
        catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public List<Equipamento> listarDisponiveis() {
        try { return dao.listarPorStatus(Equipamento.STATUS_DISPONIVEL); }
        catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public Equipamento cadastrar(String patrimonio, String nome, String categoria, String estado) {
        Equipamento equipamento = new Equipamento(
            Regras.exigirTexto("patrimônio", patrimonio, Regras.PATRIMONIO_MAX),
            Regras.exigirTexto("nome", nome, Regras.NOME_EQUIPAMENTO_MAX),
            Regras.exigirTexto("categoria", categoria, Regras.CATEGORIA_MAX),
            Regras.exigirUm("estado", estado, Regras.ESTADOS));
        try { dao.inserir(equipamento); return equipamento; }
        catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public void atualizar(int id, String patrimonio, String nome, String categoria, String estado) {
        Equipamento dados = new Equipamento(id,
            Regras.exigirTexto("patrimônio", patrimonio, Regras.PATRIMONIO_MAX),
            Regras.exigirTexto("nome", nome, Regras.NOME_EQUIPAMENTO_MAX),
            Regras.exigirTexto("categoria", categoria, Regras.CATEGORIA_MAX),
            Regras.exigirUm("estado", estado, Regras.ESTADOS), Equipamento.STATUS_DISPONIVEL);
        try {
            // O DAO nunca altera status ao editar dados do cadastro.
            if (!dao.atualizar(dados)) throw new RegraNegocioException("Equipamento não encontrado.");
        } catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public void alterarStatus(int id, String novoStatus) {
        if (!Equipamento.STATUS_DISPONIVEL.equals(novoStatus)
                && !Equipamento.STATUS_INDISPONIVEL.equals(novoStatus)) {
            throw new RegraNegocioException("Use Disponível ou Indisponível.");
        }
        try {
            Equipamento equipamento = dao.buscarPorId(id);
            if (equipamento == null) throw new RegraNegocioException("Equipamento não encontrado.");
            if (equipamento.getStatus().equals(novoStatus)) return;
            if (!dao.alterarStatusDisponibilidade(id, novoStatus)) {
                throw new RegraNegocioException(
                    "Status não alterado: equipamento emprestado ou operação concorrente.");
            }
        } catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public void excluir(int id) {
        try {
            if (!dao.excluir(id)) throw new RegraNegocioException("Equipamento não encontrado.");
        } catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }
}
