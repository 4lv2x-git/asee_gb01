package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos necesarios para registrar un producto")
public class ProductoCreacionDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre comercial del producto", example = "Camiseta Oficial Subsonic Festival")
    private String nombre;

    @Schema(description = "Descripción del producto", example = "Camiseta de algodón 100% con logo del festival")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Schema(description = "Precio unitario del producto", example = "25.00")
    private Double precio;

    @NotBlank(message = "El tipo es obligatorio")
    @Schema(description = "Tipo de producto (entrada, pase, merchandising)", example = "merchandising")
    private String tipo;

    @NotNull(message = "El stock inicial es obligatorio")
    @Min(value = 0, message = "El stock inicial no puede ser negativo")
    @Schema(description = "Stock inicial disponible para la venta", example = "50")
    private Integer stockDisponible;

    public ProductoCreacionDTO() {
    }

    public ProductoCreacionDTO(String nombre, String descripcion, Double precio, String tipo, Integer stockDisponible) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.tipo = tipo;
        this.stockDisponible = stockDisponible;
    }

    public static ProductoCreacionDTOBuilder builder() {
        return new ProductoCreacionDTOBuilder();
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

    public static class ProductoCreacionDTOBuilder {
        private String nombre;
        private String descripcion;
        private Double precio;
        private String tipo;
        private Integer stockDisponible;

        public ProductoCreacionDTOBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public ProductoCreacionDTOBuilder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public ProductoCreacionDTOBuilder precio(Double precio) {
            this.precio = precio;
            return this;
        }

        public ProductoCreacionDTOBuilder tipo(String tipo) {
            this.tipo = tipo;
            return this;
        }

        public ProductoCreacionDTOBuilder stockDisponible(Integer stockDisponible) {
            this.stockDisponible = stockDisponible;
            return this;
        }

        public ProductoCreacionDTO build() {
            return new ProductoCreacionDTO(nombre, descripcion, precio, tipo, stockDisponible);
        }
    }
}
