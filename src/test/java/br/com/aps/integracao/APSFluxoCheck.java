package br.com.aps.integracao;

import br.com.aps.controller.*;
import br.com.aps.dao.*;
import br.com.aps.model.*;
import br.com.aps.util.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

/** Integracao JDBC + Controller, executada apenas contra MySQL isolado no CI. */
public class APSFluxoCheck {
    private static void exigir(boolean ok, String mensagem) {
        if (!ok) throw new AssertionError(mensagem);
    }

    public static void main(String[] args) throws Exception {
        DatabaseMigrator.migrate();
        UsuarioController usuarios = new UsuarioController();
        EquipamentoController equipamentos = new EquipamentoController();
        EmprestimoController emprestimos = new EmprestimoController();
        String tag = UUID.randomUUID().toString().substring(0, 10);
        int usuarioId = -1;
        int equipamentoId = -1;

        try {
            Usuario usuario = usuarios.cadastrar("Pessoa " + tag,
                "aps." + tag + "@example.invalid", Usuario.TIPO_ALUNO);
            usuarioId = usuario.getId();
            Equipamento eq = equipamentos.cadastrar("PAT-" + tag, "Notebook " + tag,
                "NOTEBOOK", Equipamento.ESTADO_BOM);
            equipamentoId = eq.getId();

            exigir(equipamentos.listarPorPatrimonio().stream()
                    .anyMatch(item -> item.getId() == eq.getId()),
                    "Ordenacao por patrimonio nao retorna o equipamento.");
            exigir(equipamentos.listarPorCategoria().stream()
                    .anyMatch(item -> item.getId() == eq.getId()),
                    "Ordenacao por categoria nao retorna o equipamento.");

            Emprestimo emprestimo = emprestimos.emprestar(
                usuarioId, equipamentoId, LocalDate.now().plusDays(7));
            exigir(emprestimo.getId() > 0, "Emprestimo sem ID.");
            exigir(Equipamento.STATUS_EMPRESTADO.equals(
                new EquipamentoDAO().buscarPorId(equipamentoId).getStatus()),
                "Equipamento nao ficou emprestado.");

            boolean bloqueado = false;
            try {
                emprestimos.emprestar(usuarioId, equipamentoId, LocalDate.now().plusDays(2));
            } catch (RegraNegocioException esperado) { bloqueado = true; }
            exigir(bloqueado, "Aceitou dois emprestimos abertos.");

            bloqueado = false;
            try { equipamentos.alterarStatus(equipamentoId, Equipamento.STATUS_DISPONIVEL); }
            catch (RegraNegocioException esperado) { bloqueado = true; }
            exigir(bloqueado, "Permitiu mudar status de equipamento emprestado.");

            equipamentos.atualizar(equipamentoId, eq.getPatrimonio(), "Nome alterado",
                eq.getCategoria(), Equipamento.ESTADO_REGULAR);
            exigir(Equipamento.STATUS_EMPRESTADO.equals(
                new EquipamentoDAO().buscarPorId(equipamentoId).getStatus()),
                "Editar cadastro sobreescreveu status EMPRESTADO.");

            emprestimos.devolver(emprestimo.getId());
            exigir(Equipamento.STATUS_DISPONIVEL.equals(
                new EquipamentoDAO().buscarPorId(equipamentoId).getStatus()),
                "Devolucao nao restaurou disponibilidade.");
            exigir(Emprestimo.STATUS_DEVOLVIDO.equals(
                new EmprestimoDAO().buscarPorId(emprestimo.getId()).getStatus()),
                "Emprestimo nao foi devolvido.");

            equipamentos.alterarStatus(equipamentoId, Equipamento.STATUS_INDISPONIVEL);
            exigir(Equipamento.STATUS_INDISPONIVEL.equals(
                new EquipamentoDAO().buscarPorId(equipamentoId).getStatus()),
                "Falha ao marcar indisponivel.");
            System.out.println("APSFluxoCheck: sistema MVC e transacoes OK");
        } finally {
            try (Connection c = ConnectionFactory.getConnection()) {
                if (equipamentoId > 0) {
                    try (PreparedStatement p = c.prepareStatement(
                            "DELETE FROM emprestimo WHERE equipamento_id = ?")) {
                        p.setInt(1, equipamentoId);
                        p.executeUpdate();
                    }
                    try (PreparedStatement p = c.prepareStatement(
                            "DELETE FROM equipamento WHERE id = ?")) {
                        p.setInt(1, equipamentoId);
                        p.executeUpdate();
                    }
                }
                if (usuarioId > 0) {
                    try (PreparedStatement p = c.prepareStatement(
                            "DELETE FROM usuario WHERE id = ?")) {
                        p.setInt(1, usuarioId);
                        p.executeUpdate();
                    }
                }
            }
        }
    }
}
