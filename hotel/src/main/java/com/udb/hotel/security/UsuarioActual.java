package com.udb.hotel.security;

import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.model.Rol;
import com.udb.hotel.model.Usuario;
import com.udb.hotel.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioActual {

    private final UsuarioRepository usuarioRepo;

    public String alias() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public boolean tieneRol(Rol rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol.name()));
    }

    public boolean esPersonal() {
        return tieneRol(Rol.ADMIN) || tieneRol(Rol.RECEPCIONISTA);
    }

    public Usuario entidad() {
        return usuarioRepo.findByAliasIgnoreCase(alias())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró tu usuario."));
    }
}
