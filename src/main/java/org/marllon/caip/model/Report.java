package org.marllon.caip.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.marllon.caip.model.audit.BaseAuditableEntity;
import org.marllon.caip.model.constants.TypeReport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity(name = "tb_report")
public class Report  extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * titulo do report, nome do item perdido.
     * */
    @NotBlank(message = "The title cannot be empty.")
    @Column(nullable = false, length = 130)
    private String title;

    /**
     * data em que o report foi feito.
     * */
    @NotNull(message = "The date cannot be null.")
    @PastOrPresent(message = "The report date cannot be in the future.")
    @Column(nullable = false, name = "date_report")
    private Instant date;

    /**
     * data em que o item foi reclamado.
     * */
    @Column(name = "date_reclamed")
    private Instant dateReclamed;

    /**
     * tipo do report, de qual natureza foi feito, se perdido ou achado
     * */
    @NotNull(message = "The type report cannot be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "type_report")
    private TypeReport typeReport;

    /**
     * status do report, se ele foi fechado.
     * */
    @Builder.Default
    @Column(nullable = false, name = "is_closed")
    private boolean isClosed = false;

    /**
     * descrição do item perdido.
     * */
    @NotBlank(message = "The description cannot be empty.")
    @Column(nullable = false, name = "description")
    private String description;

    /**
     * posição do item perdido, localidade onde foi encontrado. (não obrigatorio)
     * */
    @Embedded
    private Position position;

    @ManyToOne
    @JoinColumn(name = "fk_location_id", nullable = false)
    private Location location;

    /**
     * Imagem do item perdido.
     * */
    @Column(nullable = false, name = "image_url")
    private String imageUrl;


    /**
     * Status do report.
     * */
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "report_status_step",
            joinColumns =  @JoinColumn(name = "fk_report_id"),
            inverseJoinColumns = @JoinColumn(name = "fk_status_step_id")
    )
    private List<StatusStep> statusSteps = new ArrayList<>();


    /**
     * id do usuario que encontrou o item perdido.
     * */
    @ManyToOne
    @JoinColumn(name = "fk_found_by")
    @JsonBackReference("report-foundBy")
    private User foundBy;

    /**
     * id do usuario que reclamou o item perdido ou que recuperou o item perdido.
     * */
    @ManyToOne
    @JoinColumn(name = "fk_collected_by")
    @JsonBackReference("report-collectedBy")
    private User collectedBy;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(id, report.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", dateReclamed=" + dateReclamed +
                ", typeReport=" + typeReport +
                ", isClosed=" + isClosed +
                ", description='" + description + '\'' +
                ", position=" + position +
                ", location=" + location +
                ", imageUrl='" + imageUrl + '\'' +
                ", statusSteps=" + statusSteps +
                ", foundBy=" + foundBy +
                ", collectedBy=" + collectedBy +
                '}';
    }
}
