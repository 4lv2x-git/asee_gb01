package com.subsonicfestival.inventory.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final String codigo;
    private final HttpStatus status;

    public AppException(String codigo, String mensaje, HttpStatus status) {
        super(mensaje);
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() {
        return codigo;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
