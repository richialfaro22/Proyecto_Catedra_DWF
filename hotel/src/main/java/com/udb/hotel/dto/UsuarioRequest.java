package com.udb.hotel.dto;

import com.udb.hotel.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(min = 3, max = 30, message = "El usuario debe tener entre 3 y 30 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                 message = "El usuario solo puede tener letras, números, punto, guion y guion bajo")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol) {
}
