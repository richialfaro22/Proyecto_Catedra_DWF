package com.udb.hotel.dto;

/** Datos del usuario autenticado, con su ficha de huésped si la tiene. */
public record PerfilResponse(UsuarioResponse usuario, HuespedResponse huesped) {
}
