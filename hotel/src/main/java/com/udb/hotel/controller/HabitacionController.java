package com.udb.hotel.controller;

import com.udb.hotel.dto.HabitacionRequest;
import com.udb.hotel.dto.HabitacionResponse;
import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.service.HabitacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/habitaciones")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
@Tag(name = "Habitaciones")
public class HabitacionController {

    private final HabitacionService service;

    @GetMapping
    public List<HabitacionResponse> listar(@RequestParam(required = false) Integer hotelId) {
        return service.listar(hotelId);
    }

    @GetMapping("/{id}")
    public HabitacionResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<HabitacionResponse> crear(@Valid @RequestBody HabitacionRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public HabitacionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody HabitacionRequest r) {
        return service.actualizar(id, r);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Habitación eliminada correctamente.");
    }
}
