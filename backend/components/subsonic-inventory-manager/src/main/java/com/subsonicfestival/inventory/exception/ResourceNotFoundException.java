package com.subsonicfestival.inventory.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String mensaje) {
        super("RECURSO_NO_ENCONTRADO", mensaje, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String codigo, String mensaje) {
        super(codigo, mensaje, HttpStatus.NOT_FOUND);
    }
}
