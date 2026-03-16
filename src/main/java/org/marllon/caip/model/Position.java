package org.marllon.caip.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Embeddable
public class Position {

    @Column(name = "position_x")
    private Integer x;

    @Column(name = "position_y")
    private Integer y;

}
