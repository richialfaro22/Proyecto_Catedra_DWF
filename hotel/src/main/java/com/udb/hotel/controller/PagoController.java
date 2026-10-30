package com.udb.hotel.controller;

import com.udb.hotel.dto.PagoResponse;
import com.udb.hotel.service.PagoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
@Tag(name = "Pagos")
public class PagoController {

    private final PagoService service;

    @GetMapping
    public List<PagoResponse> listar() {
        return service.listar();
    }
}
