package org.marllon.caip.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marllon.caip.domains.auth.service.AuthService;
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
    private AuthService authService;

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
                .isClosed(false)
                .statusSteps(new ArrayList<>(List.of(mockStatus)))
                .build();

        reportRequest = new ReportRequest("Casaco Preto", "Casaco de couro preto", "LOST", "http://image.com/casaco.jpg", 1L, null);
        mockReportResponse = mock(ReportResponse.class);
    }

    @Nested
    @DisplayName("Find Methods")
    class FindMethods {
        @Test
        @DisplayName("findById should return report when found")
        void findById_Success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(reportMapper.toResponse(mockReport)).thenReturn(mockReportResponse);

            ReportResponse response = reportService.findById(1L);

            assertNotNull(response);
            verify(reportRepository).findById(1L);
        }

        @Test
        @DisplayName("findById should throw exception when not found")
        void findById_Error_NotFound() {
            when(reportRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ReportNotFoundException.class, () -> reportService.findById(99L));
        }

        @Test
        @DisplayName("findMyReports should return user's reports")
        void findMyReports_Success() {
            when(authService.getAuthenticatedUser()).thenReturn(mockUser);
            when(reportRepository.findAllByCreator(mockUser)).thenReturn(List.of(mockReport));

            List<ReportResponse> result = reportService.findMyReports();

            assertFalse(result.isEmpty());
            verify(reportRepository).findAllByCreator(mockUser);
        }
    }

    @Nested
    @DisplayName("Save Method")
    class SaveMethod {
        @Test
        @DisplayName("should save new report successfully")
        void save_Success() {
            when(authService.getAuthenticatedUser()).thenReturn(mockUser);
            when(locationService.findEntityById(1L)).thenReturn(mockLocation);
            when(statusStepRepository.findByName("LOST")).thenReturn(Optional.of(mockStatus));
            when(reportRepository.save(any(Report.class))).thenReturn(mockReport);
            when(reportMapper.toResponse(mockReport)).thenReturn(mockReportResponse);

            ReportResponse response = reportService.save(reportRequest);

            assertNotNull(response);
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("should throw exception if initial status is not configured")
        void save_Error_StatusNotFound() {
            when(authService.getAuthenticatedUser()).thenReturn(mockUser);
            when(locationService.findEntityById(1L)).thenReturn(mockLocation);
            when(statusStepRepository.findByName("LOST")).thenReturn(Optional.empty());

            assertThrows(StatusConfigurationException.class, () -> reportService.save(reportRequest));
        }

        @Test
        @DisplayName("should throw exception if location is not found")
        void save_Error_LocationNotFound() {
            when(authService.getAuthenticatedUser()).thenReturn(mockUser);
            when(locationService.findEntityById(99L)).thenThrow(new LocalNaoEncontradoException("Location not found"));

            ReportRequest requestWithInvalidLocation = new ReportRequest("Title", "Desc", "LOST", "url", 99L, null);

            assertThrows(LocalNaoEncontradoException.class, () -> reportService.save(requestWithInvalidLocation));
        }
    }

    @Nested
    @DisplayName("Update Method")
    class UpdateMethod {
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

        @Test
        @DisplayName("should throw exception when updating a closed report")
        void update_Error_ReportIsClosed() {
            mockReport.setClosed(true);
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));

            assertThrows(ReportStatusTransitionException.class, () -> reportService.update(1L, reportRequest));
        }

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
        @Test
        @DisplayName("deleteReport should soft delete successfully")
        void deleteReport_Success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(authService.getAuthenticatedUser()).thenReturn(mockUser);
            
            reportService.deleteReport(1L);

            verify(reportRepository).delete(mockReport);
        }

        @Test
        @DisplayName("deleteReport should throw exception if report not found")
        void deleteReport_Error_NotFound() {
            when(reportRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ReportNotFoundException.class, () -> reportService.deleteReport(99L));
        }

        @Test
        @DisplayName("hardDeleteReport should call repository method")
        void hardDeleteReport_Success() {
            reportService.hardDeleteReport(1L);
            verify(reportRepository).hardDeleteById(1L);
        }
    }

    @Nested
    @DisplayName("Business Logic Methods")
    class BusinessLogic {
        @Test
        @DisplayName("closeReport should set isClosed to true")
        void closeReport_Success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(reportRepository.save(any(Report.class))).thenReturn(mockReport);

            reportService.closeReport(1L);

            assertTrue(mockReport.isClosed());
            verify(reportRepository).save(mockReport);
        }

        @Test
        @DisplayName("closeReport should throw exception if already closed")
        void closeReport_Error_AlreadyClosed() {
            mockReport.setClosed(true);
            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));

            assertThrows(ReportStatusTransitionException.class, () -> reportService.closeReport(1L));
        }

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

        @Test
        @DisplayName("linkReports should throw exception if first report is not LOST")
        void linkReports_Error_FirstNotLost() {
            mockReport.setTypeReport(TypeReport.FOUND);
            Report mockFound = Report.builder().id(2L).typeReport(TypeReport.FOUND).build();

            when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
            when(reportRepository.findById(2L)).thenReturn(Optional.of(mockFound));

            assertThrows(ReportStatusTransitionException.class, () -> reportService.linkReports(1L, 2L));
        }

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