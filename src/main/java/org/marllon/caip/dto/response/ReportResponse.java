package org.marllon.caip.dto.response;

import org.marllon.caip.dto.response.summarys.UserResponseSummary;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record ReportResponse(
         Long id,
         String title,
         String type,
         LocationResponse location,
         PositionResponse relationalPosition,
         UserResponseSummary collectedBy,
         Boolean isFinish,
         UserResponseSummary createdBy,
         Instant createdAt,
         List<StatusStepDetailsResponse>statusSteps,
         String imageUrl
) {
}
