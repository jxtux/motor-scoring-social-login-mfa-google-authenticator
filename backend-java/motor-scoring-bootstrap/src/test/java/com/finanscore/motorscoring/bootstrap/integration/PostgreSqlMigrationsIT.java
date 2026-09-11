package com.finanscore.motorscoring.bootstrap.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlMigrationsIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("motor_scoring")
            .withUsername("motor_scoring")
            .withPassword("motor_scoring");

    @Test
    void debeAplicarTodasLasMigracionesFlywayEnPostgreSqlReal() {
        var flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();
        var result = flyway.migrate();
        assertTrue(result.success);
        assertTrue(result.migrationsExecuted >= 5);
    }
}
