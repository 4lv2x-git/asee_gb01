package com.subsonicfestival.inventory.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {
    public ForbiddenException(String mensaje) {
        super("ACCESO_DENEGADO", mensaje, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String codigo, String mensaje) {
        super(codigo, mensaje, HttpStatus.FORBIDDEN);
    }
}
