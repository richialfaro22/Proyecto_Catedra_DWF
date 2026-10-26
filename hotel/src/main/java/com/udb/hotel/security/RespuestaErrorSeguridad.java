package com.udb.hotel.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

final class RespuestaErrorSeguridad {

    private RespuestaErrorSeguridad() {
    }

    static void escribir(HttpServletRequest req, HttpServletResponse res,
                         int estado, String error, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String json = "{\"fecha\":\"" + LocalDateTime.now() + "\","
                + "\"estado\":" + estado + ","
                + "\"error\":\"" + error + "\","
                + "\"mensaje\":\"" + mensaje.replace("\\", "\\\\").replace("\"", "\\\"") + "\","
                + "\"ruta\":\"" + req.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
        res.getWriter().write(json);
    }
}
