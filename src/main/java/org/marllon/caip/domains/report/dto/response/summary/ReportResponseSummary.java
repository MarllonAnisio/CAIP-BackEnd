package org.marllon.caip.domains.report.dto.response.summary;

import java.time.Instant;

public record ReportResponseSummary(

        Long id,
        String title,
        Instant createdAt

) {}
