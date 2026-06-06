package org.marllon.caip.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Classe base ABSTRATA para todos os testes de integração.
 *
 * O uso de @Testcontainers aqui garante que, para QUALQUER classe de teste que herde dela,
 * um conjunto de containers Docker (PostgreSQL e Redis) será iniciado antes dos testes
 * e destruído depois.
 *
 * @DynamicPropertySource subscreve dinamicamente as configurações do Spring para que a aplicação
 * se conecte aos containers que acabaram de ser criados, em vez de tentar usar localhost.
 *
 * A anotação @ActiveProfiles("integration") é herdada, garantindo que todos os testes de integração
 * usem a configuração correta que espera um banco e um cache reais.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }
}
