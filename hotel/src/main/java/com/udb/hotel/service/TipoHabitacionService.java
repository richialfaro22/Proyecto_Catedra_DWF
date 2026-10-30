package com.udb.hotel.service;

import com.udb.hotel.dto.TipoHabitacionRequest;
import com.udb.hotel.dto.TipoHabitacionResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.model.TipoHabitacion;
import com.udb.hotel.repository.HabitacionRepository;
import com.udb.hotel.repository.TarifaRepository;
import com.udb.hotel.repository.TipoHabitacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoHabitacionService {

    private final TipoHabitacionRepository repo;
    private final HabitacionRepository habitacionRepo;
    private final TarifaRepository tarifaRepo;

    @Transactional(readOnly = true)
    public List<TipoHabitacionResponse> listar(boolean soloActivos) {
        return repo.findAllByOrderByTipoHabitacionAsc().stream()
                .filter(t -> !soloActivos || t.isActivo())
                .map(TipoHabitacionResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TipoHabitacionResponse obtener(Integer id) {
        return TipoHabitacionResponse.de(buscar(id));
    }

    public TipoHabitacionResponse crear(TipoHabitacionRequest r) {
        validarNombre(r.tipoHabitacion(), null);
        TipoHabitacion t = new TipoHabitacion();
        t.setActivo(true);
        copiar(r, t);
        return TipoHabitacionResponse.de(repo.save(t));
    }

    public TipoHabitacionResponse actualizar(Integer id, TipoHabitacionRequest r) {
        TipoHabitacion t = buscar(id);
        validarNombre(r.tipoHabitacion(), id);
        copiar(r, t);
        return TipoHabitacionResponse.de(repo.save(t));
    }

    public TipoHabitacionResponse cambiarEstado(Integer id, boolean activo) {
        TipoHabitacion t = buscar(id);
        t.setActivo(activo);
        return TipoHabitacionResponse.de(repo.save(t));
    }

    public void eliminar(Integer id) {
        TipoHabitacion t = buscar(id);
        if (habitacionRepo.existsByTipoId(id) || tarifaRepo.existsByTipoId(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el tipo porque hay habitaciones o tarifas que lo usan. "
                    + "Puedes desactivarlo en su lugar.");
        }
        repo.delete(t);
    }

    public TipoHabitacion buscar(Integer id) {
        return repo.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró el tipo de habitación solicitado."));
    }

    private void validarNombre(String nombre, Integer idActual) {
        repo.findByTipoHabitacionIgnoreCase(nombre.trim()).ifPresent(otro -> {
            if (!otro.getId().equals(idActual)) {
                throw new RecursoDuplicadoException("Ya existe un tipo de habitación llamado '" + nombre.trim() + "'.");
            }
        });
    }

    private void copiar(TipoHabitacionRequest r, TipoHabitacion t) {
        t.setTipoHabitacion(r.tipoHabitacion().trim());
        t.setDescripcion(r.descripcion());
        t.setPrecio(r.precio());
        t.setCantidadPersonas(r.cantidadPersonas());
        t.setServiciosIncluidos(r.serviciosIncluidos());
    }
}
