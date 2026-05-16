package org.marllon.caip.config;

import com.cloudinary.Cloudinary;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfig {

    @Bean
    @Primary // Garante que este bean tenha prioridade sobre o bean real
    public Cloudinary cloudinary() {
        // Retorna um mock do Cloudinary.
        // As chamadas a este objeto em testes não farão nada e não causarão erros.
        return Mockito.mock(Cloudinary.class);
    }
}
