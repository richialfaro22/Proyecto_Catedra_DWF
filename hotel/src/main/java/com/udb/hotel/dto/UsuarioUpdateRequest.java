package com.udb.hotel.dto;

import com.udb.hotel.model.Rol;
import jakarta.validation.constraints.*;

/** La contraseña es opcional: si viene vacía o nula, se conserva la actual. */
public record UsuarioUpdateRequest(
        @NotBlank(message = "El alias es obligatorio")
        @Size(min = 3, max = 50, message = "El alias debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                 message = "El alias solo puede tener letras, números, punto, guion y guion bajo")
        String alias,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres")
        String email,

        @Pattern(regexp = "^$|^.{6,50}$", message = "La contraseña debe tener entre 6 y 50 caracteres")
        String contrasena,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @Positive(message = "El empleado no es válido")
        Integer empleadoId,

        @Positive(message = "El huésped no es válido")
        Integer huespedId) {
}
