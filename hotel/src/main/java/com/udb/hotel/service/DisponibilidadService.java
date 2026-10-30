package com.udb.hotel.service;

import com.udb.hotel.dto.OpcionDisponibleResponse;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.EstadoReserva;
import com.udb.hotel.model.Habitacion;
import com.udb.hotel.repository.HabitacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisponibilidadService {

    public static final Set<EstadoReserva> ESTADOS_ACTIVOS =
            EnumSet.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO);

    public static final int MAX_NOCHES = 30;
    public static final int MAX_DIAS_ANTICIPACION = 365;

    private final HabitacionRepository habitacionRepo;
    private final PrecioService precioService;

    private record Libre(Habitacion habitacion, BigDecimal total) {
    }

    public List<OpcionDisponibleResponse> buscar(String ciudad, LocalDate entrada, LocalDate salida,
                                                 int huespedes, Integer tipoId, Integer hotelId) {
        validarFechas(entrada, salida, "fechaEntrada", "fechaSalida");
        String filtroCiudad = (ciudad == null || ciudad.isBlank()) ? null : ciudad.trim();

        List<Habitacion> libres = habitacionRepo.buscarDisponibles(filtroCiudad, tipoId, hotelId, huespedes,
                entrada, salida, ESTADOS_ACTIVOS);


        Map<String, List<Libre>> grupos = new LinkedHashMap<>();
        for (Habitacion h : libres) {
            grupos.computeIfAbsent(h.getHotel().getId() + "-" + h.getTipo().getId(), k -> new ArrayList<>())
                    .add(new Libre(h, precioService.precioEstadia(h, entrada, salida)));
        }

        long noches = ChronoUnit.DAYS.between(entrada, salida);
        List<OpcionDisponibleResponse> opciones = new ArrayList<>();
        for (List<Libre> grupo : grupos.values()) {
            grupo.sort(Comparator.comparing(Libre::total));
            Libre mejor = grupo.get(0);
            var hab = mejor.habitacion();
            var hotel = hab.getHotel();
            var tipo = hab.getTipo();
            List<String> incluidos = tipo.getServiciosIncluidos() == null ? List.of()
                    : Arrays.stream(tipo.getServiciosIncluidos().split(","))
                            .map(String::trim).filter(s -> !s.isEmpty()).toList();
            opciones.add(new OpcionDisponibleResponse(hotel.getId(), hotel.getNombreHotel(), hotel.getCiudad(),
                    hotel.getDireccion(), hotel.getEstrellas(), tipo.getId(), tipo.getTipoHabitacion(),
                    tipo.getDescripcion(), tipo.getCantidadPersonas(), incluidos, hab.getId(),
                    hab.getNumeroHabitacion(),
                    grupo.stream().map(l -> new OpcionDisponibleResponse.HabitacionLibre(l.habitacion().getId(),
                            l.habitacion().getNumeroHabitacion(), l.habitacion().getNumPiso(), l.total())).toList(),
                    grupo.size(), noches, mejor.total().divide(BigDecimal.valueOf(noches), 2, RoundingMode.HALF_UP),
                    mejor.total(), hotel.getHorasCancelacionGratis(), hotel.getPorcentajePenalizacion()));
        }
        opciones.sort(Comparator.comparing(OpcionDisponibleResponse::precioTotal));
        return opciones;
    }

    public static void validarFechas(LocalDate entrada, LocalDate salida, String campoEntrada, String campoSalida) {
        LocalDate hoy = LocalDate.now();
        if (entrada.isBefore(hoy)) {
            throw new ValidacionException(campoEntrada, "La fecha de entrada no puede ser anterior a hoy.");
        }
        if (entrada.isAfter(hoy.plusDays(MAX_DIAS_ANTICIPACION))) {
            throw new ValidacionException(campoEntrada, "Solo se puede reservar con un máximo de un año de anticipación.");
        }
        if (!salida.isAfter(entrada)) {
            throw new ValidacionException(campoSalida, "La fecha de salida debe ser posterior a la fecha de entrada.");
        }
        if (ChronoUnit.DAYS.between(entrada, salida) > MAX_NOCHES) {
            throw new ValidacionException(campoSalida, "La estadía máxima es de " + MAX_NOCHES + " noches.");
        }
    }
}
