package com.udb.hotel.dto;

import com.udb.hotel.model.Empleado;

public record EmpleadoResponse(Integer id, String nombreEmpleado, String direccion, String telefono, String email,
                               String tipoEmpleado, Integer estadoId, String estado) {

    public static EmpleadoResponse de(Empleado e) {
        return new EmpleadoResponse(e.getId(), e.getNombreEmpleado(), e.getDireccion(), e.getTelefono(),
                e.getEmail(), e.getTipoEmpleado(), e.getEstado().getId(), e.getEstado().getTipoEstadoEmpleado());
    }
}
