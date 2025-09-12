package com.example.app.error;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@ControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<ErrorResponse> build(int status, String code, String msg, Map<String,Object> details) {
        String trace = MDC.get("traceId");
        var body = ErrorResponse.builder()
                .status(status).code(code).message(msg).traceId(trace).details(details).build();
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(AppException ex) {
        return build(ex.httpStatus(), ex.code(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(MethodArgumentNotValidException ex) {
        var fields = ex.getBindingResult().getFieldErrors().stream().map(fe -> Map.of(
                "field", fe.getField(), "message", fe.getDefaultMessage()
        )).toList();
        return build(400, "VALIDATION_ERROR", "validation failed", Map.of("fieldErrors", fields));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex) {
        var list = ex.getConstraintViolations().stream().map(v -> Map.of(
                "property", v.getPropertyPath().toString(), "message", v.getMessage()
        )).toList();
        return build(400, "VALIDATION_ERROR", "validation failed", Map.of("violations", list));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        return build(404, "VIDEO_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex) {
        return build(500, "INTERNAL_ERROR", "unexpected error", Map.of("reason", ex.getClass().getSimpleName()));
    }
}
