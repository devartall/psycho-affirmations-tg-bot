package com.devartall.psycho.bot

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = [PostgresTestContainerInitializer::class])
@ActiveProfiles("test")
abstract class AbstractIntegrationTest