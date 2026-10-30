package com.udb.hotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ReservaRequest(
        @NotNull(message = "Debes seleccionar una habitación")
        @Positive(message = "La habitación no es válida")
        Integer habitacionId,

        @NotNull(message = "La fecha de entrada es obligatoria")
        @FutureOrPresent(message = "La fecha de entrada debe ser hoy o una fecha futura")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria")
        @Future(message = "La fecha de salida debe ser posterior a hoy")
        LocalDate fechaSalida,

        @NotNull(message = "El número de huéspedes es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 huésped")
        @Max(value = 10, message = "El máximo es 10 huéspedes por habitación")
        Integer numPersonas,

        @Size(max = 1000, message = "Las solicitudes especiales no pueden superar 1000 caracteres")
        String observaciones,

        @NotNull(message = "Los datos del huésped son obligatorios")
        @Valid
        HuespedRequest huesped,

        @Size(max = 15, message = "No puedes agregar más de 15 servicios")
        List<@Valid ServicioSolicitado> servicios) {
}
