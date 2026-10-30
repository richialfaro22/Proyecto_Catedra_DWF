package com.udb.hotel.controller;

import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.dto.TarifaRequest;
import com.udb.hotel.dto.TarifaResponse;
import com.udb.hotel.service.TarifaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tarifas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Tarifas", description = "Tarifas de temporada por hotel y tipo de habitación (solo ADMIN)")
public class TarifaController {

    private final TarifaService service;

    @GetMapping
    public List<TarifaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public TarifaResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<TarifaResponse> crear(@Valid @RequestBody TarifaRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PutMapping("/{id}")
    public TarifaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TarifaRequest r) {
        return service.actualizar(id, r);
    }

    @PatchMapping("/{id}/activar")
    public TarifaResponse activar(@PathVariable Integer id) {
        return service.cambiarEstado(id, true);
    }

    @PatchMapping("/{id}/desactivar")
    public TarifaResponse desactivar(@PathVariable Integer id) {
        return service.cambiarEstado(id, false);
    }

    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Tarifa eliminada correctamente.");
    }
}
