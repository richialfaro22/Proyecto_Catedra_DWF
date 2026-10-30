package com.udb.hotel.dto;

import com.udb.hotel.model.TipoDocumento;
import com.udb.hotel.validation.MayorDeEdad;
import com.udb.hotel.validation.Patrones;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Datos de un huésped (paso 3 de la reserva). El formato de {@code documento} se valida en el
 * servicio según el tipo (DUI: 00000000-0, pasaporte: 6 a 12 caracteres alfanuméricos).
 */
public record HuespedRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 5, max = 150, message = "El nombre completo debe tener entre 5 y 150 caracteres")
        @Pattern(regexp = Patrones.NOMBRE_COMPLETO, message = "Escribe nombre y apellido usando solo letras")
        String nombreHuesped,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        @MayorDeEdad
        LocalDate fechaNacimiento,

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El documento de identidad o pasaporte es obligatorio")
        @Size(max = 30, message = "El documento no puede superar 30 caracteres")
        String documento,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = Patrones.TELEFONO, message = Patrones.MSG_TELEFONO)
        String telefono,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres")
        String email,

        @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
        String direccion,

        @Size(max = 50, message = "La nacionalidad no puede superar 50 caracteres")
        @Pattern(regexp = Patrones.NOMBRE_OPCIONAL, message = "La nacionalidad " + Patrones.MSG_NOMBRE)
        String nacionalidad) {
}
