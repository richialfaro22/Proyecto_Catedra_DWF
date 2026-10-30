package com.udb.hotel.controller;

import com.udb.hotel.dto.HotelRequest;
import com.udb.hotel.dto.HotelResponse;
import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.service.HotelService;
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
@RequestMapping("/hoteles")
@RequiredArgsConstructor
@Tag(name = "Hoteles", description = "Hoteles y sucursales (tabla Hotel). Consultar: cualquier usuario; modificar: ADMIN")
public class HotelController {

    private final HotelService service;

    @Operation(summary = "Lista hoteles (soloActivos=true: solo los que están en estado Activo)")
    @GetMapping
    public List<HotelResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return service.listar(soloActivos);
    }

    @Operation(summary = "Ciudades con hoteles activos (para el buscador)")
    @GetMapping("/ciudades")
    public List<String> ciudades() {
        return service.ciudades();
    }

    @GetMapping("/{id}")
    public HotelResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<HotelResponse> crear(@Valid @RequestBody HotelRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public HotelResponse actualizar(@PathVariable Integer id, @Valid @RequestBody HotelRequest r) {
        return service.actualizar(id, r);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Hotel eliminado correctamente.");
    }
}
