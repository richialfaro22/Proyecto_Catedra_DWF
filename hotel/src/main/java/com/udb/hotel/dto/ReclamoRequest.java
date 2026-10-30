package com.udb.hotel.dto;

import com.udb.hotel.model.TipoReclamo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Reclamo o solicitud. Un HUESPED lo registra a su nombre; el personal (ADMIN/RECEPCIONISTA)
 * debe indicar {@code huespedId}.
 */
public record ReclamoRequest(
        @NotNull(message = "El tipo es obligatorio")
        TipoReclamo tipo,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(min = 5, max = 120, message = "El asunto debe tener entre 5 y 120 caracteres")
        String asunto,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
        String descripcion,

        @Positive(message = "La reserva no es válida")
        Integer reservaId,

        @Positive(message = "El huésped no es válido")
        Integer huespedId) {
}
