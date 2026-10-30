package com.udb.hotel.dto;

import com.udb.hotel.model.EstadoHabitacion;
import com.udb.hotel.validation.Patrones;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record HabitacionRequest(
        @NotNull(message = "El hotel es obligatorio")
        @Positive(message = "El hotel no es válido")
        Integer hotelId,

        @NotNull(message = "El tipo de habitación es obligatorio")
        @Positive(message = "El tipo de habitación no es válido")
        Integer tipoId,

        @NotBlank(message = "El número de habitación es obligatorio")
        @Pattern(regexp = Patrones.NUMERO_HABITACION,
                 message = "El número de habitación debe ser numérico y puede terminar en una letra (ej. 101 o 12B)")
        String numeroHabitacion,

        @NotNull(message = "El piso es obligatorio")
        @Min(value = 0, message = "El piso no puede ser negativo")
        @Max(value = 200, message = "El piso no puede ser mayor a 200")
        Integer numPiso,

        @NotNull(message = "El precio base es obligatorio")
        @DecimalMin(value = "1.00", message = "El precio base debe ser mayor o igual a $1.00")
        @DecimalMax(value = "99999.99", message = "El precio base es demasiado alto")
        @Digits(integer = 5, fraction = 2, message = "El precio admite como máximo 2 decimales")
        BigDecimal precioBase,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        String descripcion,

        @NotNull(message = "El estado es obligatorio")
        EstadoHabitacion estado) {
}
