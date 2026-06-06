package org.marllon.caip.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Este é um Teste de Integração VERDADEIRO (End-to-End da infraestrutura).
 * Como esta classe herda de AbstractIntegrationTest, ela roda com o perfil "integration"
 * e utiliza o Testcontainers para levantar containers Docker reais de PostgreSQL e Redis
 * antes da execução.
 * 
 * O objetivo é garantir que as configurações da aplicação (driver, senhas, portas)
 * conseguem conversar perfeitamente com um banco e um cache reais, detectando problemas
 * que só ocorreriam em produção (ex: timeouts, falha de autenticação no Redis).
 */
class CacheServiceIT extends AbstractIntegrationTest {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * Valida a via principal do Redisson.
     * Tenta se conectar, salvar um dado no Redis em memória, ler o dado logo em seguida,
     * e garantir que ele foi mantido lá de forma consistente.
     */
    @Test
    @DisplayName("Deve conectar ao Redis via Testcontainers, salvar e recuperar dados reais")
    void deveSalvarERecuperarDoCacheReal() {
        assertNotNull(redissonClient, "RedissonClient não foi injetado! O Spring não conseguiu conectar ao Redis.");

        // Cria um bucket (como se fosse um item isolado de cache) no servidor Redis.
        RBucket<String> bucket = redissonClient.getBucket("teste-ci-professor");
        bucket.set("Integração com Redis Perfeita");

        // Lê do servidor real e valida.
        assertEquals("Integração com Redis Perfeita", bucket.get());

        // Limpa a sujeira para não poluir o Redis (embora o container morra no fim, é boa prática).
        bucket.delete();
    }
}