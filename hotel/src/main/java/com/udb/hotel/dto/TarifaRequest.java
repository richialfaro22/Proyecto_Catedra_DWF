package com.udb.hotel.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaRequest(
        @NotBlank(message = "El nombre de la tarifa es obligatorio")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombreTarifa,

        @NotNull(message = "El hotel es obligatorio")
        @Positive(message = "El hotel no es válido")
        Integer hotelId,

        @NotNull(message = "El tipo de habitación es obligatorio")
        @Positive(message = "El tipo de habitación no es válido")
        Integer tipoId,

        @NotNull(message = "El precio por noche es obligatorio")
        @DecimalMin(value = "1.00", message = "El precio por noche debe ser mayor o igual a $1.00")
        @DecimalMax(value = "99999.99", message = "El precio por noche es demasiado alto")
        @Digits(integer = 5, fraction = 2, message = "El precio admite como máximo 2 decimales")
        BigDecimal precioNoche,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @FutureOrPresent(message = "La fecha de inicio debe ser hoy o una fecha futura")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        @FutureOrPresent(message = "La fecha de fin debe ser hoy o una fecha futura")
        LocalDate fechaFin) {
}
