package com.subsonicfestival.frontend.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Formulario de registro de clientes y proveedores.
 */
public class RegistroForm {

    @NotBlank(message = "Introduce tu nombre.")
    private String nombre;

    @NotBlank(message = "Introduce tu correo electrónico.")
    @Email(message = "Introduce un correo electrónico válido.")
    private String correo;

    @NotBlank(message = "Introduce una contraseña.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String password;

    @NotBlank(message = "Repite la contraseña.")
    private String passwordConfirm;

    @Pattern(regexp = "cliente|proveedor", message = "Selecciona un tipo de cuenta válido.")
    private String rol = "cliente";

    private String tipoServicio;

    private String descripcionServicio;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
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
