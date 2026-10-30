package com.udb.hotel.dto;

import com.udb.hotel.model.TipoHabitacion;

import java.math.BigDecimal;

public record TipoHabitacionResponse(Integer id, String tipoHabitacion, String descripcion, BigDecimal precio,
                                     Integer cantidadPersonas, String serviciosIncluidos, boolean activo) {

    public static TipoHabitacionResponse de(TipoHabitacion t) {
        return new TipoHabitacionResponse(t.getId(), t.getTipoHabitacion(), t.getDescripcion(), t.getPrecio(),
                t.getCantidadPersonas(), t.getServiciosIncluidos(), t.isActivo());
    }
}
