package br.com.aps.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Configuracao independente da maquina: ambiente > arquivo opcional > MySQL do Docker local. */
record DatabaseConfig(String url, String user, String password) {
    DatabaseConfig {
        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null) {
            throw new IllegalStateException("Configuracao JDBC incompleta.");
        }
    }

    static DatabaseConfig load() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
        } catch (IOException erro) {
            throw new IllegalStateException("Falha ao ler application.properties.", erro);
        }
        return new DatabaseConfig(
            configurar("APS_DB_URL", properties.getProperty("db.url"),
                "jdbc:mysql://127.0.0.1:3307/aps_equipamentos?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo"),
            configurar("APS_DB_USER", properties.getProperty("db.user"), "aps"),
            configurar("APS_DB_PASSWORD", properties.getProperty("db.password"), "aps_local")
        );
    }

    private static String configurar(String nome, String propriedade, String padrao) {
        String ambiente = System.getenv(nome);
        if (ambiente != null) return ambiente;
        if (propriedade != null) return propriedade;
        return padrao;
    }
}
