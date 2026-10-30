package com.udb.hotel.controller;

import com.udb.hotel.dto.*;
import com.udb.hotel.model.EstadoReserva;
import com.udb.hotel.service.PagoService;
import com.udb.hotel.service.ReservaService;
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
@RequestMapping("/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Pasos 3 a 6: reserva, resumen, pago, modificación y cancelación")
public class ReservaController {

    private final ReservaService service;
    private final PagoService pagoService;

    @Operation(summary = "Lista reservas (el huésped solo ve las suyas)")
    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) EstadoReserva estado,
                                        @RequestParam(required = false) Integer hotelId) {
        return service.listar(estado, hotelId);
    }

    @GetMapping("/{id}")
    public ReservaResponse obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @Operation(summary = "Busca una reserva por su código (ej. R261028-AB12)")
    @GetMapping("/codigo/{codigo}")
    public ReservaResponse porCodigo(@PathVariable String codigo) {
        return service.obtenerPorCodigo(codigo);
    }

    @Operation(summary = "Crea la reserva (queda PENDIENTE) y devuelve el resumen")
    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }

    @Operation(summary = "Modifica fechas, huéspedes o servicios recalculando el total")
    @PutMapping("/{id}")
    public ReservaResponse modificar(@PathVariable Integer id, @Valid @RequestBody ReservaModificarRequest r) {
        return service.modificar(id, r);
    }

    @Operation(summary = "Calcula el cargo y reembolso si se cancela ahora (no cancela)")
    @GetMapping("/{id}/cancelacion")
    public CotizacionCancelacionResponse cotizarCancelacion(@PathVariable Integer id) {
        return service.cotizarCancelacion(id);
    }

    @Operation(summary = "Cancela la reserva aplicando la política del hotel")
    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable Integer id, @Valid @RequestBody CancelacionRequest r) {
        return service.cancelar(id, r);
    }

    @Operation(summary = "Registra un pago de la reserva (paso 5)")
    @PostMapping("/{id}/pagos")
    public ResponseEntity<PagoResponse> pagar(@PathVariable Integer id, @Valid @RequestBody PagoRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.pagar(id, r));
    }

    @GetMapping("/{id}/pagos")
    public List<PagoResponse> pagos(@PathVariable Integer id) {
        return pagoService.listarDeReserva(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @PatchMapping("/{id}/check-in")
    public ReservaResponse checkIn(@PathVariable Integer id) {
        return service.checkIn(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @PatchMapping("/{id}/check-out")
    public ReservaResponse checkOut(@PathVariable Integer id) {
        return service.checkOut(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public MensajeResponse eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return new MensajeResponse("Reserva eliminada correctamente.");
    }
}
