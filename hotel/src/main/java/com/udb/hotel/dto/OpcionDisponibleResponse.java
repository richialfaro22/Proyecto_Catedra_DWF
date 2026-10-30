package com.udb.hotel.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Opción del resultado de búsqueda (paso 2): un tipo de habitación en un hotel con al menos una
 * habitación libre. {@code habitacionId} es la habitación sugerida (la de menor precio).
 */
public record OpcionDisponibleResponse(
        Integer hotelId,
        String nombreHotel,
        String ciudad,
        String direccion,
        Integer estrellas,
        Integer tipoId,
        String tipoHabitacion,
        String descripcionTipo,
        Integer cantidadPersonas,
        List<String> serviciosIncluidos,
        Integer habitacionId,
        String numeroHabitacion,
        List<HabitacionLibre> habitaciones,
        int habitacionesDisponibles,
        long noches,
        BigDecimal precioPorNoche,
        BigDecimal precioTotal,
        Integer horasCancelacionGratis,
        BigDecimal porcentajePenalizacion) {

    public record HabitacionLibre(Integer id, String numeroHabitacion, Integer numPiso, BigDecimal precioTotal) {
    }
}
