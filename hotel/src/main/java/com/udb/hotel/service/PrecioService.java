package com.udb.hotel.service;

import com.udb.hotel.model.Habitacion;
import com.udb.hotel.model.ServicioAdicional;
import com.udb.hotel.model.Tarifa;
import com.udb.hotel.model.TipoCobro;
import com.udb.hotel.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrecioService {

    private final TarifaRepository tarifaRepo;

    public BigDecimal precioEstadia(Habitacion hab, LocalDate entrada, LocalDate salida) {
        List<Tarifa> tarifas = tarifaRepo.vigentes(hab.getHotel().getId(), hab.getTipo().getId(), entrada, salida);
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            final LocalDate d = noche;
            BigDecimal precio = tarifas.stream()
                    .filter(t -> !d.isBefore(t.getFechaInicio()) && !d.isAfter(t.getFechaFin()))
                    .max(Comparator.comparing(Tarifa::getId))
                    .map(Tarifa::getPrecioNoche)
                    .orElse(hab.getPrecioBase());
            total = total.add(precio);
        }
        return dinero(total);
    }

    public BigDecimal subtotalServicio(ServicioAdicional s, int cantidad, long noches) {
        BigDecimal base = s.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        if (s.getTipoCobro() == TipoCobro.POR_NOCHE) {
            base = base.multiply(BigDecimal.valueOf(noches));
        }
        return dinero(base);
    }

    public static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
