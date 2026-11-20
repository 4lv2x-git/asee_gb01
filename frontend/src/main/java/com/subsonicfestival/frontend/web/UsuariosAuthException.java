package com.subsonicfestival.frontend.web;

/**
 * Error devuelto por el microservicio de usuarios o por la comunicacion con el.
 */
public class UsuariosAuthException extends RuntimeException {

    private final int estado;

    public UsuariosAuthException(int estado, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.estado = estado;
    }

    public UsuariosAuthException(int estado, String mensaje) {
        this(estado, mensaje, null);
    }

    public int getEstado() {
        return estado;
    }

    public boolean esSesionInvalida() {
        return estado == 401 || estado == 403;
    }
}
