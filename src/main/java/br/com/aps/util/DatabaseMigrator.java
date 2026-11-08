package br.com.aps.util;

import java.util.Arrays;

import org.flywaydb.core.Flyway;

public final class DatabaseMigrator {

    private DatabaseMigrator() {
    }

    public static void migrate() {
        DatabaseConfig config = DatabaseConfig.load();
        Flyway v6 = flyway(config, "classpath:db/migration");
        // A main anterior e a V6 publicaram scripts diferentes com a mesma versão 3.
        boolean mainAnterior = Arrays.stream(v6.info().applied())
                .anyMatch(migracao -> "V3__cpf_celular_usuario.sql".equals(migracao.getScript()));
        (mainAnterior ? flyway(config, "classpath:db/migration-main") : v6).migrate();
    }

    private static Flyway flyway(DatabaseConfig config, String historico) {
        return Flyway.configure()
                .dataSource(config.url(), config.user(), config.password())
                .locations("classpath:db/migration-common", historico)
                .load();
    }
}
