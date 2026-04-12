package org.marllon.caip.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.response.ReportResponse;
import org.marllon.caip.model.Report;
import org.marllon.caip.model.User;
import org.marllon.caip.repository.ReportRepository;
import org.marllon.caip.repository.StatusStepRepository;
import org.marllon.caip.repository.UserRepository;
import org.marllon.caip.service.mapper.ReportMapper;
import org.marllon.caip.service.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final StatusStepRepository statusStepRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public ReportResponse findById(Long id) throws Exception {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new Exception("Report not found with id: " + id));

        return reportMapper.toResponse(report);
    }
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_STUDANT') or hasRole('ROLE_LIBRARIAN') or hasRole('ADMIN')")
    public List<ReportResponse> findMyReports() {
        User me = getAuthenticatedUser();

        return reportRepository.findAllByAudit_CreatedByAndIsClosedFalse(me)
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_STUDANT') or hasRole('ROLE_LIBRARIAN') or hasRole('ADMIN')")
    public List<ReportResponse> findMyActiveReports() {
        User me = getAuthenticatedUser();
        return reportRepository.findAllByAudit_CreatedBy(me)
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN','ADMIN')")
    public List<ReportResponse> findAllForStaff() {
        return reportRepository.findAll()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN','ADMIN')")
    public List<ReportResponse> findAllActive() {
        return reportRepository.findAllByIsClosedIsFalse()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN','ADMIN')")
    public List<ReportResponse> findByStatus() throws Exception {
        return reportRepository.findAllByIsClosedIsTrue()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }


    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        String registration = auth.getName();
        return userRepository.findByRegistration(registration)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

}
