package org.marllon.caip.domains.report.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.domains.auth.service.AuthService;
import org.marllon.caip.domains.report.repository.ReportRepository;
import org.marllon.caip.domains.user.entity.User;
import org.springframework.stereotype.Service;

@Service("reportSecurityService")
@RequiredArgsConstructor
public class ReportSecurityService {
    private final ReportRepository reportRepository;
    private final AuthService authService;

    public boolean isOwner(Long reportId) {
        User currentUser = authService.getAuthenticatedUser();
        return reportRepository.findById(reportId)
                .map(report -> report.getAudit().getCreatedBy().getId().equals(currentUser.getId()))
                .orElse(false);
    }
}
