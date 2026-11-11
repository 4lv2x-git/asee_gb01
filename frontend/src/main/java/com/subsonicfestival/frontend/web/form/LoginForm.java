package com.subsonicfestival.frontend.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Formulario de inicio de sesion.
 */
public class LoginForm {

    @NotBlank(message = "Introduce tu correo electrónico.")
    @Email(message = "Introduce un correo electrónico válido.")
    private String correo;

    @NotBlank(message = "Introduce tu contraseña.")
    private String password;

    private String next;

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

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
