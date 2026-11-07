package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para actualizar el stock disponible de un producto")
public class StockActualizacionDTO {

    @NotNull
    @Min(0)
    @Schema(description = "Nueva cantidad total de stock disponible", example = "150")
    private Integer stockDisponible;

    public StockActualizacionDTO() {
    }

    public StockActualizacionDTO(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public static StockActualizacionDTOBuilder builder() {
        return new StockActualizacionDTOBuilder();
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public static class StockActualizacionDTOBuilder {
        private Integer stockDisponible;

        public StockActualizacionDTOBuilder stockDisponible(Integer stockDisponible) {
            this.stockDisponible = stockDisponible;
            return this;
        }

        public StockActualizacionDTO build() {
            return new StockActualizacionDTO(stockDisponible);
        }
    }
}
