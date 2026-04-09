package org.marllon.caip.dto.response.summarys;

import java.time.Instant;
import java.time.LocalDateTime;

public record ReportResponseSummary(

        Long id,
        String title,
        Instant createdAt

) {}
