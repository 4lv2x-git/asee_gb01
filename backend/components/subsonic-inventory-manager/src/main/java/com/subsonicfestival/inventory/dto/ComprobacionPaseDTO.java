package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de comprobación de evento y pase")
public class ComprobacionPaseDTO {

    @Schema(description = "Identificador del evento", example = "evento-001")
    private String eventoId;

    @Schema(description = "Identificador del pase", example = "pase-001")
    private String paseId;

    @Schema(description = "Indica si el evento y el pase solicitado existen y están relacionados", example = "true")
    private Boolean existe;

    @Schema(description = "Nombre del pase, si existe", example = "Pase VIP 3 Días", nullable = true)
    private String nombrePase;

    @Schema(description = "Precio del pase proporcionado por el Gestor de Eventos", example = "120.00", nullable = true)
    private Double precio;

    public ComprobacionPaseDTO() {
    }

    public ComprobacionPaseDTO(String eventoId, String paseId, Boolean existe, String nombrePase, Double precio) {
        this.eventoId = eventoId;
        this.paseId = paseId;
        this.existe = existe;
        this.nombrePase = nombrePase;
        this.precio = precio;
    }

    public static ComprobacionPaseDTOBuilder builder() {
        return new ComprobacionPaseDTOBuilder();
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public String getPaseId() {
        return paseId;
    }

    public void setPaseId(String paseId) {
        this.paseId = paseId;
    }

    public Boolean getExiste() {
        return existe;
    }

    public void setExiste(Boolean existe) {
        this.existe = existe;
    }

    public String getNombrePase() {
        return nombrePase;
    }

    public void setNombrePase(String nombrePase) {
        this.nombrePase = nombrePase;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public static class ComprobacionPaseDTOBuilder {
        private String eventoId;
        private String paseId;
        private Boolean existe;
        private String nombrePase;
        private Double precio;

        public ComprobacionPaseDTOBuilder eventoId(String eventoId) {
            this.eventoId = eventoId;
            return this;
        }

        public ComprobacionPaseDTOBuilder paseId(String paseId) {
            this.paseId = paseId;
            return this;
        }

        public ComprobacionPaseDTOBuilder existe(Boolean existe) {
            this.existe = existe;
            return this;
        }

        public ComprobacionPaseDTOBuilder nombrePase(String nombrePase) {
            this.nombrePase = nombrePase;
            return this;
        }

        public ComprobacionPaseDTOBuilder precio(Double precio) {
            this.precio = precio;
            return this;
        }

        public ComprobacionPaseDTO build() {
            return new ComprobacionPaseDTO(eventoId, paseId, existe, nombrePase, precio);
        }
    }
}
