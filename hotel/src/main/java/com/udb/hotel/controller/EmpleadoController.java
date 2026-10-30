package com.udb.hotel.controller;

import com.udb.hotel.dto.EmpleadoRequest;
import com.udb.hotel.dto.EmpleadoResponse;
import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.service.EmpleadoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empleados")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Empleados")
public class EmpleadoController {

    private final EmpleadoService service;

    @GetMapping
    public List<EmpleadoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EmpleadoResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody EmpleadoRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public EmpleadoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody EmpleadoRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Empleado eliminado correctamente.");
    }
}
