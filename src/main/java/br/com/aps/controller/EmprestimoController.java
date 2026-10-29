package br.com.aps.controller;

import br.com.aps.dao.EmprestimoDAO;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Regras de emprestimos; atomicidade e bloqueios ficam no EmprestimoDAO. */
public class EmprestimoController {
    public record Evento(LocalDateTime quando, String acao, String pessoa, String equipamento) { }

    private final EmprestimoDAO dao;
    public EmprestimoController() { this(new EmprestimoDAO()); }
    public EmprestimoController(EmprestimoDAO dao) { this.dao = dao; }

    public Emprestimo emprestar(int usuarioId, int equipamentoId, LocalDate dataPrevista) {
        if (dataPrevista == null || dataPrevista.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Informe data prevista igual ou posterior a hoje.");
        }
        LocalDateTime retirada = LocalDateTime.now().withNano(0);
        Emprestimo emprestimo = new Emprestimo(usuarioId, equipamentoId,
            retirada, dataPrevista.atTime(LocalTime.of(23, 59, 59)));
        try { dao.inserir(emprestimo); return emprestimo; }
        catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public void devolver(int emprestimoId) {
        try {
            if (!dao.registrarDevolucao(emprestimoId, LocalDateTime.now())) {
                throw new RegraNegocioException("Empréstimo inexistente ou já devolvido.");
            }
        } catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public List<EmprestimoDetalhe> listarDetalhado() {
        try { return dao.listarDetalhado(); }
        catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public List<EmprestimoDetalhe> abertos() {
        List<EmprestimoDetalhe> resultado = new ArrayList<>();
        for (EmprestimoDetalhe d : listarDetalhado()) {
            if (Emprestimo.STATUS_ABERTO.equals(d.emprestimo().getStatus())) resultado.add(d);
        }
        return resultado;
    }

    public List<Evento> historico() {
        List<Evento> eventos = new ArrayList<>();
        for (EmprestimoDetalhe d : listarDetalhado()) {
            Emprestimo e = d.emprestimo();
            String equipamento = d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")";
            eventos.add(new Evento(e.getDataRetirada(), "Empréstimo", d.usuarioNome(), equipamento));
            if (e.getDataDevolucao() != null) {
                eventos.add(new Evento(e.getDataDevolucao(), "Devolução", d.usuarioNome(), equipamento));
            }
        }
        eventos.sort(Comparator.comparing(Evento::quando).reversed());
        return eventos;
    }
}
