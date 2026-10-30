package com.udb.hotel.dto;

import com.udb.hotel.model.Habitacion;

import java.math.BigDecimal;

public record HabitacionResponse(Integer id, Integer hotelId, String nombreHotel, Integer tipoId,
                                 String tipoHabitacion, Integer cantidadPersonas, String numeroHabitacion,
                                 Integer numPiso, BigDecimal precioBase, String descripcion, String estado) {

    public static HabitacionResponse de(Habitacion h) {
        return new HabitacionResponse(h.getId(), h.getHotel().getId(), h.getHotel().getNombreHotel(),
                h.getTipo().getId(), h.getTipo().getTipoHabitacion(), h.getTipo().getCantidadPersonas(),
                h.getNumeroHabitacion(), h.getNumPiso(), h.getPrecioBase(), h.getDescripcion(), h.getEstado().name());
    }
}
