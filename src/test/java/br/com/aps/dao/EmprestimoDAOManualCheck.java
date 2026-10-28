package br.com.aps.dao;

import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;
import br.com.aps.util.ConnectionFactory;
import br.com.aps.util.DatabaseMigrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Teste de integracao manual; exige MySQL local.
 * Usa INSERT direto no banco para nao depender dos outros DAOs em PRs separados.
 * Execute main pela IDE depois de criar o banco e configurar a conexao.
 */
public class EmprestimoDAOManualCheck {

    public static void main(String[] args) throws SQLException {
        DatabaseMigrator.migrate();

        String codigo = UUID.randomUUID().toString().substring(0, 12);
        int usuarioId = -1;
        int equipamentoId = -1;
        EmprestimoDAO dao = new EmprestimoDAO();
        LocalDateTime agora = LocalDateTime.now().withNano(0);

        try {
            usuarioId = criarUsuario("teste.emprestimo." + codigo + "@example.invalid");
            equipamentoId = criarEquipamento("APS-TEST-" + codigo);

            Emprestimo emprestimo = new Emprestimo(
                    usuarioId, equipamentoId, agora.minusDays(4), agora.minusDays(2)
            );

            dao.inserir(emprestimo);
            verificar(emprestimo.getId() > 0, "Emprestimo sem ID gerado.");
            verificar(Equipamento.STATUS_EMPRESTADO.equals(statusEquipamento(equipamentoId)),
                    "Equipamento nao virou EMPRESTADO.");
            verificar(Emprestimo.STATUS_ABERTO.equals(
                    dao.buscarPorId(emprestimo.getId()).getStatus()), "Status do emprestimo.");

            // Duas retiradas do mesmo equipamento nao podem ocorrer.
            boolean bloqueouDuplicado = false;
            try {
                dao.inserir(new Emprestimo(
                        usuarioId, equipamentoId, agora, agora.plusDays(2)));
            } catch (SQLException esperado) {
                bloqueouDuplicado = true;
            }
            verificar(bloqueouDuplicado, "Permitiu emprestimo duplicado.");
            verificar(dao.listarPorEquipamento(equipamentoId).size() == 1,
                    "Falha de rollback no emprestimo duplicado.");

            verificar(dao.listarAbertos().stream().anyMatch(
                    e -> e.getId() == emprestimo.getId()), "Listagem de abertos falhou.");
            verificar(dao.listarAtrasados().stream().anyMatch(
                    e -> e.getId() == emprestimo.getId()), "Listagem de atrasados falhou.");
            verificar(dao.listarPorUsuario(usuarioId).stream().anyMatch(
                    e -> e.getId() == emprestimo.getId()), "Historico por usuario falhou.");
            verificar(dao.listar().stream().anyMatch(
                    e -> e.getId() == emprestimo.getId()), "Listagem geral falhou.");
            verificar(dao.listarDetalhado().stream().map(EmprestimoDetalhe::emprestimo)
                    .anyMatch(e -> e.getId() == emprestimo.getId()), "JOIN falhou.");

            // Induz falha de integridade durante a devolucao para testar ROLLBACK.
            atualizarStatusEquipamento(equipamentoId, Equipamento.STATUS_INDISPONIVEL);
            boolean devolucaoBloqueada = false;
            try {
                dao.registrarDevolucao(emprestimo.getId(), agora);
            } catch (SQLException esperado) {
                devolucaoBloqueada = true;
            }
            verificar(devolucaoBloqueada, "Devolucao com estado inconsistente passou.");
            verificar(Emprestimo.STATUS_ABERTO.equals(
                    dao.buscarPorId(emprestimo.getId()).getStatus()),
                    "Devolucao parcial ficou gravada apesar do rollback.");

            atualizarStatusEquipamento(equipamentoId, Equipamento.STATUS_EMPRESTADO);
            verificar(dao.registrarDevolucao(emprestimo.getId(), agora),
                    "Nao registrou devolucao.");
            verificar(Equipamento.STATUS_DISPONIVEL.equals(statusEquipamento(equipamentoId)),
                    "Equipamento nao voltou a ficar disponivel.");
            verificar(Emprestimo.STATUS_DEVOLVIDO.equals(
                    dao.buscarPorId(emprestimo.getId()).getStatus()),
                    "Emprestimo nao virou DEVOLVIDO.");
            verificar(dao.buscarPorId(emprestimo.getId()).getDataDevolucao() != null,
                    "Nao gravou data da devolucao.");
            verificar(!dao.registrarDevolucao(emprestimo.getId(), agora),
                    "Permitiu devolucao repetida.");

            // Usuario inativo nao pode realizar emprestimo.
            desativarUsuario(usuarioId);
            boolean bloqueouInativo = false;
            try {
                dao.inserir(new Emprestimo(
                        usuarioId, equipamentoId, agora, agora.plusDays(1)));
            } catch (SQLException esperado) {
                bloqueouInativo = true;
            }
            verificar(bloqueouInativo, "Permitiu emprestimo de usuario inativo.");
            verificar(Equipamento.STATUS_DISPONIVEL.equals(statusEquipamento(equipamentoId)),
                    "Falha de rollback para usuario inativo.");

            System.out.println("EmprestimoDAO: verificacoes concluidas com sucesso.");
        } finally {
            // Exclui apenas os registros temporarios criados por este teste.
            limpar(usuarioId, equipamentoId);
        }
    }

    private static int criarUsuario(String email) throws SQLException {
        String sql = """
                INSERT INTO usuario (nome, email, area_profissao, ativo)
                VALUES ('Usuario Teste Emprestimo', ?, 'ALUNO', TRUE)
                """;
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, email);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next()) {
                    return r.getInt(1);
                }
            }
        }
        throw new SQLException("Falha ao preparar usuario de teste.");
    }

    private static int criarEquipamento(String patrimonio) throws SQLException {
        String sql = """
                INSERT INTO equipamento (patrimonio, nome, categoria, estado, status, modelo, marca, numero_serie)
                VALUES (?, 'Notebook Teste Emprestimo', 'NOTEBOOK', 'BOM', 'DISPONIVEL', 'Modelo teste', 'Marca teste', UUID())
                """;
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, patrimonio);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next()) {
                    return r.getInt(1);
                }
            }
        }
        throw new SQLException("Falha ao preparar equipamento de teste.");
    }

    private static String statusEquipamento(int equipamentoId) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT status FROM equipamento WHERE id = ?")) {
            p.setInt(1, equipamentoId);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        }
    }

    private static void atualizarStatusEquipamento(int equipamentoId, String status)
            throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE equipamento SET status = ? WHERE id = ?")) {
            p.setString(1, status);
            p.setInt(2, equipamentoId);
            p.executeUpdate();
        }
    }

    private static void desativarUsuario(int usuarioId) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE usuario SET ativo = FALSE WHERE id = ?")) {
            p.setInt(1, usuarioId);
            p.executeUpdate();
        }
    }

    private static void limpar(int usuarioId, int equipamentoId) throws SQLException {
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

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
