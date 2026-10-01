package br.com.aps.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/aps_equipamentos"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Sao_Paulo";

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("APS_DB_URL", DEFAULT_URL);
        String usuario = System.getenv().getOrDefault("APS_DB_USER", "root");
        String senha = System.getenv().getOrDefault("APS_DB_PASSWORD", "");

        return DriverManager.getConnection(url, usuario, senha);
    }
}
