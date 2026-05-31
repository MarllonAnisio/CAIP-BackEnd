package org.marllon.caip.domains.report.dto.response;

import org.marllon.caip.domains.report.dto.response.summary.ReportResponseSummary;

import java.util.List;

public record StatusStepDetailsResponse(
        Long id,
        String name,
        String color,
        List<ReportResponseSummary> reports

) {}
