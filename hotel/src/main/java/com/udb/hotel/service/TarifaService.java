package com.udb.hotel.service;

import com.udb.hotel.dto.TarifaRequest;
import com.udb.hotel.dto.TarifaResponse;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.Tarifa;
import com.udb.hotel.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TarifaService {

    private final TarifaRepository repo;
    private final HotelService hotelService;
    private final TipoHabitacionService tipoService;

    @Transactional(readOnly = true)
    public List<TarifaResponse> listar() {
        return repo.listarTodas().stream().map(TarifaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TarifaResponse obtener(Integer id) {
        return TarifaResponse.de(buscar(id));
    }

    public TarifaResponse crear(TarifaRequest r) {
        validar(r, null);
        Tarifa t = new Tarifa();
        t.setActiva(true);
        copiar(r, t);
        return TarifaResponse.de(repo.save(t));
    }

    public TarifaResponse actualizar(Integer id, TarifaRequest r) {
        Tarifa t = buscar(id);
        validar(r, id);
        copiar(r, t);
        return TarifaResponse.de(repo.save(t));
    }

    public TarifaResponse cambiarEstado(Integer id, boolean activa) {
        Tarifa t = buscar(id);
        if (activa && repo.contarTraslapes(t.getHotel().getId(), t.getTipo().getId(),
                t.getFechaInicio(), t.getFechaFin(), id) > 0) {
            throw new ValidacionException("fechaInicio",
                    "No se puede activar: ya existe otra tarifa activa en esas fechas para el mismo hotel y tipo.");
        }
        t.setActiva(activa);
        return TarifaResponse.de(repo.save(t));
    }

    public void eliminar(Integer id) {
        repo.delete(buscar(id));
    }

    private Tarifa buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la tarifa solicitada."));
    }

    private void validar(TarifaRequest r, Integer idActual) {
        if (r.fechaFin().isBefore(r.fechaInicio())) {
            throw new ValidacionException("fechaFin", "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        if (r.fechaFin().isAfter(r.fechaInicio().plusYears(1))) {
            throw new ValidacionException("fechaFin", "Una tarifa no puede durar más de un año.");
        }
        if (repo.contarTraslapes(r.hotelId(), r.tipoId(), r.fechaInicio(), r.fechaFin(), idActual) > 0) {
            throw new ValidacionException("fechaInicio",
                    "Ya existe una tarifa activa para ese hotel y tipo de habitación que se cruza con esas fechas.");
        }
    }

    private void copiar(TarifaRequest r, Tarifa t) {
        t.setNombreTarifa(r.nombreTarifa().trim());
        t.setHotel(hotelService.buscar(r.hotelId()));
        t.setTipo(tipoService.buscar(r.tipoId()));
        t.setPrecioNoche(r.precioNoche());
        t.setFechaInicio(r.fechaInicio());
        t.setFechaFin(r.fechaFin());
    }
}
