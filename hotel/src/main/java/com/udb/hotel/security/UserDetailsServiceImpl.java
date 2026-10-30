package com.udb.hotel.security;

import com.udb.hotel.model.Usuario;
import com.udb.hotel.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository repo;

    @Override
    public UserDetails loadUserByUsername(String aliasOEmail) {
        Usuario u = repo.buscarPorAliasOEmail(aliasOEmail.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.builder()
                .username(u.getAlias())
                .password(u.getContrasena())
                .roles(u.getRol().name())
                .disabled(!u.isActivo())
                .build();
    }
}
