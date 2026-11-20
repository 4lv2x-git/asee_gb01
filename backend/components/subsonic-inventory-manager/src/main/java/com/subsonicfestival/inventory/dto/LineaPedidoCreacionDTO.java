package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear una línea de pedido")
public class LineaPedidoCreacionDTO {

    @NotBlank
    @Schema(description = "Identificador del producto comprado", example = "prod-001")
    private String productoId;

    @NotNull
    @Min(1)
    @Schema(description = "Número de unidades solicitadas", example = "2")
    private Integer cantidad;

    @Schema(description = "Identificador del evento al que pertenece la entrada o pase", example = "evento-001", nullable = true)
    private String eventoId;

    @Schema(description = "Identificador del pase comprado", example = "pase-001", nullable = true)
    private String paseId;

    public LineaPedidoCreacionDTO() {
    }

    public LineaPedidoCreacionDTO(String productoId, Integer cantidad, String eventoId, String paseId) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.eventoId = eventoId;
        this.paseId = paseId;
    }

    public static LineaPedidoCreacionDTOBuilder builder() {
        return new LineaPedidoCreacionDTOBuilder();
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
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

    public static class LineaPedidoCreacionDTOBuilder {
        private String productoId;
        private Integer cantidad;
        private String eventoId;
        private String paseId;

        public LineaPedidoCreacionDTOBuilder productoId(String productoId) {
            this.productoId = productoId;
            return this;
        }

        public LineaPedidoCreacionDTOBuilder cantidad(Integer cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public LineaPedidoCreacionDTOBuilder eventoId(String eventoId) {
            this.eventoId = eventoId;
            return this;
        }

        public LineaPedidoCreacionDTOBuilder paseId(String paseId) {
            this.paseId = paseId;
            return this;
        }

        public LineaPedidoCreacionDTO build() {
            return new LineaPedidoCreacionDTO(productoId, cantidad, eventoId, paseId);
        }
    }
}
