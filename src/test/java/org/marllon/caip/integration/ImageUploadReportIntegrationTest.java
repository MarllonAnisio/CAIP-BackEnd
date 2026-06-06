package org.marllon.caip.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.marllon.caip.config.TestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestConfig.class)
@DisplayName("Integration Test: Image Upload and Report Flow")
class ImageUploadReportIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Testa a integração do endpoint de upload.
     * Como o FileStorageService está "mockado" (veja TestConfig), este teste 
     * não bate na internet. Ele valida se o Controller aceita o MultipartFile 
     * e retorna o formato esperado de JSON (com a chave "url") quando o serviço mockado responde.
     */
    @Test
    @DisplayName("Should upload an image and return its URL")
    @WithMockUser(roles = "STUDENT")
    void shouldUploadImageAndReturnUrl() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-data".getBytes()
        );

        mockMvc.perform(multipart("/images/upload").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.url").isString());
    }

    /**
     * Testa o fluxo principal de denúncia de itens perdidos.
     * Valida se a API recebe um payload complexo, converte para entidade, liga
     * com a Location do banco e persiste tudo com sucesso.
     */
    @Test
    @DisplayName("Should create a report with a given image URL")
    @WithMockUser(username = "student001", roles = "STUDENT")
    void shouldCreateReportWithImageUrl() throws Exception {
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/v1/caip/reports/test.jpg";
        String reportJson = String.format("""
                {
                    "title": "Lost Glasses",
                    "description": "Black frame glasses",
                    "typeReport": "LOST",
                    "imageUrl": "%s",
                    "locationId": 1,
                    "position": {
                        "latitude": -22.9068,
                        "longitude": -43.1729
                    }
                }
                """, imageUrl);

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Lost Glasses"))
                .andExpect(jsonPath("$.imageUrl").value(imageUrl));
    }

    /**
     * Testa se a busca por ID traz todos os dados corretos, incluindo 
     * as relações (como a URL da imagem que veio do serviço externo).
     */
    @Test
    @DisplayName("Should retrieve a report and include the image URL")
    @WithMockUser(username = "student001", roles = "STUDENT")
    void shouldRetrieveReportWithImageUrl() throws Exception {
        // This test assumes a report with ID 1 exists from data-integration.sql
        long reportId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/" + reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId))
                .andExpect(jsonPath("$.imageUrl").value(containsString("res.cloudinary.com")));
    }

    /**
     * Teste de Validação (@Valid).
     * O sistema deve recusar a criação do post se faltarem dados críticos, 
     * neste caso, se o DTO não receber a URL da imagem.
     */
    @Test
    @DisplayName("Should fail to create a report if image URL is missing")
    @WithMockUser(username = "student001", roles = "STUDENT")
    void shouldFailToCreateReportWithoutImageUrl() throws Exception {
        String reportJsonWithoutImage = """
                {
                    "title": "Lost Item",
                    "description": "Item description",
                    "typeReport": "LOST",
                    "locationId": 1,
                    "position": {
                        "latitude": -22.9068,
                        "longitude": -43.1729
                    }
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportJsonWithoutImage))
                .andExpect(status().isBadRequest());
    }

    /**
     * Testa o filtro por usuário criador.
     * Garante que o endpoint 'my-reports' lê corretamente o contexto do Spring Security
     * (o usuário "student001") e aplica o filtro via JPQL no ReportRepository.
     */
    @Test
    @DisplayName("Should return all reports created by the user")
    @WithMockUser(username = "student001", roles = "STUDENT")
    void shouldReturnAllReportsFromUser() throws Exception {
        // This test assumes the user 'student001' has reports from data-integration.sql
        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/my-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].imageUrl").exists());
    }
}
