package br.com.aps.util;

import org.flywaydb.core.Flyway;

public final class DatabaseMigrator {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/aps_equipamentos"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Sao_Paulo";

    private DatabaseMigrator() {
    }

    public static void migrate() {
        String url = System.getenv().getOrDefault("APS_DB_URL", DEFAULT_URL);
        String usuario = System.getenv().getOrDefault("APS_DB_USER", "root");
        String senha = System.getenv().getOrDefault("APS_DB_PASSWORD", "");

        Flyway flyway = Flyway.configure()
                .dataSource(url, usuario, senha)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();
    }
}
