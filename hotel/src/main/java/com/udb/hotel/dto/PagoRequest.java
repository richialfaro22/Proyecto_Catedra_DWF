package com.udb.hotel.dto;

import com.udb.hotel.model.MetodoPago;
import com.udb.hotel.validation.Patrones;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Pago de una reserva (paso 5). Si {@code totalPagar} no se envía se cobra el saldo pendiente.
 * Los datos de tarjeta solo son obligatorios con el método TARJETA (se valida en el servicio);
 * nunca se guarda el número completo ni el CVV.
 */
public record PagoRequest(
        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago,

        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a $0.00")
        @Digits(integer = 8, fraction = 2, message = "El monto admite como máximo 2 decimales")
        BigDecimal totalPagar,

        @Pattern(regexp = Patrones.TARJETA_OPCIONAL, message = "El número de tarjeta debe tener 16 dígitos")
        String numeroTarjeta,

        @Size(max = 100, message = "El nombre del titular no puede superar 100 caracteres")
        @Pattern(regexp = Patrones.NOMBRE_OPCIONAL, message = "El titular " + Patrones.MSG_NOMBRE)
        String titular,

        @Pattern(regexp = Patrones.VENCIMIENTO_OPCIONAL, message = "El vencimiento debe tener el formato MM/AA")
        String vencimiento,

        @Pattern(regexp = Patrones.CVV_OPCIONAL, message = "El CVV debe tener 3 o 4 dígitos")
        String cvv,

        @Size(max = 30, message = "La referencia no puede superar 30 caracteres")
        @Pattern(regexp = "^$|^[A-Za-z0-9-]+$", message = "La referencia solo puede contener letras, números y guiones")
        String referenciaTransferencia) {
}
