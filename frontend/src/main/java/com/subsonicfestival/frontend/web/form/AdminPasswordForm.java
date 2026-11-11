package com.subsonicfestival.frontend.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulario con el que un administrador restablece la contrasena de un usuario.
 */
public class AdminPasswordForm {

    @NotBlank(message = "Introduce una contraseña.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String nuevaPassword;

    @NotBlank(message = "Repite la contraseña.")
    private String nuevaPasswordConfirm;

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }

    public String getNuevaPasswordConfirm() {
        return nuevaPasswordConfirm;
    }

    public void setNuevaPasswordConfirm(String nuevaPasswordConfirm) {
        this.nuevaPasswordConfirm = nuevaPasswordConfirm;
    }
}
