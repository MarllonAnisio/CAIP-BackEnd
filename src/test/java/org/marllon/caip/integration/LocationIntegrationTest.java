package org.marllon.caip.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.marllon.caip.config.TestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestConfig.class)
@DisplayName("Integration Test: Location API")
class LocationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String LOCATION_API_URL = "/api/locations";

    @Nested
    @DisplayName("GET /api/locations")
    class GetLocations {

        /**
         * Valida se a rota de listagem de locais é pública para qualquer
         * usuário autenticado (no caso, um STUDENT).
         * Garante que o banco de dados foi inicializado com os dados corretos.
         */
        @Test
        @DisplayName("Should return a list of locations for any authenticated user")
        @WithMockUser(roles = "STUDENT")
        void findAll_AsStudent_Success() throws Exception {
            mockMvc.perform(get(LOCATION_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        /**
         * Verifica se a busca detalhada de um local específico funciona e retorna
         * as propriedades corretas da entidade.
         */
        @Test
        @DisplayName("Should return a location by ID for any authenticated user")
        @WithMockUser(roles = "STUDENT")
        void findById_AsStudent_Success() throws Exception {

            mockMvc.perform(get(LOCATION_API_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Biblioteca Central"));
        }

        /**
         * Testa o tratamento de exceção (GlobalExceptionHandler).
         * A busca por um ID inexistente deve retornar HTTP 404 (Not Found).
         */
        @Test
        @DisplayName("Should return 404 Not Found for a non-existent location ID")
        @WithMockUser(roles = "STUDENT")
        void findById_NotFound() throws Exception {
            mockMvc.perform(get(LOCATION_API_URL + "/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/locations")
    class PostLocation {

        /**
         * Teste de segurança (Autorização): Garante que usuários com perfil ADMIN
         * têm permissão para criar novos locais. Verifica se o recurso é persistido
         * e retornado com status 201 Created.
         */
        @Test
        @DisplayName("Should create a new location when user is ADMIN")
        @WithMockUser(roles = "ADMIN")
        void createLocation_AsAdmin_Success() throws Exception {
            String json = "{\"name\": \"Laboratório de Informática\"}";

            mockMvc.perform(post(LOCATION_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("Laboratório de Informática"));
        }

        /**
         * Teste de segurança (Autorização): Garante que usuários LIBRARIAN
         * também têm privilégios administrativos suficientes para gerenciar locais.
         */
        @Test
        @DisplayName("Should create a new location when user is LIBRARIAN")
        @WithMockUser(roles = "LIBRARIAN")
        void createLocation_AsLibrarian_Success() throws Exception {
            String json = "{\"name\": \"Auditório Principal\"}";

            mockMvc.perform(post(LOCATION_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        /**
         * Teste Crítico de Segurança (Caminho Triste):
         * Protege contra vazamento de privilégios. Um estudante normal NUNCA
         * deve conseguir acessar essa rota. O Spring Security deve interceptar
         * a requisição antes de chegar ao controller e retornar 403.
         */
        @Test
        @DisplayName("Should return 403 Forbidden when user is STUDENT")
        @WithMockUser(roles = "STUDENT")
        void createLocation_AsStudent_Forbidden() throws Exception {
            String json = "{\"name\": \"Sala dos Alunos\"}";

            mockMvc.perform(post(LOCATION_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden());
        }

        /**
         * Testa a validação da requisição (@Valid).
         * Garante que não é possível injetar "sujeira" (nome vazio) no banco de dados.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when location name is empty")
        @WithMockUser(roles = "ADMIN")
        void createLocation_EmptyName_BadRequest() throws Exception {
            String json = "{\"name\": \"\"}";

            mockMvc.perform(post(LOCATION_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Testa a regra de negócio de unicidade.
         * Tentar recriar a "Biblioteca Central" (já inserida pelo script SQL)
         * deve ser barrado pela regra de negócio com um erro 400.
         */
        @Test
        @DisplayName("Should return 400 Bad Request when location name already exists")
        @WithMockUser(roles = "ADMIN")
        void createLocation_NameConflict_BadRequest() throws Exception {
            // This name is inserted by data-integration.sql
            String json = "{\"name\": \"Biblioteca Central\"}";

            mockMvc.perform(post(LOCATION_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }
    
    @Nested
    @DisplayName("PUT /api/locations/{id}")
    class PutLocation {
        
        /**
         * Valida se a edição de um local existente funciona como esperado.
         */
        @Test
        @DisplayName("Should update location when user is ADMIN")
        @WithMockUser(roles = "ADMIN")
        void updateLocation_AsAdmin_Success() throws Exception {
            String json = "{\"name\": \"Biblioteca Renovada\"}";

            mockMvc.perform(put(LOCATION_API_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Biblioteca Renovada"));
        }

        /**
         * Garante que o bloqueio de segurança também se aplica ao PUT.
         */
        @Test
        @DisplayName("Should return 403 Forbidden for STUDENT on update")
        @WithMockUser(roles = "STUDENT")
        void updateLocation_AsStudent_Forbidden() throws Exception {
            String json = "{\"name\": \"Não vai funcionar\"}";

            mockMvc.perform(put(LOCATION_API_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden());
        }
    }
    
    @Nested
    @DisplayName("DELETE /api/locations/{id}")
    class DeleteLocation {
        
        /**
         * Testa o fluxo de exclusão.
         * Para garantir isolamento do teste (evitar que a exclusão da Biblioteca Central
         * quebre outros testes), criamos um local falso temporário e o deletamos em seguida.
         */
        @Test
        @DisplayName("Should delete location when user is ADMIN")
        @WithMockUser(roles = "ADMIN")
        void deleteLocation_AsAdmin_Success() throws Exception {
            // First, create a location to be deleted to avoid conflicts with other tests
            String json = "{\"name\": \"Local a ser Deletado\"}";
            String createdLocation = mockMvc.perform(post(LOCATION_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            String id = createdLocation.split("\"id\":")[1].split(",")[0];

            mockMvc.perform(delete(LOCATION_API_URL + "/" + id))
                    .andExpect(status().isNoContent());
        }

        /**
         * Garante que a operação destrutiva (DELETE) é inacessível para usuários base.
         */
        @Test
        @DisplayName("Should return 403 Forbidden for STUDENT on delete")
        @WithMockUser(roles = "STUDENT")
        void deleteLocation_AsStudent_Forbidden() throws Exception {
            mockMvc.perform(delete(LOCATION_API_URL + "/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
