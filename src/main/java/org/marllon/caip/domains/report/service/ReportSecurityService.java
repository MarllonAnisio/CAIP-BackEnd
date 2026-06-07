package org.marllon.caip.domains.report.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.core.security.SecurityContextService;
import org.marllon.caip.domains.report.repository.ReportRepository;
import org.marllon.caip.domains.user.entity.User;
import org.springframework.stereotype.Service;

@Service("reportSecurityService")
@RequiredArgsConstructor
public class ReportSecurityService {
    private final ReportRepository reportRepository;
    private final SecurityContextService securityContextService;

    public boolean isOwner(Long reportId) {
        User currentUser = securityContextService.getAuthenticatedUser();
        return reportRepository.findById(reportId)
                .map(report -> report.getAudit().getCreatedBy().getId().equals(currentUser.getId()))
                .orElse(false);
    }
}
