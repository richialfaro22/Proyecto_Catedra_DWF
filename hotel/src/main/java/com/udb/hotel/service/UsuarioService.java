package com.udb.hotel.service;

import com.udb.hotel.dto.UsuarioRequest;
import com.udb.hotel.dto.UsuarioResponse;
import com.udb.hotel.dto.UsuarioUpdateRequest;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.model.Usuario;
import com.udb.hotel.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public List<UsuarioResponse> listar() {
        return repo.findAll().stream().map(UsuarioResponse::de).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    public UsuarioResponse crear(UsuarioRequest r) {
        if (repo.findByUsername(r.username()).isPresent()) {
            throw new RecursoDuplicadoException("Ya existe un usuario con ese nombre.");
        }
        Usuario u = Usuario.builder()
                .username(r.username())
                .password(encoder.encode(r.password()))
                .rol(r.rol())
                .activo(true)
                .build();
        return UsuarioResponse.de(repo.save(u));
    }

    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest r, String usernameActual) {
        Usuario u = buscar(id);

        repo.findByUsername(r.username()).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new RecursoDuplicadoException("Ya existe un usuario con ese nombre.");
            }
        });

        boolean esElMismo = u.getUsername().equals(usernameActual);
        if (esElMismo && r.rol() != u.getRol()) {
            throw new OperacionNoPermitidaException("No puedes cambiar tu propio rol.");
        }

        u.setUsername(r.username());
        u.setRol(r.rol());
        if (r.password() != null && !r.password().isBlank()) {
            u.setPassword(encoder.encode(r.password()));
        }
        return UsuarioResponse.de(repo.save(u));
    }

    public void eliminar(Long id, String usernameActual) {
        Usuario u = buscar(id);
        if (u.getUsername().equals(usernameActual)) {
            throw new OperacionNoPermitidaException("No puedes eliminar tu propio usuario.");
        }
        repo.delete(u);
    }

    public UsuarioResponse cambiarEstado(Long id, boolean activo, String usernameActual) {
        Usuario u = buscar(id);
        if (!activo && u.getUsername().equals(usernameActual)) {
            throw new OperacionNoPermitidaException("No puedes desactivar tu propio usuario.");
        }
        u.setActivo(activo);
        return UsuarioResponse.de(repo.save(u));
    }

    private Usuario buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario solicitado."));
    }
}
