package com.subsonicfestival.inventory.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entradas_adquiridas")
public class EntradaAdquirida {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "producto_id", nullable = false, length = 100)
    private String productoId;

    @Column(name = "evento_id", length = 100)
    private String eventoId;

    @Column(name = "pase_id", length = 100)
    private String paseId;

    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    public EntradaAdquirida() {
    }

    public EntradaAdquirida(String id, Pedido pedido, String usuarioId, String productoId, String eventoId, String paseId, LocalDateTime fechaCompra, String estado) {
        this.id = id;
        this.pedido = pedido;
        this.usuarioId = usuarioId;
        this.productoId = productoId;
        this.eventoId = eventoId;
        this.paseId = paseId;
        this.fechaCompra = fechaCompra;
        this.estado = estado;
    }

    public static EntradaAdquiridaBuilder builder() {
        return new EntradaAdquiridaBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
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

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public static class EntradaAdquiridaBuilder {
        private String id;
        private Pedido pedido;
        private String usuarioId;
        private String productoId;
        private String eventoId;
        private String paseId;
        private LocalDateTime fechaCompra;
        private String estado;

        public EntradaAdquiridaBuilder id(String id) {
            this.id = id;
            return this;
        }

        public EntradaAdquiridaBuilder pedido(Pedido pedido) {
            this.pedido = pedido;
            return this;
        }

        public EntradaAdquiridaBuilder usuarioId(String usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public EntradaAdquiridaBuilder productoId(String productoId) {
            this.productoId = productoId;
            return this;
        }

        public EntradaAdquiridaBuilder eventoId(String eventoId) {
            this.eventoId = eventoId;
            return this;
        }

        public EntradaAdquiridaBuilder paseId(String paseId) {
            this.paseId = paseId;
            return this;
        }

        public EntradaAdquiridaBuilder fechaCompra(LocalDateTime fechaCompra) {
            this.fechaCompra = fechaCompra;
            return this;
        }

        public EntradaAdquiridaBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public EntradaAdquirida build() {
            return new EntradaAdquirida(id, pedido, usuarioId, productoId, eventoId, paseId, fechaCompra, estado);
        }
    }
}
