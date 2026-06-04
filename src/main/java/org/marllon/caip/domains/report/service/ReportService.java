package org.marllon.caip.domains.report.service;


import lombok.RequiredArgsConstructor;
import org.marllon.caip.domains.auth.service.AuthService;
import org.marllon.caip.domains.location.service.LocationService;
import org.marllon.caip.domains.report.dto.request.ReportRequest;
import org.marllon.caip.domains.report.dto.response.ReportResponse;
import org.marllon.caip.domains.report.exceptions.ReportNotFoundException;
import org.marllon.caip.domains.report.exceptions.ReportStatusTransitionException;
import org.marllon.caip.domains.report.exceptions.StatusConfigurationException;
import org.marllon.caip.domains.location.entity.Location;
import org.marllon.caip.domains.report.entity.Report;
import org.marllon.caip.domains.report.entity.StatusStep;
import org.marllon.caip.domains.user.entity.User;
import org.marllon.caip.domains.report.entity.constants.TypeReport;
import org.marllon.caip.domains.report.repository.ReportRepository;
import org.marllon.caip.domains.report.repository.StatusStepRepository;
import org.marllon.caip.domains.report.mapper.ReportMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final StatusStepRepository statusStepRepository;
    private final LocationService locationService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    @Cacheable(value = "tb_report", key = "#id")
    public ReportResponse findById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with id: " + id));
        return reportMapper.toResponse(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findMyReports() {
        User me = authService.getAuthenticatedUser();

        return reportRepository.findAllByCreator(me)
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findMyActiveReports() {
        User me = authService.getAuthenticatedUser();
        return reportRepository.findActiveReportsByCreator(me)
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findAllForStaff() {
        return reportRepository.findAll()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findAllActive() {
        return reportRepository.findAllByIsClosedFalse()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<ReportResponse> findClosedReports() {
        return reportRepository.findAllByIsClosedTrue()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }
    @Transactional
    public ReportResponse save(ReportRequest report) {
        User me = authService.getAuthenticatedUser();
        Location location = locationService.findEntityById(report.locationId());

        Report reportConverted = report.toEntity(me, location);

        StatusStep status = statusStepRepository.findByName(reportConverted.getTypeReport().name())
                .orElseThrow(() -> new StatusConfigurationException("Atenção: O status inicial '" + reportConverted.getTypeReport().name() + "' não está cadastrado no sistema."));
        reportConverted.setStatusSteps(new ArrayList<>(List.of(status)));

        log.info("Saving report {}", reportConverted);

        var savedReport = reportRepository.save(reportConverted);
        return reportMapper.toResponse(savedReport);

    }
    @Transactional
    @CacheEvict(value = "tb_report", key = "#reportId")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or @reportSecurity.isOwner(#reportId)")
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Relatório não encontrado"));

        reportRepository.delete(report);
        log.info("Relatório {} deletado (soft delete) pelo usuário {}",
                reportId, authService.getAuthenticatedUser().getRegistration());
    }

    @Transactional
    @CacheEvict(value = "tb_report", key = "#reportId")
    public void hardDeleteReport(Long reportId) {
        reportRepository.hardDeleteById(reportId);
    }

    @Transactional
    public ReportResponse update(Long id, ReportRequest request) {

        Report existingReport = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("Relatório não encontrado com o ID: " + id));

        if (existingReport.isClosed()) {
            throw ReportStatusTransitionException.cannotModifyTerminalStatus(id, "CONCLUÍDO");
        }

        Location location = locationService.findEntityById(request.locationId());
        reportMapper.updateEntity(existingReport, request);
        existingReport.setLocation(location);

        Report savedReport = reportRepository.save(existingReport);
        return reportMapper.toResponse(savedReport);
    }

    public void closeReport(Long reportId) {
        Report existingReport = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Relatório não encontrado com o ID: " + reportId));

        if (existingReport.isClosed()) {
            throw ReportStatusTransitionException.cannotCloseAlreadyClosed(reportId);
        }

        existingReport.setClosed(true);
        reportRepository.save(existingReport);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tb_report", key = "#perdidoId"),
            @CacheEvict(value = "tb_report", key = "#encontradoId")
    })
    public ReportResponse linkReports(Long perdidoId, Long encontradoId) {

        Report perdido = reportRepository.findById(perdidoId)
                .orElseThrow(() -> new ReportNotFoundException("Report LOST not found with id: " + perdidoId));

        Report encontrado = reportRepository.findById(encontradoId)
                .orElseThrow(() -> new ReportNotFoundException("Report FOUND not found with id: " + encontradoId));

        if (perdido.getTypeReport() != TypeReport.LOST) {
            throw ReportStatusTransitionException.incompatibleReportForMatch(perdidoId, "LOST");
        }

        if (encontrado.getTypeReport() != TypeReport.FOUND) {
            throw ReportStatusTransitionException.incompatibleReportForMatch(encontradoId, "FOUND");
        }

        StatusStep concluido = statusStepRepository.findByName("COMPLETED")
                .orElseThrow(() -> new IllegalStateException("Status CONCLUIDO não mapeado no banco"));

        perdido.getStatusSteps().add(concluido);
        perdido.setClosed(true);

        encontrado.getStatusSteps().add(concluido);
        encontrado.setClosed(true);

        log.info("Match realizado! Report Perdido [{}] vinculado ao Encontrado [{}]", perdidoId, encontradoId);

        reportRepository.save(perdido);
        Report salvo = reportRepository.save(encontrado);

        return reportMapper.toResponse(salvo);
    }


}
