package com.udb.hotel.controller;

import com.udb.hotel.dto.*;
import com.udb.hotel.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login (alias o correo), registro de huéspedes y perfil del usuario")
public class AuthController {

    private final AuthService service;

    @Operation(summary = "Inicia sesión con alias o correo y devuelve el token JWT (público)")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) {
        return service.login(r);
    }

    @Operation(summary = "Registra un huésped con su cuenta de usuario (público)")
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody RegistroRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(r));
    }

    @Operation(summary = "Datos del usuario autenticado y su ficha de huésped")
    @GetMapping("/perfil")
    public PerfilResponse perfil() {
        return service.perfil();
    }

    @Operation(summary = "Crea o actualiza la ficha de huésped del usuario autenticado")
    @PutMapping("/perfil")
    public PerfilResponse actualizarPerfil(@Valid @RequestBody HuespedRequest r) {
        return service.actualizarPerfil(r);
    }
}
