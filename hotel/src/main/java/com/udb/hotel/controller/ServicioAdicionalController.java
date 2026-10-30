package com.udb.hotel.controller;

import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.dto.ServicioRequest;
import com.udb.hotel.dto.ServicioResponse;
import com.udb.hotel.service.ServicioAdicionalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicios")
@RequiredArgsConstructor
@Tag(name = "Servicios adicionales")
public class ServicioAdicionalController {

    private final ServicioAdicionalService service;

    @GetMapping
    public List<ServicioResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return service.listar(soloActivos);
    }

    @GetMapping("/{id}")
    public ServicioResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServicioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ServicioRequest r) {
        return service.actualizar(id, r);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activar")
    public ServicioResponse activar(@PathVariable Integer id) {
        return service.cambiarEstado(id, true);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desactivar")
    public ServicioResponse desactivar(@PathVariable Integer id) {
        return service.cambiarEstado(id, false);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Servicio eliminado correctamente.");
    }
}
