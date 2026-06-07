package org.marllon.caip.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marllon.caip.core.security.SecurityContextService;

import org.marllon.caip.domains.image.exeptions.FileStorageException;
import org.marllon.caip.domains.image.service.FileStorageService;
import org.marllon.caip.domains.location.entity.Location;
import org.marllon.caip.domains.location.exceptions.LocalNaoEncontradoException;
import org.marllon.caip.domains.location.service.LocationService;
import org.marllon.caip.domains.report.dto.request.ReportRequest;
import org.marllon.caip.domains.report.dto.response.ReportResponse;
import org.marllon.caip.domains.report.entity.Report;
import org.marllon.caip.domains.report.entity.StatusStep;
import org.marllon.caip.domains.report.entity.constants.TypeReport;
import org.marllon.caip.domains.report.exceptions.ReportNotFoundException;
import org.marllon.caip.domains.report.exceptions.ReportStatusTransitionException;
import org.marllon.caip.domains.report.exceptions.StatusConfigurationException;
import org.marllon.caip.domains.report.mapper.ReportMapper;
import org.marllon.caip.domains.report.repository.ReportRepository;
import org.marllon.caip.domains.report.repository.StatusStepRepository;
import org.marllon.caip.domains.report.service.ReportService;
import org.marllon.caip.domains.user.entity.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Unit Tests")
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ReportMapper reportMapper;
    @Mock
    private StatusStepRepository statusStepRepository;
    @Mock
    private LocationService locationService;
    @Mock
    private SecurityContextService securityContextService;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ReportService reportService;

    private User mockUser;
    private Location mockLocation;
    private Report mockReport;
    private ReportRequest reportRequest;
    private StatusStep mockStatus;
    private ReportResponse mockReportResponse;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setRegistration("123456");

        mockLocation = new Location();
        mockLocation.setId(1L);
        mockLocation.setName("Biblioteca");

        mockStatus = new StatusStep();
        mockStatus.setId(1L);
        mockStatus.setName("LOST");

        mockReport = Report.builder()
                .id(1L)
                .title("Casaco Preto")
                .typeReport(TypeReport.LOST)
                .location(mockLocation)
                .imageUrl("http://image.com/casaco.jpg")
                .isClosed(false)
                .statusSteps(new ArrayList<>(List.of(mockStatus)))
                .build();

        reportRequest = new ReportRequest("Casaco Preto", "Casaco de couro preto", "LOST", "http://image.com/casaco.jpg", 1L, null);
        mockReportResponse = mock(ReportResponse.class);
    }

    @Nested
    @DisplayName("Find Methods")
    class FindMethods {
        
        /**
         * Testa a busca de um relatório pelo ID.
         * Caminho feliz: O relatório existe, é mapeado para Response e retornado.
         */
        @Test
        @DisplayName("findById should return report when found")
        void findById_Success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(reportMapper.toResponse(mockReport)).thenReturn(mockReportResponse);

            ReportResponse response = reportService.findById(1L);

            assertNotNull(response);
            verify(reportRepository).findById(1L);
        }

        /**
         * Testa o cenário de erro ao buscar um ID inexistente.
         * Garante que uma ReportNotFoundException seja lançada.
         */
        @Test
        @DisplayName("findById should throw exception when not found")
        void findById_Error_NotFound() {
            when(reportRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ReportNotFoundException.class, () -> reportService.findById(99L));
        }

        /**
         * Testa a busca dos relatórios do usuário logado.
         * Garante que a integração com o AuthService e ReportRepository funciona.
         */
        @Test
        @DisplayName("findMyReports should return user's reports")
        void findMyReports_Success() {
            when(securityContextService.getAuthenticatedUser()).thenReturn(mockUser);
            when(reportRepository.findAllByCreator(mockUser)).thenReturn(List.of(mockReport));

            List<ReportResponse> result = reportService.findMyReports();

            assertFalse(result.isEmpty());
            verify(reportRepository).findAllByCreator(mockUser);
        }
    }

    @Nested
    @DisplayName("Save Method")
    class SaveMethod {
        
        /**
         * Testa a criação de um novo relatório.
         * Valida que as dependências (AuthService, LocationService, StatusStepRepository) 
         * são chamadas corretamente e que o repositório salva o relatório mapeado.
         */
        @Test
        @DisplayName("should save new report successfully")
        void save_Success() {
            when(securityContextService.getAuthenticatedUser()).thenReturn(mockUser);
            when(locationService.findEntityById(1L)).thenReturn(mockLocation);
            when(reportMapper.toEntity(any(), any(), any())).thenReturn(mockReport);
            when(statusStepRepository.findByName("LOST")).thenReturn(Optional.of(mockStatus));
            when(reportRepository.save(any(Report.class))).thenReturn(mockReport);
            when(reportMapper.toResponse(mockReport)).thenReturn(mockReportResponse);

            ReportResponse response = reportService.save(reportRequest);

            assertNotNull(response);
            verify(reportRepository).save(any(Report.class));
        }

        /**
         * Testa a resiliência na criação de relatório:
         * Se o status inicial (ex: "LOST") não estiver populado na base (StatusStep), 
         * o sistema deve abortar para evitar inconsistência de dados.
         */
        @Test
        @DisplayName("should throw exception if initial status is not configured")
        void save_Error_StatusNotFound() {
            when(securityContextService.getAuthenticatedUser()).thenReturn(mockUser);
            when(locationService.findEntityById(1L)).thenReturn(mockLocation);
            when(reportMapper.toEntity(any(), any(), any())).thenReturn(mockReport);
            when(statusStepRepository.findByName("LOST")).thenReturn(Optional.empty());

            assertThrows(StatusConfigurationException.class, () -> reportService.save(reportRequest));
        }

        /**
         * Testa validação de domínio:
         * Você não pode criar um relatório atrelado a uma Location (ex: Sala 1) que não existe.
         */
        @Test
        @DisplayName("should throw exception if location is not found")
        void save_Error_LocationNotFound() {
            when(securityContextService.getAuthenticatedUser()).thenReturn(mockUser);
            when(locationService.findEntityById(99L)).thenThrow(new LocalNaoEncontradoException("Location not found"));

            ReportRequest requestWithInvalidLocation = new ReportRequest("Title", "Desc", "LOST", "url", 99L, null);

            assertThrows(LocalNaoEncontradoException.class, () -> reportService.save(requestWithInvalidLocation));
        }
    }

    @Nested
    @DisplayName("Update Method")
    class UpdateMethod {
        
        /**
         * Testa o caminho feliz de edição de relatórios.
         */
        @Test
        @DisplayName("should update report successfully")
        void update_Success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(locationService.findEntityById(1L)).thenReturn(mockLocation);
            when(reportRepository.save(mockReport)).thenReturn(mockReport);
            when(reportMapper.toResponse(mockReport)).thenReturn(mockReportResponse);

            ReportResponse response = reportService.update(1L, reportRequest);

            assertNotNull(response);
            verify(reportMapper).updateEntity(mockReport, reportRequest);
            verify(reportRepository).save(mockReport);
        }

        /**
         * Teste de transição de estado inválida:
         * Um relatório com status terminal ("CONCLUÍDO"/isClosed=true) não pode mais ser editado.
         */
        @Test
        @DisplayName("should throw exception when updating a closed report")
        void update_Error_ReportIsClosed() {
            mockReport.setClosed(true);
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));

            assertThrows(ReportStatusTransitionException.class, () -> reportService.update(1L, reportRequest));
        }

        /**
         * Erro comum: Tentar editar algo que não existe.
         */
        @Test
        @DisplayName("should throw exception when updating a non-existent report")
        void update_Error_NotFound() {
            when(reportRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ReportNotFoundException.class, () -> reportService.update(99L, reportRequest));
        }
    }

    @Nested
    @DisplayName("Delete Methods")
    class DeleteMethods {
        
        /**
         * Testa a exclusão completa: o sistema deve primeiro tentar excluir a imagem
         * no Storage Cloud (ex: Cloudinary) e, em seguida, fazer o soft delete do relatório.
         */
        @Test
        @DisplayName("deleteReport should delete image and report successfully")
        void deleteReport_Success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(securityContextService.getAuthenticatedUser()).thenReturn(mockUser);
            
            reportService.deleteReport(1L);

            verify(fileStorageService).delete(mockReport.getImageUrl());
            verify(reportRepository).delete(mockReport);
        }

        /**
         * Teste de resiliência (CRÍTICO): 
         * Se a API do serviço de storage (Cloudinary) cair ou a imagem já não existir lá, 
         * o processo de exclusão do relatório NÃO pode quebrar. O erro é logado, mas o 
         * Report no BD ainda é deletado.
         */
        @Test
        @DisplayName("deleteReport should still delete report even if image deletion fails")
        void deleteReport_ShouldContinueWhenFileStorageFails() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(securityContextService.getAuthenticatedUser()).thenReturn(mockUser);
            doThrow(new FileStorageException("Simulated S3 error")).when(fileStorageService).delete(anyString());

            reportService.deleteReport(1L);

            verify(fileStorageService).delete(mockReport.getImageUrl());
            verify(reportRepository).delete(mockReport); // VERIFICA que o delete do report ainda é chamado
        }

        /**
         * Erro ao tentar deletar algo que não existe.
         */
        @Test
        @DisplayName("deleteReport should throw exception if report not found")
        void deleteReport_Error_NotFound() {
            when(securityContextService.getAuthenticatedUser()).thenReturn(mockUser);
            when(reportRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ReportNotFoundException.class, () -> reportService.deleteReport(99L));
        }
    }

    @Nested
    @DisplayName("Business Logic Methods")
    class BusinessLogic {
        
        /**
         * Testa a operação de fechar um relatório isoladamente.
         */
        @Test
        @DisplayName("closeReport should set isClosed to true")
        void closeReport_Success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(reportRepository.save(any(Report.class))).thenReturn(mockReport);

            reportService.closeReport(1L);

            assertTrue(mockReport.isClosed());
            verify(reportRepository).save(mockReport);
        }

        /**
         * Regra de negócio: Você não pode fechar o que já está fechado.
         */
        @Test
        @DisplayName("closeReport should throw exception if already closed")
        void closeReport_Error_AlreadyClosed() {
            mockReport.setClosed(true);
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));

            assertThrows(ReportStatusTransitionException.class, () -> reportService.closeReport(1L));
        }

        /**
         * O teste mais complexo da classe.
         * Garante que o processo de "Match" funciona: Pega um item PERDIDO, 
         * pega um item ENCONTRADO, altera o status de AMBOS para "CONCLUÍDO" 
         * e marca ambos como fechados.
         */
        @Test
        @DisplayName("linkReports should succeed with valid reports")
        void linkReports_Success() {
            Report mockFound = Report.builder().id(2L).typeReport(TypeReport.FOUND).statusSteps(new ArrayList<>()).isClosed(false).build();
            StatusStep completedStatus = new StatusStep();
            completedStatus.setName("COMPLETED");

            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(reportRepository.findById(2L)).thenReturn(Optional.of(mockFound));
            when(statusStepRepository.findByName("COMPLETED")).thenReturn(Optional.of(completedStatus));
            when(reportRepository.save(any(Report.class))).thenReturn(mockReport).thenReturn(mockFound);
            when(reportMapper.toResponse(mockFound)).thenReturn(mockReportResponse);

            reportService.linkReports(1L, 2L);

            assertTrue(mockReport.isClosed());
            assertTrue(mockFound.isClosed());
            assertTrue(mockReport.getStatusSteps().contains(completedStatus));
            verify(reportRepository, times(2)).save(any(Report.class));
        }

        /**
         * Regra de validação no Match:
         * A ordem importa. O parâmetro 1 TEM que ser um Report do tipo LOST.
         */
        @Test
        @DisplayName("linkReports should throw exception if first report is not LOST")
        void linkReports_Error_FirstNotLost() {
            mockReport.setTypeReport(TypeReport.FOUND);
            Report mockFound = Report.builder().id(2L).typeReport(TypeReport.FOUND).build();

            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(reportRepository.findById(2L)).thenReturn(Optional.of(mockFound));

            assertThrows(ReportStatusTransitionException.class, () -> reportService.linkReports(1L, 2L));
        }

        /**
         * Regra de validação no Match:
         * Proteção contra a ausência da flag "COMPLETED" no banco de dados.
         */
        @Test
        @DisplayName("linkReports should throw exception if 'COMPLETED' status is not configured")
        void linkReports_Error_CompletedStatusNotFound() {
            Report mockFound = Report.builder().id(2L).typeReport(TypeReport.FOUND).isClosed(false).build();

            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(reportRepository.findById(2L)).thenReturn(Optional.of(mockFound));
            when(statusStepRepository.findByName("COMPLETED")).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> reportService.linkReports(1L, 2L));
        }
    }
}