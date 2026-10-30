package com.udb.hotel.controller;

import com.udb.hotel.dto.OpcionDisponibleResponse;
import com.udb.hotel.service.DisponibilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/disponibilidad")
@RequiredArgsConstructor
@Tag(name = "Disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService service;

    @Operation(summary = "Busca habitaciones libres por ciudad, fechas, huéspedes y tipo")
    @GetMapping
    public List<OpcionDisponibleResponse> buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "Debe haber al menos 1 huésped")
            @Max(value = 10, message = "El máximo es 10 huéspedes por habitación") int huespedes,
            @RequestParam(required = false) Integer tipoId,
            @RequestParam(required = false) Integer hotelId) {
        return service.buscar(ciudad, fechaEntrada, fechaSalida, huespedes, tipoId, hotelId);
    }
}
