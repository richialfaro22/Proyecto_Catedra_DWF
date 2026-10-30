package com.udb.hotel.service;

import com.udb.hotel.dto.EstadisticasResponse;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.*;
import com.udb.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstadisticaService {

    private static final Set<EstadoReserva> OCUPAN = EnumSet.of(
            EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO, EstadoReserva.COMPLETADA);

    private final HotelRepository hotelRepo;
    private final HabitacionRepository habitacionRepo;
    private final HuespedRepository huespedRepo;
    private final ReservaRepository reservaRepo;
    private final PagoRepository pagoRepo;
    private final ReclamoRepository reclamoRepo;

    public EstadisticasResponse generar(LocalDate desdeParam, LocalDate hastaParam) {
        final LocalDate desde = desdeParam != null ? desdeParam : LocalDate.now().withDayOfMonth(1);
        final LocalDate hasta = hastaParam != null ? hastaParam : YearMonth.from(desde).atEndOfMonth();
        if (hasta.isBefore(desde)) {
            throw new ValidacionException("hasta", "La fecha final no puede ser anterior a la inicial.");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > 366) {
            throw new ValidacionException("hasta", "El período máximo para estadísticas es de un año.");
        }
        final LocalDate finExclusivo = hasta.plusDays(1);
        final long dias = ChronoUnit.DAYS.between(desde, finExclusivo);

        List<Hotel> hoteles = hotelRepo.listarTodos();
        List<Habitacion> habitaciones = habitacionRepo.listarTodas().stream()
                .filter(h -> h.getEstado() != EstadoHabitacion.FUERA_DE_SERVICIO).toList();
        List<Reserva> ocupando = reservaRepo.ocupando(desde, finExclusivo, OCUPAN);
        List<Reserva> creadas = reservaRepo.creadasEntre(desde.atStartOfDay(), finExclusivo.atStartOfDay());
        List<Pago> pagos = pagoRepo.movimientosEntre(desde.atStartOfDay(), finExclusivo.atStartOfDay());

        long nochesOcupadas = ocupando.stream().mapToLong(r -> nochesEnRango(r, desde, finExclusivo)).sum();
        BigDecimal ocupacion = porcentaje(nochesOcupadas, (long) habitaciones.size() * dias);
        long canceladas = creadas.stream().filter(r -> r.getEstadoReserva() == EstadoReserva.CANCELADA).count();
        BigDecimal brutos = suma(pagos, EstadoPago.APROBADO, null);
        BigDecimal reembolsos = suma(pagos, EstadoPago.REEMBOLSADO, null);

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoReserva e : EstadoReserva.values()) {
            porEstado.put(e.name(), creadas.stream().filter(r -> r.getEstadoReserva() == e).count());
        }
        Map<String, Long> reclamos = new LinkedHashMap<>();
        for (EstadoReclamo e : EstadoReclamo.values()) {
            reclamos.put(e.name(), reclamoRepo.countByEstado(e));
        }

        List<EstadisticasResponse.PorHotel> porHotel = new ArrayList<>();
        for (Hotel h : hoteles) {
            Integer id = h.getId();
            int habs = (int) habitaciones.stream().filter(x -> x.getHotel().getId().equals(id)).count();
            long noches = ocupando.stream().filter(r -> r.getHotel().getId().equals(id))
                    .mapToLong(r -> nochesEnRango(r, desde, finExclusivo)).sum();
            long res = creadas.stream().filter(r -> r.getHotel().getId().equals(id)).count();
            long canc = creadas.stream().filter(r -> r.getHotel().getId().equals(id)
                    && r.getEstadoReserva() == EstadoReserva.CANCELADA).count();
            BigDecimal ingresos = suma(pagos, EstadoPago.APROBADO, id).subtract(suma(pagos, EstadoPago.REEMBOLSADO, id));
            porHotel.add(new EstadisticasResponse.PorHotel(id, h.getNombreHotel(), habs, noches,
                    porcentaje(noches, (long) habs * dias), res, canc, ingresos));
        }

        List<EstadisticasResponse.PorMes> porMes = new ArrayList<>();
        YearMonth ultimo = YearMonth.from(hasta);
        for (int i = 5; i >= 0; i--) {
            YearMonth m = ultimo.minusMonths(i);
            var ini = m.atDay(1).atStartOfDay();
            var fin = m.plusMonths(1).atDay(1).atStartOfDay();
            List<Pago> pm = pagoRepo.movimientosEntre(ini, fin);
            long rm = reservaRepo.creadasEntre(ini, fin).size();
            porMes.add(new EstadisticasResponse.PorMes(m.toString(),
                    suma(pm, EstadoPago.APROBADO, null).subtract(suma(pm, EstadoPago.REEMBOLSADO, null)), rm));
        }

        return new EstadisticasResponse(desde, hasta, hoteles.size(), habitaciones.size(), huespedRepo.count(),
                creadas.size(), canceladas, porcentaje(canceladas, creadas.size()), ocupacion,
                brutos, reembolsos, brutos.subtract(reembolsos), porEstado, reclamos, porHotel, porMes);
    }

    private static long nochesEnRango(Reserva r, LocalDate desde, LocalDate hastaExclusivo) {
        LocalDate ini = r.getFechaEntrada().isAfter(desde) ? r.getFechaEntrada() : desde;
        LocalDate fin = r.getFechaSalida().isBefore(hastaExclusivo) ? r.getFechaSalida() : hastaExclusivo;
        return Math.max(0, ChronoUnit.DAYS.between(ini, fin)) * r.getDetalles().size();
    }

    private static BigDecimal porcentaje(long parte, long total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(parte * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal suma(List<Pago> pagos, EstadoPago estado, Integer hotelId) {
        return pagos.stream()
                .filter(p -> p.getEstado() == estado)
                .filter(p -> hotelId == null || p.getReserva().getHotel().getId().equals(hotelId))
                .map(Pago::getTotalPagar)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
