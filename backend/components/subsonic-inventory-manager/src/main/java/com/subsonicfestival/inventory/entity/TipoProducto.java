package com.subsonicfestival.inventory.entity;

public enum TipoProducto {
    ENTRADA("entrada"),
    PASE("pase"),
    MERCHANDISING("merchandising");

    private final String valor;

    TipoProducto(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoProducto fromString(String texto) {
        if (texto == null) return null;
        for (TipoProducto tipo : TipoProducto.values()) {
            if (tipo.valor.equalsIgnoreCase(texto.trim()) || tipo.name().equalsIgnoreCase(texto.trim())) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de producto no válido: " + texto);
    }
}
