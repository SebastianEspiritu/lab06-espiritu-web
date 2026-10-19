package com.tecsup.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice   // 🔥 IMPORTANTE (mejor que @ControllerAdvice)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarErrores(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errores);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> unauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> forbidden(ForbiddenException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> notFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
