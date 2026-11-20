package com.subsonicfestival.frontend.web.form;

import jakarta.validation.constraints.NotBlank;

/**
 * Formulario para verificar el correo con el codigo recibido.
 */
public class VerificacionForm {

    @NotBlank(message = "Introduce el código recibido.")
    private String codigo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
