package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Solicitud para crear un nuevo pedido")
public class PedidoCreacionDTO {

    @NotEmpty(message = "El pedido debe contener al menos una línea")
    @Valid
    @Schema(description = "Productos y cantidades seleccionados por el cliente")
    private List<LineaPedidoCreacionDTO> lineas;

    public PedidoCreacionDTO() {
    }

    public PedidoCreacionDTO(List<LineaPedidoCreacionDTO> lineas) {
        this.lineas = lineas;
    }

    public static PedidoCreacionDTOBuilder builder() {
        return new PedidoCreacionDTOBuilder();
    }

    public List<LineaPedidoCreacionDTO> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaPedidoCreacionDTO> lineas) {
        this.lineas = lineas;
    }

    public static class PedidoCreacionDTOBuilder {
        private List<LineaPedidoCreacionDTO> lineas;

        public PedidoCreacionDTOBuilder lineas(List<LineaPedidoCreacionDTO> lineas) {
            this.lineas = lineas;
            return this;
        }

        public PedidoCreacionDTO build() {
            return new PedidoCreacionDTO(lineas);
        }
    }
}
