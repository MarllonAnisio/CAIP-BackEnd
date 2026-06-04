package org.marllon.caip.domains.report.entity;


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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.marllon.caip.core.database.audit.BaseAuditableEntity;
import org.marllon.caip.domains.location.entity.Location;
import org.marllon.caip.domains.location.entity.Position;
import org.marllon.caip.domains.user.entity.User;
import org.marllon.caip.domains.report.entity.constants.TypeReport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tb_report")
@SQLDelete(sql = "UPDATE tb_report SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Report extends BaseAuditableEntity {

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

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

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
    @ToString.Exclude
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
    @ToString.Exclude
    private List<StatusStep> statusSteps = new ArrayList<>();


    /**
     * id do usuario que encontrou o item perdido.
     * */
    @ManyToOne
    @JoinColumn(name = "fk_found_by")
    @JsonBackReference("report-foundBy")
    @ToString.Exclude
    private User foundBy;

    /**
     * id do usuario que reclamou o item perdido ou que recuperou o item perdido.
     * */
    @ManyToOne
    @JoinColumn(name = "fk_collected_by")
    @JsonBackReference("report-collectedBy")
    @ToString.Exclude
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
    
}