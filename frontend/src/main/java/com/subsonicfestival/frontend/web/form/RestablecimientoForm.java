package com.subsonicfestival.frontend.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulario para establecer una nueva contrasena con un codigo de recuperacion.
 */
public class RestablecimientoForm {

    @NotBlank(message = "Introduce el código de recuperación.")
    @Size(min = 16, message = "El código no es válido.")
    private String codigo;

    @NotBlank(message = "Introduce una contraseña.")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres.")
    private String nuevaPassword;

    @NotBlank(message = "Repite la nueva contraseña.")
    private String nuevaPasswordConfirm;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

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
