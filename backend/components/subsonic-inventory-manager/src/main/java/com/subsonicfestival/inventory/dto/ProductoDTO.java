package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Representación de un producto en catálogo y su stock")
public class ProductoDTO {

    @NotBlank
    @Schema(description = "Identificador único del producto", example = "prod-001")
    private String id;

    @NotBlank
    @Schema(description = "Nombre comercial del producto", example = "Entrada General - Subsonic Day 1")
    private String nombre;

    @Schema(description = "Descripción del producto", example = "Acceso general para el primer día del festival")
    private String descripcion;

    @NotNull
    @Min(0)
    @Schema(description = "Precio unitario del producto", example = "45.50")
    private Double precio;

    @NotBlank
    @Schema(description = "Tipo de producto (entrada, pase, merchandising)", example = "entrada")
    private String tipo;

    @NotNull
    @Min(0)
    @Schema(description = "Unidades disponibles para la venta", example = "100")
    private Integer stockDisponible;

    public ProductoDTO() {
    }

    public ProductoDTO(String id, String nombre, String descripcion, Double precio, String tipo, Integer stockDisponible) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.tipo = tipo;
        this.stockDisponible = stockDisponible;
    }

    public static ProductoDTOBuilder builder() {
        return new ProductoDTOBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public static class ProductoDTOBuilder {
        private String id;
        private String nombre;
        private String descripcion;
        private Double precio;
        private String tipo;
        private Integer stockDisponible;

        public ProductoDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ProductoDTOBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public ProductoDTOBuilder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public ProductoDTOBuilder precio(Double precio) {
            this.precio = precio;
            return this;
        }

        public ProductoDTOBuilder tipo(String tipo) {
            this.tipo = tipo;
            return this;
        }

        public ProductoDTOBuilder stockDisponible(Integer stockDisponible) {
            this.stockDisponible = stockDisponible;
            return this;
        }

        public ProductoDTO build() {
            return new ProductoDTO(id, nombre, descripcion, precio, tipo, stockDisponible);
        }
    }
}
