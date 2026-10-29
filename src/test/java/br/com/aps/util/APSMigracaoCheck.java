package br.com.aps.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.Flyway;

/** Primeiro teste do CI, em banco vazio: simula V1/V2 e verifica a atualizacao sem perda. */
public final class APSMigracaoCheck {
    public static void main(String[] args) throws Exception {
        DatabaseConfig config = DatabaseConfig.load();
        Flyway.configure().dataSource(config.url(), config.user(), config.password())
                .locations("classpath:db/migration").target("2").load().migrate();
        String email = UUID.randomUUID() + "@example.invalid";
        int id;
        try (Connection c = DriverManager.getConnection(config.url(), config.user(), config.password())) {
            try (ResultSet colunas = c.getMetaData().getColumns(c.getCatalog(), null, "usuario", "cpf")) {
                if (colunas.next()) throw new AssertionError("Execute este teste primeiro em banco vazio");
            }
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO usuario (nome, email, tipo, ativo) VALUES (?, ?, 'ALUNO', true)",
                    Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, "Cadastro anterior a V3");
                p.setString(2, email);
                p.executeUpdate();
                try (ResultSet keys = p.getGeneratedKeys()) {
                    if (!keys.next()) throw new AssertionError("Sem ID do cadastro antigo");
                    id = keys.getInt(1);
                }
            }
            try {
                DatabaseMigrator.migrate();
                try (PreparedStatement p = c.prepareStatement("SELECT nome, cpf, celular FROM usuario WHERE id = ?")) {
                    p.setInt(1, id);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || !"Cadastro anterior a V3".equals(r.getString("nome"))
                                || r.getString("cpf") != null || r.getString("celular") != null) {
                            throw new AssertionError("V3 alterou ou removeu o cadastro existente");
                        }
                    }
                }
            } finally {
                try (PreparedStatement p = c.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
                    p.setInt(1, id);
                    p.executeUpdate();
                }
            }
        }
        System.out.println("APSMigracaoCheck: V1/V2 -> V3 preserva o cadastro existente");
    }
}
