package org.marllon.caip.config;

import org.marllon.caip.domains.image.service.FileStorageService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Profile({"test", "integration"}) // Agora ativo em ambos os perfis de teste
@Configuration
public class TestConfig {

    /**
     * Cria um "dublê" (mock) do FileStorageService para todos os testes.
     * Isso é CRUCIAL para garantir que NENHUM teste (seja de unidade ou integração)
     * tente fazer uma chamada real para a internet (Cloudinary, S3, etc.).
     * Testes automatizados devem ser independentes de serviços externos.
     */
    @Bean
    @Primary
    public FileStorageService fileStorageService() {
        FileStorageService mockService = Mockito.mock(FileStorageService.class);

        // Comportamento padrão para o upload: sempre retorna uma URL fixa.
        when(mockService.upload(any(), anyString()))
                .thenReturn("https://res.cloudinary.com/test-cloud/image/upload/v1/caip/reports/mock-test.jpg");

        // Comportamento padrão para o delete: não faz nada e não lança exceção.
        doNothing().when(mockService).delete(anyString());

        return mockService;
    }
}
