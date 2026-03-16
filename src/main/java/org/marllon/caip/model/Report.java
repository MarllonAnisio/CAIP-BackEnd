package org.marllon.caip.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.marllon.caip.model.constants.TypeReport;

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
}
