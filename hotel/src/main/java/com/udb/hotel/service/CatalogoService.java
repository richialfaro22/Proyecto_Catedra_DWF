package com.udb.hotel.service;

import com.udb.hotel.dto.CatalogoRequest;
import com.udb.hotel.dto.CatalogoResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.model.EstadoEmpleado;
import com.udb.hotel.model.EstadoHotel;
import com.udb.hotel.repository.EmpleadoRepository;
import com.udb.hotel.repository.EstadoEmpleadoRepository;
import com.udb.hotel.repository.EstadoHotelRepository;
import com.udb.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogoService {

    private final EstadoHotelRepository estadoHotelRepo;
    private final EstadoEmpleadoRepository estadoEmpleadoRepo;
    private final HotelRepository hotelRepo;
    private final EmpleadoRepository empleadoRepo;


    @Transactional(readOnly = true)
    public List<CatalogoResponse> estadosHotel() {
        return estadoHotelRepo.findAllByOrderByIdAsc().stream()
                .map(e -> new CatalogoResponse(e.getId(), e.getEstadoHotel())).toList();
    }

    public CatalogoResponse guardarEstadoHotel(Integer id, CatalogoRequest r) {
        String nombre = r.nombre().trim();
        estadoHotelRepo.findByEstadoHotelIgnoreCase(nombre).ifPresent(o -> {
            if (!o.getId().equals(id)) {
                throw new RecursoDuplicadoException("Ya existe el estado de hotel '" + nombre + "'.");
            }
        });
        EstadoHotel e = id == null ? new EstadoHotel() : buscarEstadoHotel(id);
        if (e.getId() != null && e.permiteReservas() && !nombre.equalsIgnoreCase(EstadoHotel.ACTIVO)) {
            throw new OperacionNoPermitidaException(
                    "El estado 'Activo' es el que permite reservas y no se puede renombrar.");
        }
        e.setEstadoHotel(nombre);
        e = estadoHotelRepo.save(e);
        return new CatalogoResponse(e.getId(), e.getEstadoHotel());
    }

    public void eliminarEstadoHotel(Integer id) {
        EstadoHotel e = buscarEstadoHotel(id);
        if (e.permiteReservas()) {
            throw new OperacionNoPermitidaException("El estado 'Activo' no se puede eliminar.");
        }
        if (hotelRepo.existsByEstadoId(id)) {
            throw new OperacionNoPermitidaException("No se puede eliminar: hay hoteles con ese estado.");
        }
        estadoHotelRepo.delete(e);
    }

    public EstadoHotel buscarEstadoHotel(Integer id) {
        return estadoHotelRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estado de hotel."));
    }


    @Transactional(readOnly = true)
    public List<CatalogoResponse> estadosEmpleado() {
        return estadoEmpleadoRepo.findAllByOrderByIdAsc().stream()
                .map(e -> new CatalogoResponse(e.getId(), e.getTipoEstadoEmpleado())).toList();
    }

    public CatalogoResponse guardarEstadoEmpleado(Integer id, CatalogoRequest r) {
        String nombre = r.nombre().trim();
        estadoEmpleadoRepo.findByTipoEstadoEmpleadoIgnoreCase(nombre).ifPresent(o -> {
            if (!o.getId().equals(id)) {
                throw new RecursoDuplicadoException("Ya existe el estado de empleado '" + nombre + "'.");
            }
        });
        EstadoEmpleado e = id == null ? new EstadoEmpleado() : buscarEstadoEmpleado(id);
        e.setTipoEstadoEmpleado(nombre);
        e = estadoEmpleadoRepo.save(e);
        return new CatalogoResponse(e.getId(), e.getTipoEstadoEmpleado());
    }

    public void eliminarEstadoEmpleado(Integer id) {
        EstadoEmpleado e = buscarEstadoEmpleado(id);
        if (empleadoRepo.existsByEstadoId(id)) {
            throw new OperacionNoPermitidaException("No se puede eliminar: hay empleados con ese estado.");
        }
        estadoEmpleadoRepo.delete(e);
    }

    public EstadoEmpleado buscarEstadoEmpleado(Integer id) {
        return estadoEmpleadoRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el estado de empleado."));
    }
}
