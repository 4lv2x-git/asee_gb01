package com.subsonicfestival.inventory.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException {
    public BadRequestException(String mensaje) {
        super("PARAMETROS_INVALIDOS", mensaje, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String codigo, String mensaje) {
        super(codigo, mensaje, HttpStatus.BAD_REQUEST);
    }
}
