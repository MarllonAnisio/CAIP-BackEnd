package org.marllon.caip.domains.report.dto.response;

import org.marllon.caip.domains.location.dto.response.LocationResponse;
import org.marllon.caip.domains.location.dto.response.PositionResponse;
import org.marllon.caip.domains.user.dto.response.summary.UserResponseSummary;

import java.time.Instant;
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
