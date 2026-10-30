package com.udb.hotel.dto;

import com.udb.hotel.model.Tarifa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaResponse(Integer id, String nombreTarifa, Integer hotelId, String nombreHotel, Integer tipoId,
                             String tipoHabitacion, BigDecimal precioNoche, LocalDate fechaInicio,
                             LocalDate fechaFin, boolean activa) {

    public static TarifaResponse de(Tarifa t) {
        return new TarifaResponse(t.getId(), t.getNombreTarifa(), t.getHotel().getId(), t.getHotel().getNombreHotel(),
                t.getTipo().getId(), t.getTipo().getTipoHabitacion(), t.getPrecioNoche(),
                t.getFechaInicio(), t.getFechaFin(), t.isActiva());
    }
}
