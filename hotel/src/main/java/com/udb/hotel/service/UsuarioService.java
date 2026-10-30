package com.udb.hotel.service;

import com.udb.hotel.dto.UsuarioRequest;
import com.udb.hotel.dto.UsuarioResponse;
import com.udb.hotel.dto.UsuarioUpdateRequest;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.Rol;
import com.udb.hotel.model.Usuario;
import com.udb.hotel.repository.HuespedRepository;
import com.udb.hotel.repository.ReservaRepository;
import com.udb.hotel.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Administración de usuarios: creación, actualización, eliminación, activación y desactivación. */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository repo;
    private final ReservaRepository reservaRepo;
    private final HuespedRepository huespedRepo;
    private final EmpleadoService empleadoService;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repo.listarTodos().stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id) {
        return UsuarioResponse.de(buscar(id));
    }

    public UsuarioResponse crear(UsuarioRequest r) {
        validarUnicos(r.alias(), r.email(), null);
        Usuario u = Usuario.builder()
                .alias(r.alias().trim())
                .email(r.email().trim().toLowerCase())
                .contrasena(encoder.encode(r.contrasena()))
                .rol(r.rol())
                .activo(true)
                .build();
        vincular(u, r.rol(), r.empleadoId(), r.huespedId());
        return UsuarioResponse.de(repo.save(u));
    }

    public UsuarioResponse actualizar(Integer id, UsuarioUpdateRequest r, String aliasActual) {
        Usuario u = buscar(id);
        validarUnicos(r.alias(), r.email(), id);

        boolean esElMismo = u.getAlias().equalsIgnoreCase(aliasActual);
        if (esElMismo && r.rol() != u.getRol()) {
            throw new OperacionNoPermitidaException("No puedes cambiar tu propio rol.");
        }
        u.setAlias(r.alias().trim());
        u.setEmail(r.email().trim().toLowerCase());
        u.setRol(r.rol());
        if (r.contrasena() != null && !r.contrasena().isBlank()) {
            u.setContrasena(encoder.encode(r.contrasena()));
        }
        vincular(u, r.rol(), r.empleadoId(), r.huespedId());
        return UsuarioResponse.de(repo.save(u));
    }

    public void eliminar(Integer id, String aliasActual) {
        Usuario u = buscar(id);
        if (u.getAlias().equalsIgnoreCase(aliasActual)) {
            throw new OperacionNoPermitidaException("No puedes eliminar tu propio usuario.");
        }
        if (reservaRepo.existsByUsuarioId(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el usuario porque registró reservas. Puedes desactivarlo en su lugar.");
        }
        repo.delete(u);
    }

    public UsuarioResponse cambiarEstado(Integer id, boolean activo, String aliasActual) {
        Usuario u = buscar(id);
        if (!activo && u.getAlias().equalsIgnoreCase(aliasActual)) {
            throw new OperacionNoPermitidaException("No puedes desactivar tu propio usuario.");
        }
        u.setActivo(activo);
        return UsuarioResponse.de(repo.save(u));
    }

    private void vincular(Usuario u, Rol rol, Integer empleadoId, Integer huespedId) {
        if (rol == Rol.HUESPED) {
            u.setEmpleado(null);
            if (huespedId != null) {
                var h = huespedRepo.findById(huespedId)
                        .orElseThrow(() -> new ValidacionException("huespedId", "El huésped seleccionado no existe."));
                repo.findFirstByHuespedId(huespedId).ifPresent(o -> {
                    if (!o.getId().equals(u.getId())) {
                        throw new ValidacionException("huespedId", "Ese huésped ya tiene una cuenta de usuario.");
                    }
                });
                u.setHuesped(h);
            }
        } else {
            u.setHuesped(null);
            u.setEmpleado(empleadoId == null ? null : empleadoService.buscar(empleadoId));
        }
    }

    private void validarUnicos(String alias, String email, Integer idActual) {
        repo.findByAliasIgnoreCase(alias.trim()).ifPresent(o -> {
            if (!o.getId().equals(idActual)) {
                throw new ValidacionException("alias", "Ya existe un usuario con ese alias.");
            }
        });
        repo.findByEmailIgnoreCase(email.trim()).ifPresent(o -> {
            if (!o.getId().equals(idActual)) {
                throw new ValidacionException("email", "Ya existe un usuario con ese correo.");
            }
        });
    }

    private Usuario buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario solicitado."));
    }
}
