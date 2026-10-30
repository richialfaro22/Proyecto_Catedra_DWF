package com.udb.hotel.dto;

import com.udb.hotel.model.CategoriaServicio;
import com.udb.hotel.model.TipoCobro;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServicioRequest(
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombreServicio,

        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String descripcion,

        @NotNull(message = "La categoría es obligatoria")
        CategoriaServicio categoria,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        @DecimalMax(value = "9999.99", message = "El precio es demasiado alto")
        @Digits(integer = 4, fraction = 2, message = "El precio admite como máximo 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El tipo de cobro es obligatorio")
        TipoCobro tipoCobro) {
}
