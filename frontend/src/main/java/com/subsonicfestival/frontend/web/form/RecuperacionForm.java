package com.subsonicfestival.frontend.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Formulario para solicitar un codigo de recuperacion.
 */
public class RecuperacionForm {

    @NotBlank(message = "Introduce tu correo electrónico.")
    @Email(message = "Introduce un correo electrónico válido.")
    private String correo;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
