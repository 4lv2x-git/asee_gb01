package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estructura de respuesta para errores de la API")
public class ErrorDTO {

    @Schema(description = "Código de error identificador", example = "STOCK_INSUFICIENTE")
    private String codigo;

    @Schema(description = "Mensaje explicativo del error", example = "No hay stock suficiente para completar la operación.")
    private String mensaje;

    public ErrorDTO() {
    }

    public ErrorDTO(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public static ErrorDTOBuilder builder() {
        return new ErrorDTOBuilder();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public static class ErrorDTOBuilder {
        private String codigo;
        private String mensaje;

        public ErrorDTOBuilder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public ErrorDTOBuilder mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public ErrorDTO build() {
            return new ErrorDTO(codigo, mensaje);
        }
    }
}
