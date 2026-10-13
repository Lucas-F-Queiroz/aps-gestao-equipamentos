package br.com.aps.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        DatabaseConfig config = DatabaseConfig.load();
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }
}
