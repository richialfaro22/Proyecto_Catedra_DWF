package com.udb.hotel.service;

import com.udb.hotel.dto.PagoRequest;
import com.udb.hotel.dto.PagoResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.*;
import com.udb.hotel.repository.PagoRepository;
import com.udb.hotel.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PagoService {

    private final PagoRepository repo;
    private final ReservaService reservaService;
    private final UsuarioActual usuarioActual;

    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return repo.listarTodos().stream().map(PagoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarDeReserva(Integer reservaId) {
        Reserva r = reservaService.buscarConAcceso(reservaId);
        return r.getPagos().stream().map(PagoResponse::de).toList();
    }

    public PagoResponse pagar(Integer reservaId, PagoRequest p) {
        Reserva r = reservaService.buscarConAcceso(reservaId);
        if (r.getEstadoReserva() != EstadoReserva.PENDIENTE) {
            throw new OperacionNoPermitidaException("Esta reserva no tiene pagos pendientes (estado: "
                    + r.getEstadoReserva() + ").");
        }
        BigDecimal saldo = ReservaService.saldoPendiente(r);
        BigDecimal monto = p.totalPagar() == null ? saldo : PrecioService.dinero(p.totalPagar());
        if (monto.compareTo(saldo) > 0) {
            throw new ValidacionException("totalPagar", "El monto no puede ser mayor al saldo pendiente ($" + saldo + ").");
        }

        Pago pago = Pago.builder()
                .reserva(r)
                .metodoPago(p.metodoPago())
                .totalPagar(monto)
                .referencia(generarReferencia("PAG"))
                .estado(EstadoPago.APROBADO)
                .build();

        switch (p.metodoPago()) {
            case TARJETA -> {
                validarTarjeta(p);
                pago.setUltimosDigitos(p.numeroTarjeta().substring(12));
                if (p.numeroTarjeta().endsWith("0002")) {
                    pago.setEstado(EstadoPago.RECHAZADO);
                    pago.setObservacion("Pago rechazado por el banco emisor (fondos insuficientes).");
                }
            }
            case TRANSFERENCIA -> {
                if (vacio(p.referenciaTransferencia())) {
                    throw new ValidacionException("referenciaTransferencia",
                            "Ingresa el número de referencia de la transferencia.");
                }
                pago.setObservacion("Transferencia ref. " + p.referenciaTransferencia().trim());
            }
            case EFECTIVO -> {
                if (!usuarioActual.esPersonal()) {
                    throw new OperacionNoPermitidaException(
                            "El pago en efectivo solo se puede registrar en la recepción del hotel.");
                }
                pago.setObservacion("Pago en efectivo recibido por " + usuarioActual.alias());
            }
        }

        r.getPagos().add(pago);
        if (pago.getEstado() == EstadoPago.APROBADO && ReservaService.saldoPendiente(r).signum() == 0) {
            r.setEstadoReserva(EstadoReserva.CONFIRMADA);
        }
        return PagoResponse.de(repo.save(pago));
    }

    private void validarTarjeta(PagoRequest p) {
        if (vacio(p.numeroTarjeta())) {
            throw new ValidacionException("numeroTarjeta", "El número de tarjeta es obligatorio.");
        }
        if (!luhnValido(p.numeroTarjeta())) {
            throw new ValidacionException("numeroTarjeta", "El número de tarjeta no es válido. Revisa los dígitos.");
        }
        if (vacio(p.titular())) {
            throw new ValidacionException("titular", "El nombre del titular es obligatorio.");
        }
        if (vacio(p.vencimiento())) {
            throw new ValidacionException("vencimiento", "La fecha de vencimiento es obligatoria.");
        }
        YearMonth venc = YearMonth.parse(p.vencimiento(), DateTimeFormatter.ofPattern("MM/yy"));
        if (venc.isBefore(YearMonth.now())) {
            throw new ValidacionException("vencimiento", "La tarjeta está vencida.");
        }
        if (vacio(p.cvv())) {
            throw new ValidacionException("cvv", "El CVV es obligatorio.");
        }
    }


    static boolean luhnValido(String numero) {
        int suma = 0;
        boolean doble = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int d = numero.charAt(i) - '0';
            if (doble) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            suma += d;
            doble = !doble;
        }
        return suma % 10 == 0;
    }

    public static String generarReferencia(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
