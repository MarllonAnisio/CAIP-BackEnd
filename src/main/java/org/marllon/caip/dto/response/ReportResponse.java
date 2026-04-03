package org.marllon.caip.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReportResponse(
         Long id,
         String title,
         String type,
         LocationResponse location,
         PositionResponse relationalPosition,
         UserSummaryDTO collectedBy,
         Boolean isFinish,
         UserSummaryDTO createdBy,
         LocalDateTime createdAt,
         List<StatusStep>statusSteps,
         String imageUrl
) {
}
