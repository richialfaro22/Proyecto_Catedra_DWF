package com.udb.hotel.dto;

import jakarta.validation.constraints.NotBlank;

/** {@code usuario} puede ser el alias o el correo electrónico. */
public record LoginRequest(
        @NotBlank(message = "Ingresa tu usuario o correo") String usuario,
        @NotBlank(message = "La contraseña es obligatoria") String contrasena) {
}
