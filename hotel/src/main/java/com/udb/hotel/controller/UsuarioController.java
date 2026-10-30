package com.udb.hotel.controller;

import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.dto.UsuarioRequest;
import com.udb.hotel.dto.UsuarioResponse;
import com.udb.hotel.dto.UsuarioUpdateRequest;
import com.udb.hotel.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Solo el ADMIN puede usar estos endpoints. */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuarios", description = "Administración de usuarios (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Integer id,
                                      @Valid @RequestBody UsuarioUpdateRequest r,
                                      Authentication auth) {
        return service.actualizar(id, r, auth.getName());
    }

    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id, Authentication auth) {
        service.eliminar(id, auth.getName());
        return new MensajeResponse("Usuario eliminado correctamente.");
    }

    @PatchMapping("/{id}/activar")
    public UsuarioResponse activar(@PathVariable Integer id, Authentication auth) {
        return service.cambiarEstado(id, true, auth.getName());
    }

    @PatchMapping("/{id}/desactivar")
    public UsuarioResponse desactivar(@PathVariable Integer id, Authentication auth) {
        return service.cambiarEstado(id, false, auth.getName());
    }
}
