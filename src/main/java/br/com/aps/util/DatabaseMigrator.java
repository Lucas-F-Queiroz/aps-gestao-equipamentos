package br.com.aps.util;

import org.flywaydb.core.Flyway;

public final class DatabaseMigrator {

    private DatabaseMigrator() {
    }

    public static void migrate() {
        DatabaseConfig config = DatabaseConfig.load();

        Flyway flyway = Flyway.configure()
                .dataSource(config.url(), config.user(), config.password())
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();
    }
}
