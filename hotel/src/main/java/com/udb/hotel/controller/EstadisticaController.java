package com.udb.hotel.controller;

import com.udb.hotel.dto.EstadisticasResponse;
import com.udb.hotel.service.EstadisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/estadisticas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
@Tag(name = "Estadísticas")
public class EstadisticaController {

    private final EstadisticaService service;

    @Operation(summary = "Estadísticas del período [desde, hasta] (por defecto, el mes actual)")
    @GetMapping
    public EstadisticasResponse generar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.generar(desde, hasta);
    }
}
