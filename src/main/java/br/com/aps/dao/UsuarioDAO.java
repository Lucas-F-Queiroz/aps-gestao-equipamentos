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
                INSERT INTO usuario (nome, email, tipo, ativo)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getEmail());
            statement.setString(3, usuario.getTipo());
            statement.setBoolean(4, usuario.isAtivo());

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
                SELECT id, nome, email, tipo, ativo
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

    private Usuario mapearUsuario(ResultSet resultSet) throws SQLException {
        return new Usuario(
                resultSet.getInt("id"),
                resultSet.getString("nome"),
                resultSet.getString("email"),
                resultSet.getString("tipo"),
                resultSet.getBoolean("ativo")
        );
    }
}
