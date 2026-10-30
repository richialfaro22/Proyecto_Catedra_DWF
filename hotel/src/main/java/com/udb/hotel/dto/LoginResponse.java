package com.udb.hotel.dto;

public record LoginResponse(String token, String alias, String rol, long expiraEnSegundos) {
}
