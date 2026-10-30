package com.udb.hotel.dto;

import com.udb.hotel.model.ServicioAdicional;

import java.math.BigDecimal;

public record ServicioResponse(Integer id, String nombreServicio, String descripcion, String categoria,
                               BigDecimal precio, String tipoCobro, boolean activo) {

    public static ServicioResponse de(ServicioAdicional s) {
        return new ServicioResponse(s.getId(), s.getNombreServicio(), s.getDescripcion(), s.getCategoria().name(),
                s.getPrecio(), s.getTipoCobro().name(), s.isActivo());
    }
}
