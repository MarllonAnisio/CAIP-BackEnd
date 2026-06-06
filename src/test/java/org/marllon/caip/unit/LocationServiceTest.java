package org.marllon.caip.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marllon.caip.domains.location.dto.request.LocationRequest;
import org.marllon.caip.domains.location.dto.response.LocationResponse;
import org.marllon.caip.domains.location.entity.Location;
import org.marllon.caip.domains.location.exceptions.LocalJaCadastradoException;
import org.marllon.caip.domains.location.exceptions.LocalNaoEncontradoException;
import org.marllon.caip.domains.location.mapper.LocationMapper;
import org.marllon.caip.domains.location.repository.LocationRepository;
import org.marllon.caip.domains.location.service.LocationService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService Unit Tests")
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationService locationService;

    private Location mockLocation;
    private LocationRequest locationRequest;
    private LocationResponse locationResponse;

    @BeforeEach
    void setUp() {
        mockLocation = new Location();
        mockLocation.setId(1L);
        mockLocation.setName("Biblioteca Central");

        locationRequest = new LocationRequest("Biblioteca Central");
        locationResponse = new LocationResponse(1L, "Biblioteca Central");
    }

    @Nested
    @DisplayName("Find Methods")
    class FindMethods {
        
        /**
         * Verifica o fluxo principal de listagem.
         * Garante que o serviço chama o repositório e utiliza o mapper 
         * para converter a resposta de entidades para DTOs corretamente.
         */
        @Test
        @DisplayName("findAll should return a list of locations")
        void findAll_Success() {
            when(locationRepository.findAll()).thenReturn(List.of(mockLocation));
            when(locationMapper.toResponse(mockLocation)).thenReturn(locationResponse);

            List<LocationResponse> result = locationService.findAll();

            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(locationRepository).findAll();
        }

        /**
         * Valida o caminho feliz da busca por ID.
         * Se a localização existe no banco, ela deve ser retornada encapsulada no DTO.
         */
        @Test
        @DisplayName("findById should return location when found")
        void findById_Success() {
            when(locationRepository.findById(1L)).thenReturn(Optional.of(mockLocation));
            when(locationMapper.toResponse(mockLocation)).thenReturn(locationResponse);

            LocationResponse result = locationService.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.id());
        }

        /**
         * Valida o tratamento de exceção.
         * Se uma localização não for encontrada, o sistema deve lançar a exceção de domínio
         * correta e amigável (LocalNaoEncontradoException) em vez de retornar null.
         */
        @Test
        @DisplayName("findById should throw exception when not found")
        void findById_Error_NotFound() {
            when(locationRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(LocalNaoEncontradoException.class, () -> locationService.findById(99L));
        }
    }

    @Nested
    @DisplayName("Create Method")
    class CreateMethod {
        
        /**
         * Garante que a lógica de criação de uma nova Localização funcione.
         * O teste simula que o nome ainda não existe no banco (existsByNameIgnoreCase retorna false)
         * e valida se o repositório de fato salva a nova localização.
         */
        @Test
        @DisplayName("should create location successfully")
        void create_Success() {
            when(locationRepository.existsByNameIgnoreCase("Biblioteca Central")).thenReturn(false);
            when(locationRepository.save(any(Location.class))).thenReturn(mockLocation);
            when(locationMapper.toResponse(mockLocation)).thenReturn(locationResponse);

            locationService.create(locationRequest);

            verify(locationRepository).save(any(Location.class));
        }

        /**
         * Teste da regra de negócio que impede duplicação.
         * Se o administrador tentar cadastrar um local com um nome que já existe (case-insensitive),
         * o sistema DEVE barrar a inserção antes mesmo de chegar no comando do Hibernate.
         */
        @Test
        @DisplayName("should throw exception if location name already exists")
        void create_Error_AlreadyExists() {
            when(locationRepository.existsByNameIgnoreCase("Biblioteca Central")).thenReturn(true);

            assertThrows(LocalJaCadastradoException.class, () -> locationService.create(locationRequest));
            verify(locationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Method")
    class UpdateMethod {
        
        /**
         * Testa o caminho feliz de edição de um local.
         * Se o local for encontrado, e o novo nome não conflitar com outro existente,
         * o serviço deve aplicar as alterações e mandar salvar.
         */
        @Test
        @DisplayName("should update location successfully")
        void update_Success() {
            LocationRequest updateRequest = new LocationRequest("Nova Biblioteca");
            
            when(locationRepository.findById(1L)).thenReturn(Optional.of(mockLocation));
            when(locationRepository.existsByNameIgnoreCase("Nova Biblioteca")).thenReturn(false);
            when(locationRepository.save(mockLocation)).thenReturn(mockLocation);

            locationService.update(1L, updateRequest);

            verify(locationRepository).save(mockLocation);
        }

        /**
         * Impede edição de "Fantasmas".
         * Você não pode editar um local que não existe. O sistema deve abortar rápido.
         */
        @Test
        @DisplayName("should throw exception when updating non-existent location")
        void update_Error_NotFound() {
            when(locationRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(LocalNaoEncontradoException.class, () -> locationService.update(99L, locationRequest));
        }

        /**
         * Teste de restrição de conflito em atualizações.
         * O usuário tenta renomear "Local A" para "Local B". 
         * No entanto, "Local B" já existe! A aplicação deve lançar erro para evitar duplicidade.
         */
        @Test
        @DisplayName("should throw exception if updated name belongs to another location")
        void update_Error_NameConflict() {
            LocationRequest updateRequest = new LocationRequest("Outra Biblioteca");

            when(locationRepository.findById(1L)).thenReturn(Optional.of(mockLocation));
            when(locationRepository.existsByNameIgnoreCase("Outra Biblioteca")).thenReturn(true);

            assertThrows(LocalJaCadastradoException.class, () -> locationService.update(1L, updateRequest));
        }
    }

    @Nested
    @DisplayName("Delete Method")
    class DeleteMethod {
        
        /**
         * Valida que o comando de deletar será executado no banco, 
         * contanto que o Local exista.
         */
        @Test
        @DisplayName("should delete location successfully")
        void delete_Success() {
            when(locationRepository.findById(1L)).thenReturn(Optional.of(mockLocation));
            doNothing().when(locationRepository).delete(mockLocation);

            locationService.delete(1L);

            verify(locationRepository).delete(mockLocation);
        }

        /**
         * Protege contra falhas silenciosas. Tentar deletar um recurso fantasma 
         * deve retornar erro para o front-end saber que aquela operação não faz sentido.
         */
        @Test
        @DisplayName("should throw exception when deleting non-existent location")
        void delete_Error_NotFound() {
            when(locationRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(LocalNaoEncontradoException.class, () -> locationService.delete(99L));
        }
    }
}