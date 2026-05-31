package org.marllon.caip.domains.user.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.marllon.caip.domains.user.entity.constants.Role;
import org.marllon.caip.domains.report.entity.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity(name = "tb_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The registration cannot be empty.")
    @Column(unique = true, nullable = false, length = 12, name = "registration")
    private String registration;

    @NotBlank(message = "The name cannot be empty.")
    @Column(nullable = false, length = 50, name = "name")
    private String name;


    @NotBlank(message = "The password cannot be empty.")
    @Column(nullable = false, name = "password")
    private String password;


    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;

    @NotBlank(message = "The name cannot be empty.")
    @Column(nullable = false, name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "foundBy")
    @JsonManagedReference("report-foundBy")
    private List<Report> reportsFound = new ArrayList<>();;

    @OneToMany(mappedBy = "collectedBy")
    @JsonManagedReference("report-collectedBy")
    private List<Report> reportsCollected = new ArrayList<>();

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", registration='" + registration + '\'' +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(registration, user.registration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(registration);
    }
}
