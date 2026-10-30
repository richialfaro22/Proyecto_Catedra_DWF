package com.udb.hotel.dto;

import com.udb.hotel.validation.Patrones;
import jakarta.validation.constraints.*;

public record EmpleadoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        @Pattern(regexp = Patrones.NOMBRE, message = "El nombre " + Patrones.MSG_NOMBRE)
        String nombreEmpleado,

        @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
        String direccion,

        @Pattern(regexp = Patrones.TELEFONO_OPCIONAL, message = Patrones.MSG_TELEFONO)
        String telefono,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres")
        String email,

        @NotBlank(message = "El tipo de empleado es obligatorio")
        @Size(max = 50, message = "El tipo de empleado no puede superar 50 caracteres")
        @Pattern(regexp = Patrones.NOMBRE, message = "El tipo de empleado " + Patrones.MSG_NOMBRE)
        String tipoEmpleado,

        @NotNull(message = "El estado del empleado es obligatorio")
        @Positive(message = "El estado no es válido")
        Integer estadoId) {
}
