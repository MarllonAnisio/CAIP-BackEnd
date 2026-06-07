package org.marllon.caip.domains.report.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.marllon.caip.domains.location.dto.request.PositionRequest;
import org.marllon.caip.domains.location.entity.Location;
import org.marllon.caip.domains.location.entity.Position;
import org.marllon.caip.domains.report.entity.Report;
import org.marllon.caip.domains.user.entity.User;
import org.marllon.caip.domains.report.entity.constants.TypeReport;

import java.time.Instant;

public record ReportRequest(

        @NotBlank(message = "The title cannot be empty.")
        String title,

        @NotBlank(message = "The description cannot be empty.")
        String description,

        @NotBlank(message = "The type of report cannot be empty.")
        String typeReport,

        @NotBlank(message = "The image URL cannot be empty.")
        String imageUrl,

        @NotNull(message = "The location ID cannot be null.")
        Long locationId,

        PositionRequest position

){}
