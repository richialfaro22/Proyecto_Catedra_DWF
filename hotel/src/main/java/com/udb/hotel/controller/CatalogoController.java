package com.udb.hotel.controller;

import com.udb.hotel.dto.CatalogoRequest;
import com.udb.hotel.dto.CatalogoResponse;
import com.udb.hotel.dto.MensajeResponse;
import com.udb.hotel.service.CatalogoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/catalogos")
@RequiredArgsConstructor
@Tag(name = "Catálogos")
public class CatalogoController {

    private final CatalogoService service;


    @GetMapping("/estados-hotel")
    public List<CatalogoResponse> estadosHotel() {
        return service.estadosHotel();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/estados-hotel")
    public ResponseEntity<CatalogoResponse> crearEstadoHotel(@Valid @RequestBody CatalogoRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarEstadoHotel(null, r));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/estados-hotel/{id}")
    public CatalogoResponse actualizarEstadoHotel(@PathVariable Integer id, @Valid @RequestBody CatalogoRequest r) {
        return service.guardarEstadoHotel(id, r);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/estados-hotel/{id}")
    public MensajeResponse eliminarEstadoHotel(@PathVariable Integer id) {
        service.eliminarEstadoHotel(id);
        return new MensajeResponse("Estado eliminado correctamente.");
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estados-empleado")
    public List<CatalogoResponse> estadosEmpleado() {
        return service.estadosEmpleado();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/estados-empleado")
    public ResponseEntity<CatalogoResponse> crearEstadoEmpleado(@Valid @RequestBody CatalogoRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarEstadoEmpleado(null, r));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/estados-empleado/{id}")
    public CatalogoResponse actualizarEstadoEmpleado(@PathVariable Integer id, @Valid @RequestBody CatalogoRequest r) {
        return service.guardarEstadoEmpleado(id, r);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/estados-empleado/{id}")
    public MensajeResponse eliminarEstadoEmpleado(@PathVariable Integer id) {
        service.eliminarEstadoEmpleado(id);
        return new MensajeResponse("Estado eliminado correctamente.");
    }
}
