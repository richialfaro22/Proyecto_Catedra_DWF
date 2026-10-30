package com.udb.hotel.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex,
                                                         HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return construir(HttpStatus.BAD_REQUEST,
                "Revisa los datos enviados, hay campos con errores.", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoInvalido(HttpMessageNotReadableException ex,
                                                        HttpServletRequest req) {
        String detalle = String.valueOf(ex.getMostSpecificCause().getMessage());
        String mensaje = "No pudimos leer los datos enviados. Verifica que el formato y los tipos de dato sean correctos.";
        if (detalle.contains("LocalDate")) {
            mensaje = "Una de las fechas no tiene un formato válido. Usa el formato AAAA-MM-DD.";
        } else if (detalle.contains("not one of the values accepted") || detalle.contains("Enum")) {
            mensaje = "Uno de los valores seleccionados no es válido. Elige una opción de la lista.";
        } else if (detalle.contains("Integer") || detalle.contains("Long")
                || detalle.contains("BigDecimal") || detalle.contains("numeric")) {
            mensaje = "Uno de los campos numéricos contiene texto o un valor no válido. Ingresa solo números.";
        } else if (detalle.contains("Required request body is missing")) {
            mensaje = "Debes enviar los datos solicitados en el cuerpo de la petición.";
        }
        return construir(HttpStatus.BAD_REQUEST, mensaje, req, null);
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorResponse> validacionNegocio(ValidacionException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), req, ex.getCampos());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoParametro(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST,
                "El valor '" + ex.getValue() + "' no es válido para el parámetro '" + ex.getName() + "'.",
                req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> parametroFaltante(MissingServletRequestParameterException ex,
                                                           HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'.", req, null);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> parametrosInvalidos(HandlerMethodValidationException ex,
                                                             HttpServletRequest req) {
        String detalle = ex.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .filter(m -> m != null && !m.isBlank())
                .distinct()
                .reduce((a, b) -> a + " " + b)
                .orElse("Revisa los datos enviados.");
        return construir(HttpStatus.BAD_REQUEST, detalle, req, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> credencialesIncorrectas(HttpServletRequest req) {
        return construir(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.", req, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> usuarioDesactivado(HttpServletRequest req) {
        return construir(HttpStatus.FORBIDDEN,
                "Tu usuario está desactivado. Comunícate con el administrador.", req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(HttpServletRequest req) {
        return construir(HttpStatus.UNAUTHORIZED,
                "No pudimos validar tu identidad. Inicia sesión de nuevo.", req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(HttpServletRequest req) {
        return construir(HttpStatus.FORBIDDEN,
                "No tienes permiso para realizar esta acción.", req, null);
    }

    @ExceptionHandler({RecursoNoEncontradoException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> noEncontrado(Exception ex, HttpServletRequest req) {
        String mensaje = (ex instanceof RecursoNoEncontradoException)
                ? ex.getMessage()
                : "No se encontró el recurso solicitado.";
        return construir(HttpStatus.NOT_FOUND, mensaje, req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpServletRequest req) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED,
                "Este recurso no admite ese tipo de petición.", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> conflicto(HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT,
                "No se pudo guardar: ya existe un registro con esos datos o está siendo usado en otro lugar.",
                req, null);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> duplicado(RecursoDuplicadoException ex,
                                                   HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorResponse> operacionNoPermitida(OperacionNoPermitidaException ex,
                                                              HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta de nuevo más tarde.", req, null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje,
                                                    HttpServletRequest req,
                                                    Map<String, String> campos) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), estado.value(),
                estado.getReasonPhrase(), mensaje, req.getRequestURI(), campos);
        return ResponseEntity.status(estado).body(body);
    }
}
