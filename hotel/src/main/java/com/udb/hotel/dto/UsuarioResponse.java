package com.udb.hotel.dto;

import com.udb.hotel.model.Usuario;

public record UsuarioResponse(Long id, String username, String rol, boolean activo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getRol().name(), u.isActivo());
    }
}
