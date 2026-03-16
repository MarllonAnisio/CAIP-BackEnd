package org.marllon.caip.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.marllon.caip.model.constants.TypeReport;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor

@Data

@Getter
@Setter

@Entity(name = "tb_report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "The title cannot be empty.")
    @Column(nullable = false, length = 130)
    private String title;

    @NotBlank(message = "The typeReport cannot be empty.")
    @Enumerated
    @Column(nullable = false, name = "type_report")
    private TypeReport typeReport;

    @Column(nullable = false, name = "is_closed")
    private boolean isClosed = false;

    @NotBlank(message = "The description cannot be empty.")
    @Column(nullable = false, name = "description")
    private String description;

    @Embedded
    @Column(name = "position")
    private Position position;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "report_status_step",
            joinColumns =  @JoinColumn(name = "fk_report_id"),
            inverseJoinColumns = @JoinColumn(name = "fk_status_step_id")
    )
    private List<StatusStep> statusSteps;
}
