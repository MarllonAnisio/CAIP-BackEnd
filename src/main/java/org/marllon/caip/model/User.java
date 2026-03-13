package org.marllon.caip.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity(name = "tb_users")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(unique = true, nullable = false, length = 12, name = "registration")
    private String registration;

    @NotNull
    @Column(nullable = false, length = 50, name = "name")
    private String name;


    @NotNull
    @Column(nullable = false, name = "password")
    private String password;

    @NotNull
    @Column(nullable = false, name = "is_active")
    private boolean isActive = true;

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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", registration='" + registration + '\'' +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
