package com.subsonicfestival.frontend.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Usuario devuelto por el microservicio {@code subsonic-users-auth}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Usuario(
        String id,
        String nombre,
        String correo,
        String rol,
        String estado,
        boolean emailVerificado,
        String direccion,
        String tipoServicio,
        String descripcionServicio) {

    public boolean esAdmin() {
        return "admin".equals(rol);
    }

    public boolean esProveedor() {
        return "proveedor".equals(rol);
    }

    public boolean estaActivo() {
        return "activo".equals(estado);
    }
}
