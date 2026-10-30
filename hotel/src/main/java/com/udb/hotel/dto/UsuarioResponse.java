package com.udb.hotel.dto;

import com.udb.hotel.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(Integer id, String alias, String email, String rol, boolean activo,
                              LocalDateTime fechaCreacion, Integer empleadoId, String nombreEmpleado,
                              Integer huespedId, String nombreHuesped) {

    public static UsuarioResponse de(Usuario u) {
        var e = u.getEmpleado();
        var h = u.getHuesped();
        return new UsuarioResponse(u.getId(), u.getAlias(), u.getEmail(), u.getRol().name(), u.isActivo(),
                u.getFechaCreacion(), e != null ? e.getId() : null, e != null ? e.getNombreEmpleado() : null,
                h != null ? h.getId() : null, h != null ? h.getNombreHuesped() : null);
    }
}
