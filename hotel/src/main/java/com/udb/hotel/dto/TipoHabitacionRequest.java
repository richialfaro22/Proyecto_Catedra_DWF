package com.udb.hotel.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TipoHabitacionRequest(
        @NotBlank(message = "El nombre del tipo es obligatorio")
        @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
        String tipoHabitacion,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "1.00", message = "El precio debe ser mayor o igual a $1.00")
        @DecimalMax(value = "99999.99", message = "El precio es demasiado alto")
        @Digits(integer = 5, fraction = 2, message = "El precio admite como máximo 2 decimales")
        BigDecimal precio,

        @NotNull(message = "La cantidad de personas es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1 persona")
        @Max(value = 10, message = "La cantidad máxima es 10 personas")
        Integer cantidadPersonas,

        @Size(max = 300, message = "Los servicios incluidos no pueden superar 300 caracteres")
        String serviciosIncluidos) {
}
