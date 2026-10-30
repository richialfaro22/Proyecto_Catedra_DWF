package com.udb.hotel.exception;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Error de validación de negocio asociado a uno o varios campos
 * (ej. fecha de salida anterior a la de entrada). Responde 400 con el mapa de campos.
 */
@Getter
public class ValidacionException extends RuntimeException {

    private final Map<String, String> campos;

    public ValidacionException(String campo, String mensaje) {
        super(mensaje);
        this.campos = new LinkedHashMap<>();
        this.campos.put(campo, mensaje);
    }
}
