package br.com.aps.dao;

import br.com.aps.model.Usuario;
import br.com.aps.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void inserir(Usuario usuario) throws SQLException {
        String sql = """
                INSERT INTO usuario (nome, email, area_profissao, ativo, matricula)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getEmail());
            statement.setString(3, usuario.getTipo());
            statement.setBoolean(4, usuario.isAtivo());
            statement.setString(5, usuario.getMatricula());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        String sql = """
                SELECT id, nome, email, area_profissao, ativo, matricula
                FROM usuario
                ORDER BY nome ASC, id ASC
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                usuarios.add(mapearUsuario(resultSet));
            }
        }

        return usuarios;
    }

    // Retorna null se nao existir usuario com o ID informado.
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT id, nome, email, area_profissao, ativo, matricula
                FROM usuario
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearUsuario(resultSet);
                }
            }
        }

        return null;
    }

    // Busca parcial: "ana" encontra "Ana Maria", conforme a collation do banco.
    public List<Usuario> buscarPorNome(String nome) throws SQLException {
        String sql = """
                SELECT id, nome, email, area_profissao, ativo, matricula
                FROM usuario
                WHERE nome LIKE ?
                ORDER BY nome ASC, id ASC
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + nome.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    usuarios.add(mapearUsuario(resultSet));
                }
            }
        }

        return usuarios;
    }

    // Retorna false se o ID nao existir no banco.
    public boolean atualizar(Usuario usuario) throws SQLException {
        String sql = """
                UPDATE usuario
                SET nome = ?, email = ?, area_profissao = ?, ativo = ?, matricula = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getEmail());
            statement.setString(3, usuario.getTipo());
            statement.setBoolean(4, usuario.isAtivo());
            statement.setString(5, usuario.getMatricula());
            statement.setInt(6, usuario.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // ON DELETE RESTRICT impede excluir usuarios com historico de emprestimos.
    // Nesses casos, a SQLException deve ser tratada pelo Controller.
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Usuario mapearUsuario(ResultSet resultSet) throws SQLException {
        Usuario usuario = new Usuario(
                resultSet.getInt("id"),
                resultSet.getString("nome"),
                resultSet.getString("email"),
                resultSet.getString("area_profissao"),
                resultSet.getBoolean("ativo")
        );
        usuario.setMatricula(resultSet.getString("matricula"));
        return usuario;
    }
}
