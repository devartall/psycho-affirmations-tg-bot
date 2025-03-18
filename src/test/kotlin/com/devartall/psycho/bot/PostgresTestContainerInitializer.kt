package com.devartall.psycho.bot

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer

object PostgresTestContainer {
    val instance: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:17.4") // Укажите версию Postgres
        .apply {
            withDatabaseName("test-db")
            withUsername("test-user")
            withPassword("test-pass")
            start()
        }
}

class PostgresTestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val postgresContainer = PostgresTestContainer.instance

        TestPropertyValues.of(
            "spring.datasource.url=${postgresContainer.jdbcUrl}",
            "spring.datasource.username=${postgresContainer.username}",
            "spring.datasource.password=${postgresContainer.password}",
        ).applyTo(applicationContext.environment)
    }
}
