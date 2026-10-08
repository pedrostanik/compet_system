package com.petshop.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * A real PostgreSQL for tests that need a database. Same major version as production (16).
 * Spring starts it once per test context and points the datasource at it (@ServiceConnection);
 * Flyway then builds the schema from db/migration and Hibernate validates it — so every test
 * run also proves the migrations match the entities.
 *
 * Use with @Import(TestcontainersConfiguration.class). Requires Docker.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
    }
}
