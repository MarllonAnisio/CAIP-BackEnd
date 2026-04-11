package org.marllon.caip.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.marllon.caip.model.Location;
import org.marllon.caip.model.Position;
import org.marllon.caip.model.Report;
import org.marllon.caip.model.User;
import org.marllon.caip.model.constants.TypeReport;

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
){
    public Report toEntity(User author, Location location) {

        Position positionEntity = null;
        if (this.position != null && this.position.latitude() != null && this.position.longitude() != null) {
            positionEntity = new Position();
            positionEntity.setLatidude(this.position.latitude());
            positionEntity.setLongitude(this.position.latitude());
        }

        return Report.builder()
                .title(this.title)
                .description(this.description)
                .typeReport(TypeReport.valueOf(this.typeReport))
                .imageUrl(this.imageUrl)
                .position(positionEntity)
                .location(location)
                .foundBy(author)


                .isClosed(false)
                .date(Instant.now())
                .build();
    }
}
