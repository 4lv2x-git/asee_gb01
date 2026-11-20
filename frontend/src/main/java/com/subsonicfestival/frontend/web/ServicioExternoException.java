package com.subsonicfestival.frontend.web;

/**
 * Error de comunicacion con un microservicio externo.
 *
 * <p>Evita que una caida de festivales o inventario rompa el panel: los
 * controladores capturan la excepcion y muestran un aviso en la propia pagina.</p>
 */
public class ServicioExternoException extends RuntimeException {

    public ServicioExternoException(String mensaje) {
        super(mensaje);
    }

    public ServicioExternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
