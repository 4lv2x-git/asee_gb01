package com.subsonicfestival.inventory.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends AppException {
    public InsufficientStockException(String mensaje) {
        super("STOCK_INSUFICIENTE", mensaje, HttpStatus.CONFLICT);
    }
}
