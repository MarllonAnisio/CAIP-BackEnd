package org.marllon.caip.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity(name = "tb_status_step")
public class StatusStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The name cannot be empty.")
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @NotBlank(message = "The description cannot be empty.")
    @Column(nullable = false, length = 255, name = "description")
    private String description;

    @NotBlank(message = "The color cannot be empty.")
    @Column(nullable = false, name = "color")
    private String color;

    @ManyToMany(mappedBy = "statusSteps")
    @JsonIgnore
    private List<Report> reports;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StatusStep that = (StatusStep) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "StatusStep{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
