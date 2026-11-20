package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de una línea dentro de un pedido")
public class LineaPedidoDTO {

    @Schema(description = "Identificador del producto comprado", example = "prod-001")
    private String productoId;

    @Schema(description = "Nombre comercial del producto", example = "Entrada General - Subsonic Day 1")
    private String nombreProducto;

    @Schema(description = "Cantidad comprada", example = "2")
    private Integer cantidad;

    @Schema(description = "Precio unitario aplicado", example = "45.50")
    private Double precioUnitario;

    @Schema(description = "Subtotal de la línea de pedido", example = "91.00")
    private Double subtotal;

    @Schema(description = "Identificador del evento si aplica", example = "evento-001", nullable = true)
    private String eventoId;

    @Schema(description = "Identificador del pase si aplica", example = "pase-001", nullable = true)
    private String paseId;

    public LineaPedidoDTO() {
    }

    public LineaPedidoDTO(String productoId, String nombreProducto, Integer cantidad, Double precioUnitario, Double subtotal, String eventoId, String paseId) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.eventoId = eventoId;
        this.paseId = paseId;
    }

    public static LineaPedidoDTOBuilder builder() {
        return new LineaPedidoDTOBuilder();
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
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

    public static class LineaPedidoDTOBuilder {
        private String productoId;
        private String nombreProducto;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
        private String eventoId;
        private String paseId;

        public LineaPedidoDTOBuilder productoId(String productoId) {
            this.productoId = productoId;
            return this;
        }

        public LineaPedidoDTOBuilder nombreProducto(String nombreProducto) {
            this.nombreProducto = nombreProducto;
            return this;
        }

        public LineaPedidoDTOBuilder cantidad(Integer cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public LineaPedidoDTOBuilder precioUnitario(Double precioUnitario) {
            this.precioUnitario = precioUnitario;
            return this;
        }

        public LineaPedidoDTOBuilder subtotal(Double subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public LineaPedidoDTOBuilder eventoId(String eventoId) {
            this.eventoId = eventoId;
            return this;
        }

        public LineaPedidoDTOBuilder paseId(String paseId) {
            this.paseId = paseId;
            return this;
        }

        public LineaPedidoDTO build() {
            return new LineaPedidoDTO(productoId, nombreProducto, cantidad, precioUnitario, subtotal, eventoId, paseId);
        }
    }
}
