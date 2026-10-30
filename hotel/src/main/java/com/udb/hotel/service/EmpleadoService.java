package com.udb.hotel.service;

import com.udb.hotel.dto.EmpleadoRequest;
import com.udb.hotel.dto.EmpleadoResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.model.Empleado;
import com.udb.hotel.repository.EmpleadoRepository;
import com.udb.hotel.repository.HotelRepository;
import com.udb.hotel.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpleadoService {

    private final EmpleadoRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final HotelRepository hotelRepo;
    private final CatalogoService catalogoService;

    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listar() {
        return repo.listarTodos().stream().map(EmpleadoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public EmpleadoResponse obtener(Integer id) {
        return EmpleadoResponse.de(buscar(id));
    }

    public EmpleadoResponse crear(EmpleadoRequest r) {
        validarEmail(r.email(), null);
        Empleado e = new Empleado();
        copiar(r, e);
        return EmpleadoResponse.de(repo.save(e));
    }

    public EmpleadoResponse actualizar(Integer id, EmpleadoRequest r) {
        Empleado e = buscar(id);
        validarEmail(r.email(), id);
        copiar(r, e);
        return EmpleadoResponse.de(repo.save(e));
    }

    public void eliminar(Integer id) {
        Empleado e = buscar(id);
        if (usuarioRepo.existsByEmpleadoId(id) || hotelRepo.existsByEmpleadoId(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar: el empleado tiene una cuenta de usuario o hoteles a su cargo. "
                    + "Puedes cambiar su estado a Inactivo.");
        }
        repo.delete(e);
    }

    public Empleado buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el empleado solicitado."));
    }

    private void validarEmail(String email, Integer idActual) {
        if (email == null || email.isBlank()) {
            return;
        }
        repo.findByEmailIgnoreCase(email.trim()).ifPresent(o -> {
            if (!o.getId().equals(idActual)) {
                throw new RecursoDuplicadoException("Ya existe un empleado con el correo " + email.trim() + ".");
            }
        });
    }

    private void copiar(EmpleadoRequest r, Empleado e) {
        e.setNombreEmpleado(r.nombreEmpleado().trim());
        e.setDireccion(vacio(r.direccion()));
        e.setTelefono(vacio(r.telefono()));
        e.setEmail(r.email() == null || r.email().isBlank() ? null : r.email().trim().toLowerCase());
        e.setTipoEmpleado(r.tipoEmpleado().trim());
        e.setEstado(catalogoService.buscarEstadoEmpleado(r.estadoId()));
    }

    private static String vacio(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
