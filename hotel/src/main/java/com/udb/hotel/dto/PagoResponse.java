package com.udb.hotel.dto;

import com.udb.hotel.model.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(Integer id, Integer reservaId, String codigoReserva, String nombreHuesped,
                           String nombreHotel, BigDecimal totalPagar, LocalDateTime fechaPago, String metodoPago,
                           String estado, String referencia, String ultimosDigitos, String observacion) {

    public static PagoResponse de(Pago p) {
        var r = p.getReserva();
        return new PagoResponse(p.getId(), r.getId(), r.getCodigo(), r.getHuesped().getNombreHuesped(),
                r.getHotel().getNombreHotel(), p.getTotalPagar(), p.getFechaPago(), p.getMetodoPago().name(),
                p.getEstado().name(), p.getReferencia(), p.getUltimosDigitos(), p.getObservacion());
    }
}
