package br.com.aps.dao;

import br.com.aps.model.Equipamento;
import br.com.aps.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EquipamentoDAO {

    private static final String COLUNAS =
            "id, patrimonio, nome, categoria, estado, status";

    public void inserir(Equipamento equipamento) throws SQLException {
        String sql = """
                INSERT INTO equipamento (patrimonio, nome, categoria, estado, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, equipamento.getPatrimonio());
            statement.setString(2, equipamento.getNome());
            statement.setString(3, equipamento.getCategoria());
            statement.setString(4, equipamento.getEstado());
            statement.setString(5, equipamento.getStatus());

            statement.executeUpdate();

            try (ResultSet chaves = statement.getGeneratedKeys()) {
                if (chaves.next()) {
                    equipamento.setId(chaves.getInt(1));
                }
            }
        }
    }

    // Ordem padrao da tabela: nome e, em caso de empate, patrimonio.
    public List<Equipamento> listar() throws SQLException {
        return consultarTodos("ORDER BY nome ASC, patrimonio ASC, id ASC");
    }

    public List<Equipamento> listarOrdenadoPorPatrimonio() throws SQLException {
        return consultarTodos("ORDER BY patrimonio ASC, id ASC");
    }

    public List<Equipamento> listarOrdenadoPorCategoria() throws SQLException {
        return consultarTodos("ORDER BY categoria ASC, nome ASC, id ASC");
    }

    public Equipamento buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM equipamento WHERE id = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return mapearEquipamento(resultado);
                }
            }
        }

        return null;
    }

    // Patrimonio e unico; a busca retorna um registro ou null.
    public Equipamento buscarPorPatrimonio(String patrimonio) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM equipamento WHERE patrimonio = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, patrimonio.trim());

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return mapearEquipamento(resultado);
                }
            }
        }

        return null;
    }

    // Busca parcial de nome: ex. "note" encontra "Notebook Dell".
    public List<Equipamento> buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT " + COLUNAS + """
                 FROM equipamento
                 WHERE nome LIKE ?
                 ORDER BY nome ASC, patrimonio ASC, id ASC
                """;

        List<Equipamento> equipamentos = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + nome.trim() + "%");

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    equipamentos.add(mapearEquipamento(resultado));
                }
            }
        }

        return equipamentos;
    }

    public List<Equipamento> listarPorStatus(String status) throws SQLException {
        String sql = "SELECT " + COLUNAS + """
                 FROM equipamento
                 WHERE status = ?
                 ORDER BY nome ASC, patrimonio ASC, id ASC
                """;

        List<Equipamento> equipamentos = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    equipamentos.add(mapearEquipamento(resultado));
                }
            }
        }

        return equipamentos;
    }

    // Retorna false se o ID informado nao existir.
    public boolean atualizar(Equipamento equipamento) throws SQLException {
        String sql = """
                UPDATE equipamento
                SET patrimonio = ?, nome = ?, categoria = ?, estado = ?, status = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, equipamento.getPatrimonio());
            statement.setString(2, equipamento.getNome());
            statement.setString(3, equipamento.getCategoria());
            statement.setString(4, equipamento.getEstado());
            statement.setString(5, equipamento.getStatus());
            statement.setInt(6, equipamento.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // Equipamento com historico de emprestimos nao pode ser excluido:
    // o MySQL bloqueia pelo ON DELETE RESTRICT da chave estrangeira.
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM equipamento WHERE id = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    // Recebe somente SQL fixo dos metodos acima, nunca texto vindo da tela.
    private List<Equipamento> consultarTodos(String ordenacao) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM equipamento " + ordenacao;
        List<Equipamento> equipamentos = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {
                equipamentos.add(mapearEquipamento(resultado));
            }
        }

        return equipamentos;
    }

    private Equipamento mapearEquipamento(ResultSet resultado) throws SQLException {
        return new Equipamento(
                resultado.getInt("id"),
                resultado.getString("patrimonio"),
                resultado.getString("nome"),
                resultado.getString("categoria"),
                resultado.getString("estado"),
                resultado.getString("status")
        );
    }
}
