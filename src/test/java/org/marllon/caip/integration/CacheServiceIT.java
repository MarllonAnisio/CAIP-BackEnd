package org.marllon.caip.integration;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("integration") // <--- A mágica acontece aqui! Chama o arquivo novo.
class CacheServiceIT {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void deveSalvarERecuperarDoCacheReal() {
        assertNotNull(redissonClient, "RedissonClient não foi injetado!");

        RBucket<String> bucket = redissonClient.getBucket("teste-ci-professor");
        bucket.set("Integração com Redis Perfeita");

        assertEquals("Integração com Redis Perfeita", bucket.get());

        bucket.delete(); // Limpa a sujeira do teste
    }
}