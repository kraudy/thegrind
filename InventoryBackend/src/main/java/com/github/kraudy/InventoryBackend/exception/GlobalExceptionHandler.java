package com.github.kraudy.InventoryBackend.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getStatusCode().value() == 400 ? "Bad Request" : ex.getStatusCode().toString());
        body.put("message", ex.getReason());           // ← YOUR CUSTOM MESSAGE GOES HERE
        body.put("path", ""); // can be improved later

        return new ResponseEntity<>(body, ex.getStatusCode());
    }
}
