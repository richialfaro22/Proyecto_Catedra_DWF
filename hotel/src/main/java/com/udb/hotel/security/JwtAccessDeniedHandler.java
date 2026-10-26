package com.udb.hotel.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
                       AccessDeniedException ex) throws IOException {
        RespuestaErrorSeguridad.escribir(req, res, 403, "Forbidden",
                "No tienes permiso para realizar esta acción.");
    }
}
