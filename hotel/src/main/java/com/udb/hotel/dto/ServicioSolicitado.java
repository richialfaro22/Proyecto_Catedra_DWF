package com.udb.hotel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ServicioSolicitado(
        @NotNull(message = "El servicio es obligatorio")
        @Positive(message = "El servicio no es válido")
        Integer servicioId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 20, message = "La cantidad máxima es 20")
        Integer cantidad) {
}
