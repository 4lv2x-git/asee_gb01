package com.subsonicfestival.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "precio", nullable = false)
    private Double precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoProducto tipo;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible;

    public Producto() {
    }

    public Producto(String id, String nombre, String descripcion, Double precio, TipoProducto tipo, Integer stockDisponible) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.tipo = tipo;
        this.stockDisponible = stockDisponible;
    }

    public static ProductoBuilder builder() {
        return new ProductoBuilder();
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

    public TipoProducto getTipo() {
        return tipo;
    }

    public void setTipo(TipoProducto tipo) {
        this.tipo = tipo;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public static class ProductoBuilder {
        private String id;
        private String nombre;
        private String descripcion;
        private Double precio;
        private TipoProducto tipo;
        private Integer stockDisponible;

        public ProductoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ProductoBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public ProductoBuilder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public ProductoBuilder precio(Double precio) {
            this.precio = precio;
            return this;
        }

        public ProductoBuilder tipo(TipoProducto tipo) {
            this.tipo = tipo;
            return this;
        }

        public ProductoBuilder stockDisponible(Integer stockDisponible) {
            this.stockDisponible = stockDisponible;
            return this;
        }

        public Producto build() {
            return new Producto(id, nombre, descripcion, precio, tipo, stockDisponible);
        }
    }
}
