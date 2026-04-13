package org.marllon.caip.dto.response.summarys;

import java.time.Instant;

public record ReportResponseSummary(

        Long id,
        String title,
        Instant createdAt

) {}
