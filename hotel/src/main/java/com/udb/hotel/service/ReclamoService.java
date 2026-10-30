package com.udb.hotel.service;

import com.udb.hotel.dto.ReclamoRequest;
import com.udb.hotel.dto.ReclamoRespuestaRequest;
import com.udb.hotel.dto.ReclamoResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.*;
import com.udb.hotel.repository.ReclamoRepository;
import com.udb.hotel.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReclamoService {

    private final ReclamoRepository repo;
    private final HuespedService huespedService;
    private final ReservaService reservaService;
    private final UsuarioActual usuarioActual;

    @Transactional(readOnly = true)
    public List<ReclamoResponse> listar(EstadoReclamo estado) {
        List<Reclamo> lista;
        if (usuarioActual.esPersonal()) {
            lista = repo.listar(estado);
        } else {
            Huesped h = usuarioActual.entidad().getHuesped();
            lista = h == null ? List.of() : repo.listarDeHuesped(h.getId()).stream()
                    .filter(c -> estado == null || c.getEstado() == estado).toList();
        }
        return lista.stream().map(ReclamoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ReclamoResponse obtener(Integer id) {
        Reclamo c = buscar(id);
        verificarAcceso(c);
        return ReclamoResponse.de(c);
    }

    public ReclamoResponse crear(ReclamoRequest r) {
        Huesped huesped;
        if (usuarioActual.esPersonal()) {
            if (r.huespedId() == null) {
                throw new ValidacionException("huespedId", "Selecciona el huésped que realiza el reclamo.");
            }
            huesped = huespedService.buscar(r.huespedId());
        } else {
            huesped = usuarioActual.entidad().getHuesped();
            if (huesped == null) {
                throw new OperacionNoPermitidaException(
                        "Antes completa tus datos de huésped en tu perfil (o realiza una reserva) para enviar reclamos");
            }
        }

        Reserva reserva = null;
        if (r.reservaId() != null) {
            reserva = reservaService.buscarConAcceso(r.reservaId());
            if (!reserva.getHuesped().getId().equals(huesped.getId()) && usuarioActual.esPersonal()) {
                throw new ValidacionException("reservaId", "La reserva seleccionada no pertenece a este huésped!");
            }
        }

        Reclamo c = Reclamo.builder()
                .huesped(huesped)
                .reserva(reserva)
                .tipo(r.tipo())
                .asunto(r.asunto().trim())
                .descripcion(r.descripcion().trim())
                .estado(EstadoReclamo.ABIERTO)
                .build();
        return ReclamoResponse.de(repo.save(c));
    }

    public ReclamoResponse responder(Integer id, ReclamoRespuestaRequest r) {
        Reclamo c = buscar(id);
        if (c.getEstado() == EstadoReclamo.CERRADO) {
            throw new OperacionNoPermitidaException("El reclamo a sido cerrado");
        }
        if (r.estado() == EstadoReclamo.ABIERTO) {
            throw new ValidacionException("estado", "Al responder, el estado debe ser EN PROCESO, RESUELTO o CERRADO.");
        }
        c.setEstado(r.estado());
        c.setRespuesta(r.respuesta().trim());
        c.setFechaRespuesta(LocalDateTime.now());
        c.setAtendidoPor(usuarioActual.alias());
        return ReclamoResponse.de(repo.save(c));
    }

    public void eliminar(Integer id) {
        repo.delete(buscar(id));
    }

    private Reclamo buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el reclamo solicitado."));
    }

    private void verificarAcceso(Reclamo c) {
        if (usuarioActual.esPersonal()) {
            return;
        }
        Huesped h = usuarioActual.entidad().getHuesped();
        if (h == null || !h.getId().equals(c.getHuesped().getId())) {
            throw new AccessDeniedException("No tienes acceso a este reclamo.");
        }
    }
}
