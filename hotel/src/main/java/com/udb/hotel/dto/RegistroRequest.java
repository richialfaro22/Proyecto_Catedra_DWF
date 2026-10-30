package com.udb.hotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registro público: crea la cuenta (rol HUESPED) y su ficha de huésped. El correo de la cuenta es el del huésped. */
public record RegistroRequest(
        @NotBlank(message = "El alias es obligatorio")
        @Size(min = 3, max = 50, message = "El alias debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                 message = "El alias solo puede tener letras, números, punto, guion y guion bajo")
        String alias,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
        String contrasena,

        @NotNull(message = "Los datos personales son obligatorios")
        @Valid
        HuespedRequest huesped) {
}
