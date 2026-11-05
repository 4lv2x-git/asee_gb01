package com.subsonicfestival.inventory.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Column(name = "importe_total", nullable = false)
    private Double importeTotal;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LineaPedido> lineas = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntradaAdquirida> entradas = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(String id, String usuarioId, LocalDateTime fechaCompra, Double importeTotal, String estado, List<LineaPedido> lineas, List<EntradaAdquirida> entradas) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fechaCompra = fechaCompra;
        this.importeTotal = importeTotal;
        this.estado = estado;
        if (lineas != null) {
            this.lineas = lineas;
        }
        if (entradas != null) {
            this.entradas = entradas;
        }
    }

    public static PedidoBuilder builder() {
        return new PedidoBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public Double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(Double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<LineaPedido> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaPedido> lineas) {
        this.lineas = lineas != null ? lineas : new ArrayList<>();
    }

    public List<EntradaAdquirida> getEntradas() {
        return entradas;
    }

    public void setEntradas(List<EntradaAdquirida> entradas) {
        this.entradas = entradas != null ? entradas : new ArrayList<>();
    }

    public void addLinea(LineaPedido linea) {
        lineas.add(linea);
        linea.setPedido(this);
    }

    public void addEntrada(EntradaAdquirida entrada) {
        entradas.add(entrada);
        entrada.setPedido(this);
    }

    public static class PedidoBuilder {
        private String id;
        private String usuarioId;
        private LocalDateTime fechaCompra;
        private Double importeTotal;
        private String estado;
        private List<LineaPedido> lineas = new ArrayList<>();
        private List<EntradaAdquirida> entradas = new ArrayList<>();

        public PedidoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PedidoBuilder usuarioId(String usuarioId) {
            this.usuarioId = usuarioId;
            return this;
        }

        public PedidoBuilder fechaCompra(LocalDateTime fechaCompra) {
            this.fechaCompra = fechaCompra;
            return this;
        }

        public PedidoBuilder importeTotal(Double importeTotal) {
            this.importeTotal = importeTotal;
            return this;
        }

        public PedidoBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public PedidoBuilder lineas(List<LineaPedido> lineas) {
            this.lineas = lineas != null ? lineas : new ArrayList<>();
            return this;
        }

        public PedidoBuilder entradas(List<EntradaAdquirida> entradas) {
            this.entradas = entradas != null ? entradas : new ArrayList<>();
            return this;
        }

        public Pedido build() {
            return new Pedido(id, usuarioId, fechaCompra, importeTotal, estado, lineas, entradas);
        }
    }
}
