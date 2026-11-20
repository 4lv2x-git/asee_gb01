package com.subsonicfestival.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos modificables de un producto")
public class ProductoActualizacionDTO {

    @Size(min = 1, message = "El nombre no puede estar vacío si se proporciona")
    @Schema(description = "Nuevo nombre comercial del producto", example = "Entrada VIP - Subsonic Day 1")
    private String nombre;

    @Schema(description = "Nueva descripción del producto", example = "Acceso VIP con consumición incluida")
    private String descripcion;

    @Min(value = 0, message = "El precio no puede ser negativo")
    @Schema(description = "Nuevo precio unitario del producto", example = "75.00")
    private Double precio;

    @Schema(description = "Nuevo tipo de producto (entrada, pase, merchandising)", example = "entrada")
    private String tipo;

    public ProductoActualizacionDTO() {
    }

    public ProductoActualizacionDTO(String nombre, String descripcion, Double precio, String tipo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.tipo = tipo;
    }

    public static ProductoActualizacionDTOBuilder builder() {
        return new ProductoActualizacionDTOBuilder();
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

    public static class ProductoActualizacionDTOBuilder {
        private String nombre;
        private String descripcion;
        private Double precio;
        private String tipo;

        public ProductoActualizacionDTOBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public ProductoActualizacionDTOBuilder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public ProductoActualizacionDTOBuilder precio(Double precio) {
            this.precio = precio;
            return this;
        }

        public ProductoActualizacionDTOBuilder tipo(String tipo) {
            this.tipo = tipo;
            return this;
        }

        public ProductoActualizacionDTO build() {
            return new ProductoActualizacionDTO(nombre, descripcion, precio, tipo);
        }
    }
}
