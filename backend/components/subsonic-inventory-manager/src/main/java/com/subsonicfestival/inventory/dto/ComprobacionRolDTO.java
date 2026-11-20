package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de comprobación de rol de usuario")
public class ComprobacionRolDTO {

    @Schema(description = "Identificador del usuario", example = "usr-123")
    private String usuarioId;

    @Schema(description = "Indica si el usuario tiene rol cliente", example = "true")
    private Boolean esCliente;

    public ComprobacionRolDTO() {
    }

    public ComprobacionRolDTO(String usuarioId, Boolean esCliente) {
        this.usuarioId = usuarioId;
        this.esCliente = esCliente;
    }

    public static ComprobacionRolDTOBuilder builder() {
        return new ComprobacionRolDTOBuilder();
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Boolean getEsCliente() {
        return esCliente;
    }

    public void setEsCliente(Boolean esCliente) {
        this.esCliente = esCliente;
    }

    public static class ComprobacionRolDTOBuilder {
        private String usuarioId;
        private Boolean esCliente;

        public ComprobacionRolDTOBuilder usuarioId(String usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public ComprobacionRolDTOBuilder esCliente(Boolean esCliente) {
            this.esCliente = esCliente;
            return this;
        }

        public ComprobacionRolDTO build() {
            return new ComprobacionRolDTO(usuarioId, esCliente);
        }
    }
}
