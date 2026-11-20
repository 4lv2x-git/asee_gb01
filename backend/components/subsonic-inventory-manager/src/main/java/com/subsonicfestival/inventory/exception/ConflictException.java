package com.subsonicfestival.inventory.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {
    public ConflictException(String mensaje) {
        super("CONFLICTO", mensaje, HttpStatus.CONFLICT);
    }

    public ConflictException(String codigo, String mensaje) {
        super(codigo, mensaje, HttpStatus.CONFLICT);
    }
}
