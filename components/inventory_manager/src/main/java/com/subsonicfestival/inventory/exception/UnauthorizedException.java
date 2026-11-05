package com.subsonicfestival.inventory.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String mensaje) {
        super("NO_AUTORIZADO", mensaje, HttpStatus.UNAUTHORIZED);
    }
}
