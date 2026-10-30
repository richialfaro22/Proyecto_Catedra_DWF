package com.udb.hotel.service;

import com.udb.hotel.dto.*;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.Huesped;
import com.udb.hotel.model.Rol;
import com.udb.hotel.model.Usuario;
import com.udb.hotel.repository.UsuarioRepository;
import com.udb.hotel.security.JwtService;
import com.udb.hotel.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepo;
    private final HuespedService huespedService;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final UsuarioActual usuarioActual;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest r) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(r.usuario().trim(), r.contrasena()));
        Usuario u = usuarioRepo.buscarPorAliasOEmail(r.usuario().trim()).orElseThrow();
        return respuestaLogin(u);
    }

    public LoginResponse registrar(RegistroRequest r) {
        if (usuarioRepo.findByAliasIgnoreCase(r.alias().trim()).isPresent()) {
            throw new ValidacionException("alias", "El alias '" + r.alias().trim() + "' ya está en uso.");
        }
        if (usuarioRepo.findByEmailIgnoreCase(r.huesped().email().trim()).isPresent()) {
            throw new ValidacionException("huesped.email", "Ya existe una cuenta registrada con ese correo.");
        }
        Huesped h = huespedService.crearEntidad(r.huesped(), "huesped.documento");
        Usuario u = usuarioRepo.save(Usuario.builder()
                .alias(r.alias().trim())
                .contrasena(encoder.encode(r.contrasena()))
                .email(h.getEmail())
                .rol(Rol.HUESPED)
                .huesped(h)
                .activo(true)
                .build());
        return respuestaLogin(u);
    }

    @Transactional(readOnly = true)
    public PerfilResponse perfil() {
        Usuario u = usuarioActual.entidad();
        HuespedResponse h = u.getHuesped() != null ? HuespedResponse.de(u.getHuesped()) : null;
        return new PerfilResponse(UsuarioResponse.de(u), h);
    }


    public PerfilResponse actualizarPerfil(HuespedRequest r) {
        Usuario u = usuarioActual.entidad();
        Huesped h = u.getHuesped() != null
                ? huespedService.actualizarEntidad(u.getHuesped(), r, "documento")
                : huespedService.crearEntidad(r, "documento");
        u.setHuesped(h);
        usuarioRepo.save(u);
        return new PerfilResponse(UsuarioResponse.de(u), HuespedResponse.de(h));
    }

    private LoginResponse respuestaLogin(Usuario u) {
        return new LoginResponse(jwtService.generarToken(u), u.getAlias(), u.getRol().name(),
                jwtService.getExpiration() / 1000);
    }
}
