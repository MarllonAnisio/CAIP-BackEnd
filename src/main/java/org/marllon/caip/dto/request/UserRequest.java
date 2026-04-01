package org.marllon.caip.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(@NotBlank(message = "Username é obrigatório")
                          String username,

                          @NotBlank(message = "Matrícula é obrigatória")
                          String registration,

                          @NotBlank(message = "Senha é obrigatória")
                          @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
                          String password) {}
