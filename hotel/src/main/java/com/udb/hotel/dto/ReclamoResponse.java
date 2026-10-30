package com.udb.hotel.dto;

import com.udb.hotel.model.Reclamo;

import java.time.LocalDateTime;

public record ReclamoResponse(Integer id, Integer huespedId, String nombreHuesped, Integer reservaId,
                              String codigoReserva, String tipo, String asunto, String descripcion, String estado,
                              String respuesta, LocalDateTime fechaReclamo, LocalDateTime fechaRespuesta,
                              String atendidoPor) {

    public static ReclamoResponse de(Reclamo c) {
        var r = c.getReserva();
        return new ReclamoResponse(c.getId(), c.getHuesped().getId(), c.getHuesped().getNombreHuesped(),
                r != null ? r.getId() : null, r != null ? r.getCodigo() : null, c.getTipo().name(), c.getAsunto(),
                c.getDescripcion(), c.getEstado().name(), c.getRespuesta(), c.getFechaReclamo(),
                c.getFechaRespuesta(), c.getAtendidoPor());
    }
}
