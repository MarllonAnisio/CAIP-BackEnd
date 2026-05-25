package org.marllon.caip.model.constants;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    ADMIN("ADMIN"),
    STUDENT("STUDENT"),
    LIBRARIAN("LIBRARIAN");

    private final String name;

    public static boolean exists(String name){
        for(Role role : Role.values()){
            if(role.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
