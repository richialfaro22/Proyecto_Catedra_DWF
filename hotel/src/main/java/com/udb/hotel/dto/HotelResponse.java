package com.udb.hotel.dto;

import com.udb.hotel.model.Hotel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HotelResponse(Integer id, String nombreHotel, String ciudad, String direccion, String telefono,
                            String email, String descripcion, Integer estrellas, Integer horasCancelacionGratis,
                            BigDecimal porcentajePenalizacion, LocalDateTime fechaRegistro, Integer estadoId,
                            String estado, boolean permiteReservas, Integer empleadoId, String nombreEmpleado) {

    public static HotelResponse de(Hotel h) {
        var e = h.getEmpleado();
        return new HotelResponse(h.getId(), h.getNombreHotel(), h.getCiudad(), h.getDireccion(), h.getTelefono(),
                h.getEmail(), h.getDescripcion(), h.getEstrellas(), h.getHorasCancelacionGratis(),
                h.getPorcentajePenalizacion(), h.getFechaRegistro(), h.getEstado().getId(),
                h.getEstado().getEstadoHotel(), h.getEstado().permiteReservas(),
                e != null ? e.getId() : null, e != null ? e.getNombreEmpleado() : null);
    }
}
