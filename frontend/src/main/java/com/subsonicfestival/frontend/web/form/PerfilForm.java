package com.subsonicfestival.frontend.web.form;

import com.subsonicfestival.frontend.web.dto.Usuario;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos editables del perfil propio.
 */
public class PerfilForm {

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    private String direccion;

    private String tipoServicio;

    private String descripcionServicio;

    public static PerfilForm desde(Usuario usuario) {
        PerfilForm formulario = new PerfilForm();
        if (usuario != null) {
            formulario.setNombre(usuario.nombre());
            formulario.setDireccion(usuario.direccion());
            formulario.setTipoServicio(usuario.tipoServicio());
            formulario.setDescripcionServicio(usuario.descripcionServicio());
        }
        return formulario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getDescripcionServicio() {
        return descripcionServicio;
    }

    public void setDescripcionServicio(String descripcionServicio) {
        this.descripcionServicio = descripcionServicio;
    }
}
