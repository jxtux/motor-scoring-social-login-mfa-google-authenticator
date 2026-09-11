package com.finanscore.motorscoring.presentation.exception;

import com.finanscore.motorscoring.application.exception.*;
import com.finanscore.motorscoring.domain.exception.DomainException;
import com.finanscore.motorscoring.presentation.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Handler general de la API.
 *
 * FIXED12:
 * - queda con LOWEST_PRECEDENCE para no interceptar excepciones IAM específicas;
 * - registra la excepción completa antes de devolver INTERNAL_ERROR.
 *   Antes FIXED11 devolvía 500 pero descartaba el stacktrace, por eso
 *   `docker compose logs -f motor-scoring-api` no mostraba la causa real.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> bean(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> v = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> v.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return build(400, "INVALID_REQUEST", "La solicitud contiene campos inválidos.", req, v);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> format(Exception ex, HttpServletRequest req) {
        return build(400, "INVALID_FORMAT", "Formato de solicitud inválido.", req, Map.of());
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, ConstraintViolationException.class })
    ResponseEntity<ErrorResponse> path(Exception ex, HttpServletRequest req) {
        return build(400, "INVALID_PATH_PARAMETER", "El identificador debe ser un número positivo.", req, Map.of());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorResponse> nf(Exception ex, HttpServletRequest req) {
        return build(404, "RESOURCE_NOT_FOUND", ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(SolicitudDuplicadaException.class)
    ResponseEntity<ErrorResponse> dup(Exception ex, HttpServletRequest req) {
        return build(409, "DUPLICATE_APPLICATION", ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErrorResponse> dom(Exception ex, HttpServletRequest req) {
        return build(422, "BUSINESS_VALIDATION_ERROR", ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> gen(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return build(500, "INTERNAL_ERROR", "Ocurrió un error interno.", req, Map.of());
    }

    private ResponseEntity<ErrorResponse> build(int status, String code, String msg, HttpServletRequest req,
            Map<String, String> v) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(OffsetDateTime.now(), status, code, msg, req.getRequestURI(), v));
    }
}
