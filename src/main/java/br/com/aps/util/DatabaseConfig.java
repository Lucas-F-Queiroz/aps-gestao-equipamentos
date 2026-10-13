package br.com.aps.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

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
                throw new IllegalStateException("Crie src/main/resources/application.properties a partir de application.properties.example.");
            }
            Properties properties = new Properties();
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            return new DatabaseConfig(properties.getProperty("db.url"),
                    properties.getProperty("db.user"), properties.getProperty("db.password"));
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler application.properties.", e);
        }
    }
}
