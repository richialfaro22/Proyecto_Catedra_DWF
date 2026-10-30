package com.udb.hotel.dto;

import com.udb.hotel.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Resumen completo de una reserva (paso 4 del proceso de reserva). */
public record ReservaResponse(
        Integer id,
        String codigo,
        String estadoReserva,
        LocalDateTime fechaReserva,
        HuespedResponse huesped,
        Integer hotelId,
        String nombreHotel,
        String ciudad,
        String direccionHotel,
        List<Detalle> detalles,
        Integer habitacionId,
        String numeroHabitacion,
        String tipoHabitacion,
        Integer cantidadPersonas,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        long noches,
        int numPersonas,
        String observaciones,
        List<ServicioReservado> servicios,
        BigDecimal subtotalHabitaciones,
        BigDecimal subtotalServicios,
        BigDecimal total,
        BigDecimal totalPagado,
        BigDecimal totalReembolsado,
        BigDecimal saldoPendiente,
        String creadaPor,
        LocalDateTime fechaCancelacion,
        String motivoCancelacion,
        BigDecimal cargoCancelacion,
        Integer horasCancelacionGratis,
        BigDecimal porcentajePenalizacion,
        List<PagoResponse> pagos) {

    /** Fila de Detalle_Reserva. */
    public record Detalle(Integer id, Integer habitacionId, String numeroHabitacion, String tipoHabitacion,
                          Integer cantidadPersonas, Integer numPersonas, Integer cantidadNoches,
                          BigDecimal precioReserva) {
    }

    public record ServicioReservado(Integer servicioId, String nombreServicio, String categoria, String tipoCobro,
                                    Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
    }

    public static ReservaResponse de(Reserva r, BigDecimal pagado, BigDecimal reembolsado, BigDecimal saldo,
                                     boolean incluirPagos) {
        var hotel = r.getHotel();
        List<Detalle> detalles = r.getDetalles().stream()
                .map(d -> new Detalle(d.getId(), d.getHabitacion().getId(), d.getHabitacion().getNumeroHabitacion(),
                        d.getHabitacion().getTipo().getTipoHabitacion(),
                        d.getHabitacion().getTipo().getCantidadPersonas(), d.getNumPersonas(),
                        d.getCantidadNoches(), d.getPrecioReserva()))
                .toList();
        Detalle principal = detalles.isEmpty() ? null : detalles.get(0);
        List<ServicioReservado> servicios = r.getServicios().stream()
                .map(s -> new ServicioReservado(s.getServicio().getId(), s.getServicio().getNombreServicio(),
                        s.getServicio().getCategoria().name(), s.getServicio().getTipoCobro().name(),
                        s.getCantidad(), s.getPrecioUnitario(), s.getSubtotal()))
                .toList();
        List<PagoResponse> pagos = incluirPagos ? r.getPagos().stream().map(PagoResponse::de).toList() : List.of();
        return new ReservaResponse(r.getId(), r.getCodigo(), r.getEstadoReserva().name(), r.getFechaReserva(),
                HuespedResponse.de(r.getHuesped()), hotel.getId(), hotel.getNombreHotel(), hotel.getCiudad(),
                hotel.getDireccion(), detalles,
                principal != null ? principal.habitacionId() : null,
                principal != null ? principal.numeroHabitacion() : null,
                principal != null ? principal.tipoHabitacion() : null,
                principal != null ? principal.cantidadPersonas() : null,
                r.getFechaEntrada(), r.getFechaSalida(), r.getNoches(), r.getNumPersonas(), r.getObservaciones(),
                servicios, r.getSubtotalHabitaciones(), r.getSubtotalServicios(), r.getTotal(), pagado, reembolsado,
                saldo, r.getUsuario().getAlias(), r.getFechaCancelacion(), r.getMotivoCancelacion(),
                r.getCargoCancelacion(), hotel.getHorasCancelacionGratis(), hotel.getPorcentajePenalizacion(), pagos);
    }
}
