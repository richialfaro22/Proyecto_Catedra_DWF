package com.udb.hotel.controller;

import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.dto.ReclamoRequest;
import com.udb.hotel.dto.ReclamoRespuestaRequest;
import com.udb.hotel.dto.ReclamoResponse;
import com.udb.hotel.model.EstadoReclamo;
import com.udb.hotel.service.ReclamoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reclamos")
@RequiredArgsConstructor
@Tag(name = "Reclamos y solicitudes")
public class ReclamoController {

    private final ReclamoService service;

    @GetMapping
    public List<ReclamoResponse> listar(@RequestParam(required = false) EstadoReclamo estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public ReclamoResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ReclamoResponse> crear(@Valid @RequestBody ReclamoRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @PutMapping("/{id}/respuesta")
    public ReclamoResponse responder(@PathVariable Integer id, @Valid @RequestBody ReclamoRespuestaRequest r) {
        return service.responder(id, r);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Reclamo eliminado correctamente.");
    }
}
