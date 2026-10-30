package com.udb.hotel.dto;

import com.udb.hotel.model.EstadoReclamo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReclamoRespuestaRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoReclamo estado,

        @NotBlank(message = "La respuesta es obligatoria")
        @Size(min = 5, max = 1000, message = "La respuesta debe tener entre 5 y 1000 caracteres")
        String respuesta) {
}
