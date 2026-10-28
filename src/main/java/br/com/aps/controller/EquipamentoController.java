package br.com.aps.controller;

import br.com.aps.dao.EmprestimoDAO;
import br.com.aps.dao.EquipamentoDAO;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.Equipamento;

import java.sql.SQLException;
import java.util.List;

/**
 * Regras e validacoes de equipamentos (tabela equipamento do Lucas):
 * patrimonio ate 50 e unico, nome ate 100, categoria ate 50, estado NOVO/BOM/REGULAR/DANIFICADO.
 * Equipamento novo nasce DISPONIVEL. EMPRESTADO so muda por emprestimo e devolucao, nunca a mao.
 * Quem tem historico de emprestimos nao e excluido.
 */
public class EquipamentoController {
    private final EquipamentoDAO equipamentoDAO;
    private final EmprestimoDAO emprestimoDAO;

    public EquipamentoController() {
        this(new EquipamentoDAO(), new EmprestimoDAO());
    }

    public EquipamentoController(EquipamentoDAO equipamentoDAO, EmprestimoDAO emprestimoDAO) {
        this.equipamentoDAO = equipamentoDAO;
        this.emprestimoDAO = emprestimoDAO;
    }

    public List<Equipamento> listar() {
        try {
            return equipamentoDAO.listar();
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    /** Regra 2 do Lucas: so equipamentos DISPONIVEL podem ser emprestados. */
    public List<Equipamento> listarDisponiveis() {
        try {
            return equipamentoDAO.listarPorStatus(Equipamento.STATUS_DISPONIVEL);
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    public Equipamento cadastrar(String patrimonio, String nome, String categoria, String estado) {
        return cadastrar(patrimonio, nome, categoria, estado, nome, "Não informado", patrimonio, null, null);
    }

    public Equipamento cadastrar(String patrimonio, String nome, String categoria, String estado,
            String modelo, String marca, String numeroSerie, String sistemaOperacional, String departamento) {
        String patrimonioValido = Regras.exigirTexto("patrimônio", patrimonio, Regras.PATRIMONIO_MAX);
        String nomeValido = Regras.exigirTexto("nome", nome, Regras.NOME_EQUIPAMENTO_MAX);
        String categoriaValida = Regras.exigirTexto("categoria", categoria, Regras.CATEGORIA_MAX);
        String estadoValido = Regras.exigirUm("estado", estado, Regras.ESTADOS);
        verificarPatrimonioRepetido(patrimonioValido, 0);

        Equipamento equipamento = new Equipamento(patrimonioValido, nomeValido, categoriaValida, estadoValido);
        completar(equipamento, modelo, marca, numeroSerie, sistemaOperacional, departamento);
        try {
            equipamentoDAO.inserir(equipamento);
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
        return equipamento;
    }

    /** Altera os dados do cadastro. O status NAO muda por aqui. */
    public void atualizar(int id, String patrimonio, String nome, String categoria, String estado) {
        try {
            Equipamento atual = equipamentoDAO.buscarPorId(id);
            if (atual == null) throw new RegraNegocioException("Equipamento não encontrado.");
            atualizar(id, patrimonio, nome, categoria, estado, atual.getModelo(), atual.getMarca(),
                atual.getNumeroSerie(), atual.getSistemaOperacional(), atual.getDepartamento());
        } catch (SQLException erro) { throw TradutorErros.traduzir(erro); }
    }

    public void atualizar(int id, String patrimonio, String nome, String categoria, String estado,
            String modelo, String marca, String numeroSerie, String sistemaOperacional, String departamento) {
        String patrimonioValido = Regras.exigirTexto("patrimônio", patrimonio, Regras.PATRIMONIO_MAX);
        String nomeValido = Regras.exigirTexto("nome", nome, Regras.NOME_EQUIPAMENTO_MAX);
        String categoriaValida = Regras.exigirTexto("categoria", categoria, Regras.CATEGORIA_MAX);
        String estadoValido = Regras.exigirUm("estado", estado, Regras.ESTADOS);
        verificarPatrimonioRepetido(patrimonioValido, id);

        try {
            Equipamento atual = equipamentoDAO.buscarPorId(id);
            if (atual == null) {
                throw new RegraNegocioException("Equipamento não encontrado.");
            }
            // mantem o status que ja esta gravado
            Equipamento novo = new Equipamento(
                id, patrimonioValido, nomeValido, categoriaValida, estadoValido, atual.getStatus());
            completar(novo, modelo, marca, numeroSerie, sistemaOperacional, departamento);
            if (!equipamentoDAO.atualizar(novo)) {
                throw new RegraNegocioException("Equipamento não encontrado.");
            }
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    /** Marca como INDISPONIVEL (manutencao ou baixa) ou libera de novo (DISPONIVEL). */
    public void alterarStatus(int id, String novoStatus) {
        if (!Equipamento.STATUS_DISPONIVEL.equals(novoStatus)
                && !Equipamento.STATUS_INDISPONIVEL.equals(novoStatus)) {
            throw new RegraNegocioException("Só é possível escolher Disponível ou Indisponível.");
        }
        try {
            Equipamento equipamento = equipamentoDAO.buscarPorId(id);
            if (equipamento == null) {
                throw new RegraNegocioException("Equipamento não encontrado.");
            }
            if (Equipamento.STATUS_EMPRESTADO.equals(equipamento.getStatus()) || temEmprestimoAberto(id)) {
                throw new RegraNegocioException(
                    "Este equipamento está emprestado. O status só muda quando a devolução for registrada.");
            }
            equipamento.setStatus(novoStatus);
            if (!equipamentoDAO.atualizar(equipamento)) {
                throw new RegraNegocioException("Equipamento não encontrado.");
            }
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    public void excluir(int id) {
        try {
            if (!emprestimoDAO.listarPorEquipamento(id).isEmpty()) {
                throw new RegraNegocioException(
                    "Este equipamento possui histórico de empréstimos e não pode ser excluído.");
            }
            if (!equipamentoDAO.excluir(id)) {
                throw new RegraNegocioException("Equipamento não encontrado.");
            }
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    private boolean temEmprestimoAberto(int equipamentoId) throws SQLException {
        for (Emprestimo emprestimo : emprestimoDAO.listarPorEquipamento(equipamentoId)) {
            if (Emprestimo.STATUS_ABERTO.equals(emprestimo.getStatus())) {
                return true;
            }
        }
        return false;
    }

    // O banco tambem recusa patrimonio repetido (UNIQUE); aqui a mensagem sai antes e ignora maiusculas.
    private void verificarPatrimonioRepetido(String patrimonio, int idDoEquipamentoAtual) {
        try {
            for (Equipamento existente : equipamentoDAO.listar()) {
                if (existente.getId() != idDoEquipamentoAtual
                        && existente.getPatrimonio().equalsIgnoreCase(patrimonio)) {
                    throw new RegraNegocioException("Já existe um equipamento com esse patrimônio.");
                }
            }
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }
    private static void completar(Equipamento e, String modelo, String marca, String serie,
            String sistema, String departamento) {
        e.setModelo(Regras.exigirTexto("modelo", modelo, 100));
        e.setMarca(Regras.exigirTexto("marca", marca, 100));
        e.setNumeroSerie(Regras.exigirTexto("número de série", serie, 100));
        e.setSistemaOperacional(Regras.textoOpcional("sistema operacional", sistema, 100));
        e.setDepartamento(Regras.textoOpcional("departamento", departamento, 100));
    }
}
