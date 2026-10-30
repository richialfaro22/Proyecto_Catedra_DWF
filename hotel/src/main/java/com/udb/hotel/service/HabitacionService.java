package com.udb.hotel.service;

import com.udb.hotel.dto.HabitacionRequest;
import com.udb.hotel.dto.HabitacionResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.model.Habitacion;
import com.udb.hotel.repository.HabitacionRepository;
import com.udb.hotel.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitacionService {

    private final HabitacionRepository repo;
    private final ReservaRepository reservaRepo;
    private final HotelService hotelService;
    private final TipoHabitacionService tipoService;

    @Transactional(readOnly = true)
    public List<HabitacionResponse> listar(Integer hotelId) {
        var lista = hotelId == null ? repo.listarTodas() : repo.listarPorHotel(hotelId);
        return lista.stream().map(HabitacionResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public HabitacionResponse obtener(Integer id) {
        return HabitacionResponse.de(buscar(id));
    }

    public HabitacionResponse crear(HabitacionRequest r) {
        validarNumero(r.hotelId(), r.numeroHabitacion(), null);
        Habitacion h = new Habitacion();
        copiar(r, h);
        return HabitacionResponse.de(repo.save(h));
    }

    public HabitacionResponse actualizar(Integer id, HabitacionRequest r) {
        Habitacion h = buscar(id);
        validarNumero(r.hotelId(), r.numeroHabitacion(), id);
        boolean tieneReservas = reservaRepo.contarDetallesDeHabitacion(id) > 0;
        if (!h.getHotel().getId().equals(r.hotelId()) && tieneReservas) {
            throw new OperacionNoPermitidaException("No puedes cambiar de hotel una habitación que ya tiene reservas.");
        }
        copiar(r, h);
        return HabitacionResponse.de(repo.save(h));
    }

    public void eliminar(Integer id) {
        Habitacion h = buscar(id);
        if (reservaRepo.contarDetallesDeHabitacion(id) > 0) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar la habitación porque tiene reservas registradas. "
                    + "Cambia su estado a FUERA_DE_SERVICIO en su lugar.");
        }
        repo.delete(h);
    }

    public Habitacion buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la habitación solicitada."));
    }

    private void validarNumero(Integer hotelId, String numero, Integer idActual) {
        repo.findByHotelIdAndNumeroHabitacionIgnoreCase(hotelId, numero.trim()).ifPresent(otra -> {
            if (!otra.getId().equals(idActual)) {
                throw new RecursoDuplicadoException("El hotel ya tiene una habitación con el número " + numero.trim() + ".");
            }
        });
    }

    private void copiar(HabitacionRequest r, Habitacion h) {
        h.setHotel(hotelService.buscar(r.hotelId()));
        h.setTipo(tipoService.buscar(r.tipoId()));
        h.setNumeroHabitacion(r.numeroHabitacion().trim().toUpperCase());
        h.setNumPiso(r.numPiso());
        h.setPrecioBase(r.precioBase());
        h.setDescripcion(r.descripcion());
        h.setEstado(r.estado());
    }
}
