package com.udb.hotel.dto;

import java.math.BigDecimal;

/** Lo que pasaría si se cancela la reserva en este momento. */
public record CotizacionCancelacionResponse(
        Integer reservaId,
        String codigo,
        BigDecimal total,
        BigDecimal totalPagado,
        boolean dentroDePlazoGratis,
        long horasParaEntrada,
        Integer horasCancelacionGratis,
        BigDecimal porcentajePenalizacion,
        BigDecimal cargo,
        BigDecimal reembolso,
        String mensaje) {
}
