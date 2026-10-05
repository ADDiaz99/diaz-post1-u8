package com.example.auditoria.adapter.in.web;

import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import com.example.auditoria.usecase.HallazgoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduce las excepciones del dominio y de los casos de uso a HTTP. Vive en el
 * círculo Interface Adapters: el dominio lanza excepciones sin saber que existe HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HallazgoNotFoundException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(HallazgoNotFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, ex, List.of());
    }

    /** Transición no permitida por la máquina de estados, o cierre sin plan de remediación. */
    @ExceptionHandler({TransicionInvalidaException.class, IllegalStateException.class})
    public ResponseEntity<ErrorRespuesta> reglaDeNegocio(RuntimeException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex, List.of());
    }

    /** Invariantes del constructor o de los Value Objects, y UUID mal formado en la URL. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> datoInvalido(IllegalArgumentException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return ResponseEntity.badRequest().body(new ErrorRespuesta(400, "Validacion",
                "Datos de entrada invalidos", detalles));
    }

    /** JSON mal formado, fecha con formato inválido o severidad que no existe. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta(400, "JsonInvalido",
                "El cuerpo de la peticion no es valido (revise fechas yyyy-MM-dd y severidad: CRITICA, ALTA, MEDIA o BAJA)",
                List.of()));
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus status, RuntimeException ex, List<String> detalles) {
        return ResponseEntity.status(status)
                .body(new ErrorRespuesta(status.value(), ex.getClass().getSimpleName(), ex.getMessage(), detalles));
    }
}
