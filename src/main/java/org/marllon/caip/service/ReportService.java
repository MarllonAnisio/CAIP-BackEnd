package org.marllon.caip.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.request.ReportRequest;
import org.marllon.caip.dto.response.ReportResponse;
import org.marllon.caip.exception.auth_exceptions.UnauthorizedException;
import org.marllon.caip.exception.reports_exceptions.ReportStatusTransitionException;
import org.marllon.caip.exception.reports_exceptions.StatusConfigurationException;
import org.marllon.caip.exception.user_exceptions.IllegalUserActionException;
import org.marllon.caip.model.Location;
import org.marllon.caip.model.Report;
import org.marllon.caip.model.StatusStep;
import org.marllon.caip.model.User;
import org.marllon.caip.model.constants.TypeReport;
import org.marllon.caip.repository.ReportRepository;
import org.marllon.caip.repository.StatusStepRepository;
import org.marllon.caip.repository.UserRepository;
import org.marllon.caip.service.mapper.ReportMapper;
import org.marllon.caip.service.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserMapper userMapper;
    private final StatusStepRepository statusStepRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;

    @Transactional(readOnly = true)
    @Cacheable(value = "tb_report", key = "#id")
    public ReportResponse findById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + id));
        return reportMapper.toResponse(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findMyReports() {
        User me = getAuthenticatedUser();

        return reportRepository.findAllByAudit_CreatedBy(me)
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findMyActiveReports() {
        User me = getAuthenticatedUser();
        return reportRepository.findAllByAudit_CreatedByAndIsClosedFalse(me)
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
        return reportRepository.findAllByIsClosedIsFalse()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<ReportResponse> findClosedReports() {
        return reportRepository.findAllByIsClosedIsTrue()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }
    @Transactional
    public ReportResponse save(ReportRequest report) {
        User me = getAuthenticatedUser();
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
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Relatório não encontrado"));

        User me = getAuthenticatedUser();
        boolean isOwner = report.getFoundBy()
                .getId()
                .equals(me.getId());

        if (!isOwner) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isStaff = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN") || a.getAuthority().equals("ROLE_ADMIN"));

            if (!isStaff) {
                log.warn("Usuário {} tentou deletar o relatório {} sem permissão", me.getRegistration(), reportId);
                throw new AccessDeniedException("Você só tem permissão para deletar os seus próprios relatórios.");
            }
        }

        reportRepository.delete(report);
        log.info("Relatório {} deletado (soft delete) pelo usuário {}", reportId, me.getRegistration());
    }

    @Transactional
    @CacheEvict(value = "tb_report", key = "#reportId")
    public void hardDeleteReport(Long reportId) {
        reportRepository.hardDeleteById(reportId);
    }

    @Transactional
    public ReportResponse update(Long id, ReportRequest request) {

        Report existingReport = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Relatório não encontrado com o ID: " + id));

        if (existingReport.isClosed()) {
            throw ReportStatusTransitionException.cannotModifyTerminalStatus(id, "CONCLUÍDO");  // ✅
        }

        Location location = locationService.findEntityById(request.locationId());
        reportMapper.updateEntity(existingReport, request);
        existingReport.setLocation(location);

        Report savedReport = reportRepository.save(existingReport);
        return reportMapper.toResponse(savedReport);
    }

    public void closeReport(Long reportId) {
        Report existingReport = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Relatório não encontrado com o ID: " + reportId));

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
                .orElseThrow(() -> new EntityNotFoundException("Report LOST not found with id: " + perdidoId));

        Report encontrado = reportRepository.findById(encontradoId)
                .orElseThrow(() -> new EntityNotFoundException("Report FOUND not found with id: " + encontradoId));

        if (perdido.getTypeReport() != TypeReport.LOST) {
            throw ReportStatusTransitionException.incompatibleReportForMatch(perdidoId, "LOST");  // ✅
        }

        if (encontrado.getTypeReport() != TypeReport.FOUND) {
            throw ReportStatusTransitionException.incompatibleReportForMatch(encontradoId, "FOUND");  // ✅
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
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("Usuário não autenticado");
        }
        String registration = auth.getName();
        return userRepository.findByRegistration(registration)
                .orElseThrow(() -> new IllegalUserActionException("Usuário autenticado não encontrado"));
    }

}
