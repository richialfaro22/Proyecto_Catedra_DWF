package com.udb.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record EstadisticasResponse(
        LocalDate desde,
        LocalDate hasta,
        int totalHoteles,
        int totalHabitaciones,
        long totalHuespedes,
        long reservasCreadas,
        long reservasCanceladas,
        BigDecimal tasaCancelacion,
        BigDecimal ocupacionPorcentaje,
        BigDecimal ingresosBrutos,
        BigDecimal reembolsos,
        BigDecimal ingresosNetos,
        Map<String, Long> reservasPorEstado,
        Map<String, Long> reclamosPorEstado,
        List<PorHotel> porHotel,
        List<PorMes> ingresosPorMes) {

    public record PorHotel(Integer hotelId, String nombreHotel, int habitaciones, long nochesOcupadas,
                           BigDecimal ocupacionPorcentaje, long reservas, long cancelaciones,
                           BigDecimal ingresos) {
    }

    public record PorMes(String mes, BigDecimal ingresos, long reservas) {
    }
}
