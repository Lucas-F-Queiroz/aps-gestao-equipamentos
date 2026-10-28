package br.com.aps.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Le os dados de conexao do application.properties local.
 * O arquivo real nao e versionado: cada aluno informa as credenciais do seu MySQL.
 */
record DatabaseConfig(String url, String user, String password) {

    DatabaseConfig {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Configure db.url em application.properties.");
        }
        if (user == null || user.isBlank()) {
            throw new IllegalStateException("Configure db.user em application.properties.");
        }
        if (password == null) {
            throw new IllegalStateException("Configure db.password em application.properties (pode ser vazio).");
        }
    }

    static DatabaseConfig load() {
        try (InputStream input = DatabaseConfig.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                    "Crie src/main/resources/application.properties a partir de application.properties.example.");
            }

            Properties propriedades = new Properties();
            propriedades.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            return new DatabaseConfig(
                propriedades.getProperty("db.url"),
                propriedades.getProperty("db.user"),
                propriedades.getProperty("db.password")
            );
        } catch (IOException erro) {
            throw new IllegalStateException("Nao foi possivel ler application.properties.", erro);
        }
    }
}
