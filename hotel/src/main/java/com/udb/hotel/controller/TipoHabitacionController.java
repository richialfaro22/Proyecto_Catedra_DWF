package com.udb.hotel.controller;

import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.dto.TipoHabitacionRequest;
import com.udb.hotel.dto.TipoHabitacionResponse;
import com.udb.hotel.service.TipoHabitacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos-habitacion")
@RequiredArgsConstructor
@Tag(name = "Tipos de habitación", description = "Consultar: cualquier usuario; modificar: ADMIN")
public class TipoHabitacionController {

    private final TipoHabitacionService service;

    @GetMapping
    public List<TipoHabitacionResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return service.listar(soloActivos);
    }

    @GetMapping("/{id}")
    public TipoHabitacionResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TipoHabitacionResponse> crear(@Valid @RequestBody TipoHabitacionRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TipoHabitacionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoHabitacionRequest r) {
        return service.actualizar(id, r);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activar")
    public TipoHabitacionResponse activar(@PathVariable Integer id) {
        return service.cambiarEstado(id, true);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desactivar")
    public TipoHabitacionResponse desactivar(@PathVariable Integer id) {
        return service.cambiarEstado(id, false);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Tipo de habitación eliminado correctamente.");
    }
}
