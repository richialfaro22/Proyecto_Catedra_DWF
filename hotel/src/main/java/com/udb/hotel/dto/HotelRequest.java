package com.udb.hotel.dto;

import com.udb.hotel.validation.Patrones;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record HotelRequest(
        @NotBlank(message = "El nombre del hotel es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        String nombreHotel,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(min = 2, max = 100, message = "La ciudad debe tener entre 2 y 100 caracteres")
        @Pattern(regexp = Patrones.NOMBRE, message = "La ciudad " + Patrones.MSG_NOMBRE)
        String ciudad,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
        String direccion,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = Patrones.TELEFONO, message = Patrones.MSG_TELEFONO)
        String telefono,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres")
        String email,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        String descripcion,

        @NotNull(message = "Las estrellas son obligatorias")
        @Min(value = 1, message = "Las estrellas deben estar entre 1 y 5")
        @Max(value = 5, message = "Las estrellas deben estar entre 1 y 5")
        Integer estrellas,

        @NotNull(message = "Las horas de cancelación gratuita son obligatorias")
        @Min(value = 0, message = "Las horas de cancelación gratuita no pueden ser negativas")
        @Max(value = 720, message = "Las horas de cancelación gratuita no pueden superar 720 (30 días)")
        Integer horasCancelacionGratis,

        @NotNull(message = "El porcentaje de penalización es obligatorio")
        @DecimalMin(value = "0.00", message = "El porcentaje de penalización no puede ser negativo")
        @DecimalMax(value = "100.00", message = "El porcentaje de penalización no puede superar 100")
        @Digits(integer = 3, fraction = 2, message = "El porcentaje admite como máximo 2 decimales")
        BigDecimal porcentajePenalizacion,

        @NotNull(message = "El estado del hotel es obligatorio")
        @Positive(message = "El estado no es válido")
        Integer estadoId,

        @Positive(message = "El empleado no es válido")
        Integer empleadoId) {
}
