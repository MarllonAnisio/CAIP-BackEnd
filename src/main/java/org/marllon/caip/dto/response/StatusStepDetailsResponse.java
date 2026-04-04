package org.marllon.caip.dto.response;

import org.marllon.caip.dto.response.summarys.ReportResponseSummary;

import java.util.List;

public record StatusStepDetailsResponse(
        Long id,
        String name,
        String color,
        List<ReportResponseSummary> reports

) {}
