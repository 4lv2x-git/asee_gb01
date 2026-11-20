package com.subsonicfestival.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lineas_pedido")
public class LineaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "producto_id", nullable = false, length = 100)
    private String productoId;

    @Column(name = "nombre_producto", length = 200)
    private String nombreProducto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    @Column(name = "evento_id", length = 100)
    private String eventoId;

    @Column(name = "pase_id", length = 100)
    private String paseId;

    public LineaPedido() {
    }

    public LineaPedido(Long id, Pedido pedido, String productoId, String nombreProducto, Integer cantidad, Double precioUnitario, Double subtotal, String eventoId, String paseId) {
        this.id = id;
        this.pedido = pedido;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.eventoId = eventoId;
        this.paseId = paseId;
    }

    public static LineaPedidoBuilder builder() {
        return new LineaPedidoBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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

    public static class LineaPedidoBuilder {
        private Long id;
        private Pedido pedido;
        private String productoId;
        private String nombreProducto;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
        private String eventoId;
        private String paseId;

        public LineaPedidoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LineaPedidoBuilder pedido(Pedido pedido) {
            this.pedido = pedido;
            return this;
        }

        public LineaPedidoBuilder productoId(String productoId) {
            this.productoId = productoId;
            return this;
        }

        public LineaPedidoBuilder nombreProducto(String nombreProducto) {
            this.nombreProducto = nombreProducto;
            return this;
        }

        public LineaPedidoBuilder cantidad(Integer cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public LineaPedidoBuilder precioUnitario(Double precioUnitario) {
            this.precioUnitario = precioUnitario;
            return this;
        }

        public LineaPedidoBuilder subtotal(Double subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public LineaPedidoBuilder eventoId(String eventoId) {
            this.eventoId = eventoId;
            return this;
        }

        public LineaPedidoBuilder paseId(String paseId) {
            this.paseId = paseId;
            return this;
        }

        public LineaPedido build() {
            return new LineaPedido(id, pedido, productoId, nombreProducto, cantidad, precioUnitario, subtotal, eventoId, paseId);
        }
    }
}
