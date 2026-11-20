package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Datos de una entrada o pase adquirido por un usuario")
public class EntradaAdquiridaDTO {

    @Schema(description = "Identificador único de la entrada adquirida", example = "ent-001")
    private String id;

    @Schema(description = "Pedido al que pertenece la entrada", example = "ped-001")
    private String pedidoId;

    @Schema(description = "Usuario que adquirió la entrada", example = "usr-123")
    private String usuarioId;

    @Schema(description = "Producto asociado a la entrada o pase", example = "prod-001")
    private String productoId;

    @Schema(description = "Evento/festival al que corresponde", example = "evento-001", nullable = true)
    private String eventoId;

    @Schema(description = "Pase concreto adquirido", example = "pase-001", nullable = true)
    private String paseId;

    @Schema(description = "Fecha de adquisición")
    private LocalDateTime fechaCompra;

    @Schema(description = "Estado de la entrada y de su eventual solicitud de cancelación", example = "VALIDA")
    private String estado;

    public EntradaAdquiridaDTO() {
    }

    public EntradaAdquiridaDTO(String id, String pedidoId, String usuarioId, String productoId, String eventoId, String paseId, LocalDateTime fechaCompra, String estado) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.productoId = productoId;
        this.eventoId = eventoId;
        this.paseId = paseId;
        this.fechaCompra = fechaCompra;
        this.estado = estado;
    }

    public static EntradaAdquiridaDTOBuilder builder() {
        return new EntradaAdquiridaDTOBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
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

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public static class EntradaAdquiridaDTOBuilder {
        private String id;
        private String pedidoId;
        private String usuarioId;
        private String productoId;
        private String eventoId;
        private String paseId;
        private LocalDateTime fechaCompra;
        private String estado;

        public EntradaAdquiridaDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public EntradaAdquiridaDTOBuilder pedidoId(String pedidoId) {
            this.pedidoId = pedidoId;
            return this;
        }

        public EntradaAdquiridaDTOBuilder usuarioId(String usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public EntradaAdquiridaDTOBuilder productoId(String productoId) {
            this.productoId = productoId;
            return this;
        }

        public EntradaAdquiridaDTOBuilder eventoId(String eventoId) {
            this.eventoId = eventoId;
            return this;
        }

        public EntradaAdquiridaDTOBuilder paseId(String paseId) {
            this.paseId = paseId;
            return this;
        }

        public EntradaAdquiridaDTOBuilder fechaCompra(LocalDateTime fechaCompra) {
            this.fechaCompra = fechaCompra;
            return this;
        }

        public EntradaAdquiridaDTOBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public EntradaAdquiridaDTO build() {
            return new EntradaAdquiridaDTO(id, pedidoId, usuarioId, productoId, eventoId, paseId, fechaCompra, estado);
        }
    }
}
