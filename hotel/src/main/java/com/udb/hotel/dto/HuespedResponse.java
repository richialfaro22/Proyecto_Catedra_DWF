package com.udb.hotel.dto;

import com.udb.hotel.model.Huesped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public record HuespedResponse(Integer id, String nombreHuesped, LocalDate fechaNacimiento, Integer edad,
                              String tipoDocumento, String documento, String telefono, String email,
                              String direccion, String nacionalidad, LocalDateTime fechaRegistro) {

    public static HuespedResponse de(Huesped h) {
        return new HuespedResponse(h.getId(), h.getNombreHuesped(), h.getFechaNacimiento(),
                Period.between(h.getFechaNacimiento(), LocalDate.now()).getYears(), h.getTipoDocumento().name(),
                h.getDocumento(), h.getTelefono(), h.getEmail(), h.getDireccion(), h.getNacionalidad(),
                h.getFechaRegistro());
    }
}
