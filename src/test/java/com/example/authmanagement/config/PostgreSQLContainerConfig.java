package com.example.authmanagement.config;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgreSQLContainerConfig implements AfterAllCallback {

    @Container
    protected static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:15.0-alpine")
            .withDatabaseName("authdb")
            .withUsername("myuser")
            .withPassword("mypassword");

    static {
        postgresqlContainer.start();
        System.setProperty("spring.datasource.url", postgresqlContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresqlContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresqlContainer.getPassword());
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        postgresqlContainer.stop();
    }

}
