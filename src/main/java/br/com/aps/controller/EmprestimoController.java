package br.com.aps.controller;

import br.com.aps.dao.DevolucaoDAO;
import br.com.aps.dao.EmprestimoDAO;
import br.com.aps.dao.EquipamentoDAO;
import br.com.aps.dao.UsuarioDAO;
import br.com.aps.model.DevolucaoDetalhe;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;
import br.com.aps.model.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Emprestimos e devolucoes, com as regras do Lucas:
 *   1. a pessoa precisa estar ativa;
 *   2. o equipamento precisa estar DISPONIVEL;
 *   3. um equipamento nao pode ter dois emprestimos abertos;
 *   4. ao emprestar: equipamento EMPRESTADO e emprestimo ABERTO;
 *   5. ao devolver: emprestimo DEVOLVIDO, data de devolucao preenchida, equipamento DISPONIVEL.
 * A devolucao prevista nao pode ser anterior a retirada.
 * As regras 4 e 5 sao gravadas em transacao pelo EmprestimoDAO.
 */
public class EmprestimoController {

    /** Uma linha do historico: um emprestimo ou uma devolucao, com data e hora. */
    public record Evento(LocalDateTime quando, String acao, String pessoa, String equipamento) {
    }

    private final EmprestimoDAO emprestimoDAO;
    private final UsuarioDAO usuarioDAO;
    private final EquipamentoDAO equipamentoDAO;
    private final DevolucaoDAO devolucaoDAO;

    public EmprestimoController() {
        this(new EmprestimoDAO(), new UsuarioDAO(), new EquipamentoDAO(), new DevolucaoDAO());
    }

    public EmprestimoController(EmprestimoDAO emprestimoDAO, UsuarioDAO usuarioDAO,
                                EquipamentoDAO equipamentoDAO, DevolucaoDAO devolucaoDAO) {
        this.emprestimoDAO = emprestimoDAO;
        this.usuarioDAO = usuarioDAO;
        this.equipamentoDAO = equipamentoDAO;
        this.devolucaoDAO = devolucaoDAO;
    }

    /**
     * Registra um emprestimo. A retirada e agora; a devolucao prevista vale ate o fim do dia
     * informado e nao pode ser anterior a hoje.
     */
    public Emprestimo emprestar(int usuarioId, int equipamentoId, LocalDate dataPrevista) {
        if (dataPrevista == null) {
            throw new RegraNegocioException("Informe a devolução prevista.");
        }
        if (dataPrevista.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A devolução prevista não pode ser anterior à retirada (hoje).");
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
            if (usuario == null || !usuario.isAtivo()) {                              // regra 1
                throw new RegraNegocioException("A pessoa precisa estar ativa para realizar um empréstimo.");
            }
            Equipamento equipamento = equipamentoDAO.buscarPorId(equipamentoId);
            if (equipamento == null || !Equipamento.STATUS_DISPONIVEL.equals(equipamento.getStatus())) { // regra 2
                throw new RegraNegocioException("O equipamento precisa estar com status Disponível.");
            }
            for (Emprestimo existente : emprestimoDAO.listarPorEquipamento(equipamentoId)) {   // regra 3
                if (Emprestimo.STATUS_ABERTO.equals(existente.getStatus())) {
                    throw new RegraNegocioException("Este equipamento já possui um empréstimo aberto.");
                }
            }

            LocalDateTime retirada = LocalDateTime.now().withNano(0);
            LocalDateTime prevista = dataPrevista.atTime(LocalTime.of(23, 59, 59));
            Emprestimo emprestimo = new Emprestimo(usuarioId, equipamentoId, retirada, prevista);
            emprestimoDAO.inserir(emprestimo);                                          // regra 4 (transacao)
            return emprestimo;
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        } catch (IllegalArgumentException erro) {
            throw new RegraNegocioException(erro.getMessage(), erro);
        }
    }

    /**
     * Devolucao (regra 5): informa COMO o equipamento voltou (NOVO, BOM, REGULAR ou DANIFICADO)
     * e uma observacao opcional. O equipamento volta para DISPONIVEL com o estado informado.
     */
    public void devolver(int emprestimoId, String estado, String observacao) {
        devolver(emprestimoId, estado, observacao, null);
    }

    public void devolver(int emprestimoId, String estado, String observacao, String responsavel) {
        String responsavelValido = Regras.exigirTexto("responsável pelo registro", responsavel, 100);
        String estadoValido = Regras.exigirUm("estado do equipamento", estado, Regras.ESTADOS);
        String observacaoValida = observacao == null ? "" : observacao.trim();
        if (observacaoValida.length() > Regras.OBSERVACAO_MAX) {
            throw new RegraNegocioException(
                "O campo observações aceita no máximo " + Regras.OBSERVACAO_MAX + " caracteres.");
        }

        try {
            boolean devolvido = emprestimoDAO.registrarDevolucao(
                emprestimoId, LocalDateTime.now(), estadoValido, observacaoValida, responsavelValido);
            if (!devolvido) {
                if (emprestimoDAO.buscarPorId(emprestimoId) == null) {
                    throw new RegraNegocioException("Empréstimo não encontrado.");
                }
                throw new RegraNegocioException("Este empréstimo já foi devolvido.");
            }
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        } catch (IllegalArgumentException erro) {
            throw new RegraNegocioException(erro.getMessage(), erro);
        }
    }

    /** Todos os emprestimos, com nomes (do mais novo para o mais antigo). */
    public List<EmprestimoDetalhe> listarDetalhado() {
        try {
            return emprestimoDAO.listarDetalhado();
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    /** So os emprestimos ABERTOS (ainda nao devolvidos). */
    public List<EmprestimoDetalhe> abertos() {
        List<EmprestimoDetalhe> abertos = new ArrayList<>();
        for (EmprestimoDetalhe detalhe : listarDetalhado()) {
            if (Emprestimo.STATUS_ABERTO.equals(detalhe.emprestimo().getStatus())) {
                abertos.add(detalhe);
            }
        }
        return abertos;
    }

    /** Estado em que os equipamentos foram devolvidos (alimenta a tela Relatorios). */
    public List<DevolucaoDetalhe> devolucoes() {
        try {
            return devolucaoDAO.listarDetalhado();
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    /** Historico de emprestimos e devolucoes, do mais novo para o mais antigo. */
    public List<Evento> historico() {
        try {
            Map<Integer, String> categorias = new HashMap<>();
            for (Equipamento equipamento : equipamentoDAO.listar()) {
                categorias.put(equipamento.getId(), equipamento.getCategoria());
            }

            List<Evento> eventos = new ArrayList<>();
            for (EmprestimoDetalhe detalhe : emprestimoDAO.listarDetalhado()) {
                Emprestimo emprestimo = detalhe.emprestimo();
                String equipamento = (categorias.getOrDefault(emprestimo.getEquipamentoId(), "")
                    + " " + detalhe.equipamentoPatrimonio()).trim();
                eventos.add(new Evento(emprestimo.getDataRetirada(), "Empréstimo", detalhe.usuarioNome(), equipamento));
                if (emprestimo.getDataDevolucao() != null) {
                    eventos.add(new Evento(emprestimo.getDataDevolucao(), "Devolução", detalhe.usuarioNome(), equipamento));
                }
            }
            eventos.sort(Comparator.comparing(Evento::quando).reversed());
            return eventos;
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }
}
