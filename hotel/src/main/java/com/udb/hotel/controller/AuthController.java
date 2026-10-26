package com.udb.hotel.controller;

import com.udb.hotel.dto.LoginRequest;
import com.udb.hotel.dto.LoginResponse;
import com.udb.hotel.model.Usuario;
import com.udb.hotel.repository.UsuarioRepository;
import com.udb.hotel.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository repo;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest r) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(r.username(), r.password()));
        Usuario u = repo.findByUsername(r.username()).orElseThrow();
        return ResponseEntity.ok(
                new LoginResponse(jwtService.generarToken(u), u.getUsername(), u.getRol().name()));
    }
}
