package com.udb.hotel.service;

import com.udb.hotel.dto.*;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.*;
import com.udb.hotel.repository.HabitacionRepository;
import com.udb.hotel.repository.ReclamoRepository;
import com.udb.hotel.repository.ReservaRepository;
import com.udb.hotel.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaService {


    public static final LocalTime HORA_ENTRADA = LocalTime.of(15, 0);

    private static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RANDOM = new Random();

    private final ReservaRepository repo;
    private final HabitacionRepository habitacionRepo;
    private final ReclamoRepository reclamoRepo;
    private final HuespedService huespedService;
    private final ServicioAdicionalService servicioService;
    private final PrecioService precioService;
    private final UsuarioActual usuarioActual;

    // consultas

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(EstadoReserva estado, Integer hotelId) {
        List<Reserva> lista;
        if (usuarioActual.esPersonal()) {
            lista = repo.listar(estado, hotelId);
        } else {
            Usuario yo = usuarioActual.entidad();
            Integer huespedId = yo.getHuesped() != null ? yo.getHuesped().getId() : null;
            lista = repo.listarDeUsuario(yo.getId(), huespedId).stream()
                    .filter(r -> estado == null || r.getEstadoReserva() == estado)
                    .toList();
        }
        return lista.stream().map(r -> resumen(r, false)).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Integer id) {
        return resumen(buscarConAcceso(id), true);
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtenerPorCodigo(String codigo) {
        Reserva r = repo.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una reserva con el código " + codigo + "."));
        verificarAcceso(r);
        return resumen(r, true);
    }

    //  creación

    public ReservaResponse crear(ReservaRequest r) {
        DisponibilidadService.validarFechas(r.fechaEntrada(), r.fechaSalida(), "fechaEntrada", "fechaSalida");

        Habitacion hab = habitacionRepo.bloquearParaReserva(r.habitacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La habitación seleccionada no existe."));
        validarHabitacionReservable(hab, r.numPersonas());
        verificarDisponible(hab, r.fechaEntrada(), r.fechaSalida(), null);

        Usuario usuario = usuarioActual.entidad();
        Huesped huesped = huespedService.resolverParaReserva(r.huesped(), usuario, usuarioActual.esPersonal());

        Reserva reserva = Reserva.builder()
                .codigo(generarCodigo())
                .huesped(huesped)
                .hotel(hab.getHotel())
                .usuario(usuario)
                .fechaEntrada(r.fechaEntrada())
                .fechaSalida(r.fechaSalida())
                .observaciones(limpiar(r.observaciones()))
                .estadoReserva(EstadoReserva.PENDIENTE)
                .build();
        reserva.getDetalles().add(DetalleReserva.builder()
                .reserva(reserva)
                .habitacion(hab)
                .numPersonas(r.numPersonas())
                .cantidadNoches(0)
                .precioReserva(BigDecimal.ZERO)
                .build());
        asignarServicios(reserva, r.servicios());
        recalcularTotales(reserva);
        return resumen(repo.save(reserva), true);
    }

    //  modificación

    public ReservaResponse modificar(Integer id, ReservaModificarRequest m) {
        Reserva r = buscarConAcceso(id);
        if (r.getEstadoReserva() != EstadoReserva.PENDIENTE && r.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw new OperacionNoPermitidaException(
                    "Solo se pueden modificar reservas que estan pendientes o confirmadas (estado actual: "
                    + r.getEstadoReserva() + ").");
        }
        if (!r.getFechaEntrada().isAfter(LocalDate.now())) {
            throw new OperacionNoPermitidaException(
                    "La reserva ya no se puede modificar porque la fecha de entrada es la actual o ya pasó");
        }
        DisponibilidadService.validarFechas(m.fechaEntrada(), m.fechaSalida(), "fechaEntrada", "fechaSalida");

        DetalleReserva detalle = r.getDetalles().get(0);
        Habitacion hab = habitacionRepo.bloquearParaReserva(detalle.getHabitacion().getId()).orElseThrow();
        if (m.numPersonas() > hab.getTipo().getCantidadPersonas()) {
            throw new ValidacionException("numPersonas", "La habitación " + hab.getTipo().getTipoHabitacion()
                    + " admite como máximo " + hab.getTipo().getCantidadPersonas() + " huéspedes.");
        }
        verificarDisponible(hab, m.fechaEntrada(), m.fechaSalida(), r.getId());

        r.setFechaEntrada(m.fechaEntrada());
        r.setFechaSalida(m.fechaSalida());
        r.setObservaciones(limpiar(m.observaciones()));
        detalle.setNumPersonas(m.numPersonas());
        if (m.servicios() != null) {
            r.getServicios().clear();
            asignarServicios(r, m.servicios());
        }
        recalcularTotales(r);

        BigDecimal neto = netoPagado(r);
        int cmp = neto.compareTo(r.getTotal());
        if (cmp > 0) {
            registrarReembolso(r, neto.subtract(r.getTotal()), "Reembolso por modificación de la reserva");
            r.setEstadoReserva(EstadoReserva.CONFIRMADA);
        } else if (cmp == 0 && neto.signum() > 0) {
            r.setEstadoReserva(EstadoReserva.CONFIRMADA);
        } else {
            r.setEstadoReserva(EstadoReserva.PENDIENTE);
        }
        return resumen(repo.save(r), true);
    }


    @Transactional(readOnly = true)
    public CotizacionCancelacionResponse cotizarCancelacion(Integer id) {
        Reserva r = buscarConAcceso(id);
        validarCancelable(r);
        return cotizar(r);
    }

    public ReservaResponse cancelar(Integer id, CancelacionRequest c) {
        Reserva r = buscarConAcceso(id);
        validarCancelable(r);
        CotizacionCancelacionResponse cot = cotizar(r);

        if (cot.reembolso().signum() > 0) {
            registrarReembolso(r, cot.reembolso(), "Reembolso por cancelación");
        }
        r.setEstadoReserva(EstadoReserva.CANCELADA);
        r.setFechaCancelacion(LocalDateTime.now());
        r.setMotivoCancelacion(c.motivo().trim());
        r.setCargoCancelacion(cot.cargo());
        return resumen(repo.save(r), true);
    }


    public ReservaResponse checkIn(Integer id) {
        Reserva r = buscar(id);
        if (r.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw new OperacionNoPermitidaException("Solo se puede hacer check-in de reservas que fueron confirmadas");
        }
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(r.getFechaEntrada()) || !hoy.isBefore(r.getFechaSalida())) {
            throw new OperacionNoPermitidaException("El check-in solo se puede realizar entre la fecha de entrada ("
                    + r.getFechaEntrada() + ") y el día anterior a la salida.");
        }
        r.setEstadoReserva(EstadoReserva.EN_CURSO);
        r.getDetalles().forEach(d -> d.getHabitacion().setEstado(EstadoHabitacion.OCUPADA));
        return resumen(repo.save(r), true);
    }

    public ReservaResponse checkOut(Integer id) {
        Reserva r = buscar(id);
        if (r.getEstadoReserva() != EstadoReserva.EN_CURSO) {
            throw new OperacionNoPermitidaException("Solo se puede hacer check-out de reservas que estan en curso");
        }
        r.setEstadoReserva(EstadoReserva.COMPLETADA);
        r.getDetalles().forEach(d -> {
            if (d.getHabitacion().getEstado() == EstadoHabitacion.OCUPADA) {
                d.getHabitacion().setEstado(EstadoHabitacion.DISPONIBLE);
            }
        });
        return resumen(repo.save(r), true);
    }

    public void eliminar(Integer id) {
        Reserva r = buscar(id);
        boolean tienePagos = r.getPagos().stream().anyMatch(p -> p.getEstado() == EstadoPago.APROBADO);
        if (r.getEstadoReserva() != EstadoReserva.CANCELADA
                && !(r.getEstadoReserva() == EstadoReserva.PENDIENTE && !tienePagos)) {
            throw new OperacionNoPermitidaException(
                    "Solo se pueden eliminar reservas canceladas o pendientes sin pagos pendientes");
        }
        if (reclamoRepo.existsByReservaId(id)) {
            throw new OperacionNoPermitidaException("No se puede eliminar: la reserva tiene reclamos asociados");
        }
        repo.delete(r);
    }


    public Reserva buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la reserva solicitada"));
    }

    public Reserva buscarConAcceso(Integer id) {
        Reserva r = buscar(id);
        verificarAcceso(r);
        return r;
    }

    public void verificarAcceso(Reserva r) {
        if (usuarioActual.esPersonal()) {
            return;
        }
        Usuario yo = usuarioActual.entidad();
        boolean creador = r.getUsuario().getId().equals(yo.getId());
        boolean titular = yo.getHuesped() != null && r.getHuesped().getId().equals(yo.getHuesped().getId());
        if (!creador && !titular) {
            throw new AccessDeniedException("No tienes acceso a esta reserva.");
        }
    }

    public static BigDecimal totalPagos(Reserva r, EstadoPago estado) {
        return r.getPagos().stream()
                .filter(p -> p.getEstado() == estado)
                .map(Pago::getTotalPagar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal netoPagado(Reserva r) {
        return totalPagos(r, EstadoPago.APROBADO).subtract(totalPagos(r, EstadoPago.REEMBOLSADO));
    }

    public static BigDecimal saldoPendiente(Reserva r) {
        if (r.getEstadoReserva() == EstadoReserva.CANCELADA) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal saldo = r.getTotal().subtract(netoPagado(r));
        return saldo.signum() < 0 ? BigDecimal.ZERO.setScale(2) : PrecioService.dinero(saldo);
    }

    public ReservaResponse resumen(Reserva r, boolean incluirPagos) {
        return ReservaResponse.de(r, PrecioService.dinero(totalPagos(r, EstadoPago.APROBADO)),
                PrecioService.dinero(totalPagos(r, EstadoPago.REEMBOLSADO)), saldoPendiente(r), incluirPagos);
    }


    private void validarHabitacionReservable(Habitacion hab, int personas) {
        if (!hab.getHotel().getEstado().permiteReservas() || !hab.getTipo().isActivo()) {
            throw new OperacionNoPermitidaException("La habitación seleccionada no está disponible para reservas");
        }
        if (hab.getEstado() == EstadoHabitacion.MANTENIMIENTO || hab.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new OperacionNoPermitidaException("La habitación " + hab.getNumeroHabitacion()
                    + " no está disponible (" + hab.getEstado() + "). Elige otra opción.");
        }
        if (personas > hab.getTipo().getCantidadPersonas()) {
            throw new ValidacionException("numPersonas", "La habitación " + hab.getTipo().getTipoHabitacion()
                    + " admite como máximo " + hab.getTipo().getCantidadPersonas() + " huéspedes.");
        }
    }

    private void verificarDisponible(Habitacion hab, LocalDate entrada, LocalDate salida, Integer excluirId) {
        long cruces = repo.contarCruces(hab.getId(), entrada, salida, DisponibilidadService.ESTADOS_ACTIVOS, excluirId);
        if (cruces > 0) {
            throw new OperacionNoPermitidaException("Lo sentimos, la habitación " + hab.getNumeroHabitacion()
                    + " ya no está disponible en esas fechas. Realiza una nueva búsqueda.");
        }
    }

    private void asignarServicios(Reserva reserva, List<ServicioSolicitado> solicitados) {
        if (solicitados == null) {
            return;
        }
        Set<Integer> vistos = new HashSet<>();
        for (int i = 0; i < solicitados.size(); i++) {
            ServicioSolicitado s = solicitados.get(i);
            if (!vistos.add(s.servicioId())) {
                throw new ValidacionException("servicios[" + i + "].servicioId",
                        "El servicio está repetido; cambia la cantidad en lugar de agregarlo dos veces.");
            }
            ServicioAdicional servicio = servicioService.buscar(s.servicioId());
            if (!servicio.isActivo()) {
                throw new ValidacionException("servicios[" + i + "].servicioId",
                        "El servicio '" + servicio.getNombreServicio() + "' no está disponible actualmente.");
            }
            reserva.getServicios().add(ReservaServicio.builder()
                    .reserva(reserva)
                    .servicio(servicio)
                    .cantidad(s.cantidad())
                    .precioUnitario(servicio.getPrecio())
                    .subtotal(BigDecimal.ZERO)
                    .build());
        }
    }

    private void recalcularTotales(Reserva r) {
        long noches = r.getNoches();
        BigDecimal habitaciones = BigDecimal.ZERO;
        for (DetalleReserva d : r.getDetalles()) {
            d.setCantidadNoches((int) noches);
            d.setPrecioReserva(precioService.precioEstadia(d.getHabitacion(), r.getFechaEntrada(), r.getFechaSalida()));
            habitaciones = habitaciones.add(d.getPrecioReserva());
        }
        BigDecimal servicios = BigDecimal.ZERO;
        for (ReservaServicio rs : r.getServicios()) {
            rs.setSubtotal(precioService.subtotalServicio(rs.getServicio(), rs.getCantidad(), noches));
            servicios = servicios.add(rs.getSubtotal());
        }
        r.setSubtotalHabitaciones(PrecioService.dinero(habitaciones));
        r.setSubtotalServicios(PrecioService.dinero(servicios));
        r.setTotal(PrecioService.dinero(habitaciones.add(servicios)));
    }

    private void validarCancelable(Reserva r) {
        if (r.getEstadoReserva() != EstadoReserva.PENDIENTE && r.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw new OperacionNoPermitidaException(
                    "Solo se pueden cancelar reservas que estan pendientes o confirmadas (estado actual: "
                    + r.getEstadoReserva() + ").");
        }
    }

    private CotizacionCancelacionResponse cotizar(Reserva r) {
        Hotel hotel = r.getHotel();
        LocalDateTime entrada = r.getFechaEntrada().atTime(HORA_ENTRADA);
        long horas = Math.max(0, Duration.between(LocalDateTime.now(), entrada).toHours());
        boolean gratis = horas >= hotel.getHorasCancelacionGratis();

        BigDecimal pagado = PrecioService.dinero(netoPagado(r));
        BigDecimal cargoTeorico = gratis ? BigDecimal.ZERO
                : r.getTotal().multiply(hotel.getPorcentajePenalizacion()).divide(BigDecimal.valueOf(100));

        BigDecimal cargo = PrecioService.dinero(cargoTeorico.min(pagado));
        BigDecimal reembolso = PrecioService.dinero(pagado.subtract(cargo));

        String mensaje;
        if (gratis) {
            mensaje = "Estás dentro del plazo de cancelación gratuita (" + hotel.getHorasCancelacionGratis()
                    + " horas antes de la entrada). No se aplicará ningún cargo.";
        } else {
            mensaje = "Cancelaciones con menos de " + hotel.getHorasCancelacionGratis()
                    + " horas de anticipación tienen un cargo del " + hotel.getPorcentajePenalizacion()
                    + "% sobre el total. Cargo aplicado: $" + cargo + ".";
        }
        if (reembolso.signum() > 0) {
            mensaje += " Se reembolsarán $" + reembolso + " al método de pago original.";
        }
        return new CotizacionCancelacionResponse(r.getId(), r.getCodigo(), r.getTotal(), pagado, gratis, horas,
                hotel.getHorasCancelacionGratis(), hotel.getPorcentajePenalizacion(), cargo, reembolso, mensaje);
    }


    private void registrarReembolso(Reserva r, BigDecimal monto, String observacion) {
        MetodoPago metodo = r.getPagos().stream()
                .filter(p -> p.getEstado() == EstadoPago.APROBADO)
                .reduce((a, b) -> b)
                .map(Pago::getMetodoPago)
                .orElse(MetodoPago.EFECTIVO);
        r.getPagos().add(Pago.builder()
                .reserva(r)
                .metodoPago(metodo)
                .totalPagar(PrecioService.dinero(monto))
                .estado(EstadoPago.REEMBOLSADO)
                .referencia(PagoService.generarReferencia("REE"))
                .observacion(observacion)
                .build());
    }

    private String generarCodigo() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String codigo;
        do {
            StringBuilder sb = new StringBuilder("R").append(fecha).append('-');
            for (int i = 0; i < 4; i++) {
                sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
            }
            codigo = sb.toString();
        } while (repo.findByCodigo(codigo).isPresent());
        return codigo;
    }

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
