package com.udb.hotel.service;

import com.udb.hotel.dto.HotelRequest;
import com.udb.hotel.dto.HotelResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.model.Hotel;
import com.udb.hotel.repository.HabitacionRepository;
import com.udb.hotel.repository.HotelRepository;
import com.udb.hotel.repository.ReservaRepository;
import com.udb.hotel.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelService {

    private final HotelRepository repo;
    private final HabitacionRepository habitacionRepo;
    private final TarifaRepository tarifaRepo;
    private final ReservaRepository reservaRepo;
    private final CatalogoService catalogoService;
    private final EmpleadoService empleadoService;

    @Transactional(readOnly = true)
    public List<HotelResponse> listar(boolean soloActivos) {
        return repo.listarTodos().stream()
                .filter(h -> !soloActivos || h.getEstado().permiteReservas())
                .map(HotelResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<String> ciudades() {
        return repo.ciudadesActivas();
    }

    @Transactional(readOnly = true)
    public HotelResponse obtener(Integer id) {
        return HotelResponse.de(buscar(id));
    }

    public HotelResponse crear(HotelRequest r) {
        validarNombre(r.nombreHotel(), null);
        Hotel h = new Hotel();
        copiar(r, h);
        return HotelResponse.de(repo.save(h));
    }

    public HotelResponse actualizar(Integer id, HotelRequest r) {
        Hotel h = buscar(id);
        validarNombre(r.nombreHotel(), id);
        copiar(r, h);
        return HotelResponse.de(repo.save(h));
    }

    public void eliminar(Integer id) {
        Hotel h = buscar(id);
        if (habitacionRepo.existsByHotelId(id) || tarifaRepo.existsByHotelId(id) || reservaRepo.existsByHotelId(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el hotel porque tiene habitaciones, tarifas o reservas. "
                    + "Cambia su estado a Inactivo en su lugar.");
        }
        repo.delete(h);
    }

    public Hotel buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el hotel solicitado."));
    }

    private void validarNombre(String nombre, Integer idActual) {
        repo.findByNombreHotelIgnoreCase(nombre.trim()).ifPresent(otro -> {
            if (!otro.getId().equals(idActual)) {
                throw new RecursoDuplicadoException("Ya existe un hotel con el nombre '" + nombre.trim() + "'.");
            }
        });
    }

    private void copiar(HotelRequest r, Hotel h) {
        h.setNombreHotel(r.nombreHotel().trim());
        h.setCiudad(r.ciudad().trim());
        h.setDireccion(r.direccion().trim());
        h.setTelefono(r.telefono().trim());
        h.setEmail(r.email() == null || r.email().isBlank() ? null : r.email().trim().toLowerCase());
        h.setDescripcion(r.descripcion());
        h.setEstrellas(r.estrellas());
        h.setHorasCancelacionGratis(r.horasCancelacionGratis());
        h.setPorcentajePenalizacion(r.porcentajePenalizacion());
        h.setEstado(catalogoService.buscarEstadoHotel(r.estadoId()));
        h.setEmpleado(r.empleadoId() == null ? null : empleadoService.buscar(r.empleadoId()));
    }
}
