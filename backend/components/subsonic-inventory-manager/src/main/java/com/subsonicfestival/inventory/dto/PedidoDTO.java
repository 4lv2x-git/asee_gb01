package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detalle completo de un pedido y sus entradas asociadas")
public class PedidoDTO {

    @Schema(description = "Identificador único del pedido", example = "ped-001")
    private String id;

    @Schema(description = "ID del usuario comprador", example = "usr-123")
    private String usuarioId;

    @Schema(description = "Líneas de productos comprados")
    private List<LineaPedidoDTO> lineas;

    @Schema(description = "Fecha y hora en la que se registró la compra")
    private LocalDateTime fechaCompra;

    @Schema(description = "Importe total calculado por el backend", example = "91.00")
    private Double importeTotal;

    @Schema(description = "Estado actual del pedido", example = "COMPLETADO")
    private String estado;

    @Schema(description = "Entradas o pases asociados al pedido")
    private List<EntradaAdquiridaDTO> entradas;

    public PedidoDTO() {
    }

    public PedidoDTO(String id, String usuarioId, List<LineaPedidoDTO> lineas, LocalDateTime fechaCompra, Double importeTotal, String estado, List<EntradaAdquiridaDTO> entradas) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.lineas = lineas;
        this.fechaCompra = fechaCompra;
        this.importeTotal = importeTotal;
        this.estado = estado;
        this.entradas = entradas;
    }

    public static PedidoDTOBuilder builder() {
        return new PedidoDTOBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<LineaPedidoDTO> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaPedidoDTO> lineas) {
        this.lineas = lineas;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public Double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(Double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<EntradaAdquiridaDTO> getEntradas() {
        return entradas;
    }

    public void setEntradas(List<EntradaAdquiridaDTO> entradas) {
        this.entradas = entradas;
    }

    public static class PedidoDTOBuilder {
        private String id;
        private String usuarioId;
        private List<LineaPedidoDTO> lineas;
        private LocalDateTime fechaCompra;
        private Double importeTotal;
        private String estado;
        private List<EntradaAdquiridaDTO> entradas;

        public PedidoDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PedidoDTOBuilder usuarioId(String usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public PedidoDTOBuilder lineas(List<LineaPedidoDTO> lineas) {
            this.lineas = lineas;
            return this;
        }

        public PedidoDTOBuilder fechaCompra(LocalDateTime fechaCompra) {
            this.fechaCompra = fechaCompra;
            return this;
        }

        public PedidoDTOBuilder importeTotal(Double importeTotal) {
            this.importeTotal = importeTotal;
            return this;
        }

        public PedidoDTOBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public PedidoDTOBuilder entradas(List<EntradaAdquiridaDTO> entradas) {
            this.entradas = entradas;
            return this;
        }

        public PedidoDTO build() {
            return new PedidoDTO(id, usuarioId, lineas, fechaCompra, importeTotal, estado, entradas);
        }
    }
}
