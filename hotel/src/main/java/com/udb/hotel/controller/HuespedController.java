package com.udb.hotel.controller;

import com.udb.hotel.dto.HuespedRequest;
import com.udb.hotel.dto.HuespedResponse;
import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.service.HuespedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/huespedes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
@Tag(name = "Huéspedes")
public class HuespedController {

    private final HuespedService service;

    @Operation(summary = "Lista huéspedes; q filtra por nombre, apellido, documento o correo")
    @GetMapping
    public List<HuespedResponse> listar(@RequestParam(required = false) String q) {
        return service.listar(q);
    }

    @GetMapping("/{id}")
    public HuespedResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<HuespedResponse> crear(@Valid @RequestBody HuespedRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public HuespedResponse actualizar(@PathVariable Integer id, @Valid @RequestBody HuespedRequest r) {
        return service.actualizar(id, r);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Huésped eliminado correctamente.");
    }
}
