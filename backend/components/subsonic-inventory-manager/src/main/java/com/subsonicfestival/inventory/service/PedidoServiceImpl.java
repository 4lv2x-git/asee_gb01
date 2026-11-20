package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.*;
import com.subsonicfestival.inventory.entity.*;
import com.subsonicfestival.inventory.exception.ConflictException;
import com.subsonicfestival.inventory.exception.ForbiddenException;
import com.subsonicfestival.inventory.exception.InsufficientStockException;
import com.subsonicfestival.inventory.exception.ResourceNotFoundException;
import com.subsonicfestival.inventory.repository.PedidoRepository;
import com.subsonicfestival.inventory.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final InternoService internoService;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, ProductoRepository productoRepository, InternoService internoService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.internoService = internoService;
    }

    @Override
    @Transactional
    public PedidoDTO crearPedido(String usuarioId, PedidoCreacionDTO pedidoCreacionDTO) {
        // 1. Validar rol de cliente
        ComprobacionRolDTO rol = internoService.verificarCliente(usuarioId);
        if (rol == null || !Boolean.TRUE.equals(rol.getEsCliente())) {
            throw new ForbiddenException("NO_ES_CLIENTE", "El usuario autenticado no tiene rol cliente");
        }

        // 2. Validar líneas y productos
        double importeTotal = 0.0;
        List<Producto> productosParaActualizar = new ArrayList<>();
        List<LineaPedido> lineasEntidad = new ArrayList<>();
        List<EntradaAdquirida> entradasEntidad = new ArrayList<>();

        String pedidoId = "ped-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime ahora = LocalDateTime.now();

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .usuarioId(usuarioId)
                .fechaCompra(ahora)
                .estado("COMPLETADO")
                .lineas(new ArrayList<>())
                .entradas(new ArrayList<>())
                .build();

        for (LineaPedidoCreacionDTO lineaDTO : pedidoCreacionDTO.getLineas()) {
            Producto producto = productoRepository.findById(lineaDTO.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("PRODUCTO_NO_ENCONTRADO",
                            "Producto no encontrado: " + lineaDTO.getProductoId()));

            // Si es un pase o entrada asociado a evento, validar con Gestor de Eventos
            if (lineaDTO.getPaseId() != null || lineaDTO.getEventoId() != null || producto.getTipo() == TipoProducto.PASE) {
                String eventoId = lineaDTO.getEventoId() != null ? lineaDTO.getEventoId() : "evento-general";
                String paseId = lineaDTO.getPaseId() != null ? lineaDTO.getPaseId() : producto.getId();
                internoService.verificarEventoPase(eventoId, paseId);
            }

            // Validar stock suficiente
            if (producto.getStockDisponible() < lineaDTO.getCantidad()) {
                throw new InsufficientStockException("No hay stock suficiente para el producto: " + producto.getNombre());
            }

            // Reducir stock
            producto.setStockDisponible(producto.getStockDisponible() - lineaDTO.getCantidad());
            productosParaActualizar.add(producto);

            // Calcular subtotales
            double subtotal = producto.getPrecio() * lineaDTO.getCantidad();
            importeTotal += subtotal;

            LineaPedido linea = LineaPedido.builder()
                    .pedido(pedido)
                    .productoId(producto.getId())
                    .nombreProducto(producto.getNombre())
                    .cantidad(lineaDTO.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .eventoId(lineaDTO.getEventoId())
                    .paseId(lineaDTO.getPaseId())
                    .build();

            pedido.addLinea(linea);

            // Si es entrada o pase, generar registros de EntradaAdquirida por cada unidad
            if (producto.getTipo() == TipoProducto.ENTRADA || producto.getTipo() == TipoProducto.PASE) {
                for (int i = 0; i < lineaDTO.getCantidad(); i++) {
                    String entradaId = "ent-" + UUID.randomUUID().toString().substring(0, 8);
                    EntradaAdquirida entrada = EntradaAdquirida.builder()
                            .id(entradaId)
                            .pedido(pedido)
                            .usuarioId(usuarioId)
                            .productoId(producto.getId())
                            .eventoId(lineaDTO.getEventoId())
                            .paseId(lineaDTO.getPaseId())
                            .fechaCompra(ahora)
                            .estado("VALIDA")
                            .build();

                    pedido.addEntrada(entrada);
                }
            }
        }

        pedido.setImporteTotal(importeTotal);

        // Guardar stock actualizado
        productoRepository.saveAll(productosParaActualizar);

        // Guardar pedido (en cascada guarda líneas y entradas)
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        return mapToDTO(pedidoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidosUsuario(String usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByFechaCompraDesc(usuarioId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPedidoPorId(String pedidoId, String usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("PEDIDO_NO_ENCONTRADO", "Pedido no encontrado: " + pedidoId));

        if (!pedido.getUsuarioId().equals(usuarioId)) {
            throw new ForbiddenException("PEDIDO_NO_PERMANECE_A_USUARIO", "El pedido no pertenece al usuario autenticado");
        }

        return mapToDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoDTO solicitarCancelacionPedido(String pedidoId, String usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("PEDIDO_NO_ENCONTRADO", "Pedido no encontrado: " + pedidoId));

        if (!pedido.getUsuarioId().equals(usuarioId)) {
            throw new ForbiddenException("PEDIDO_NO_PERMANECE_A_USUARIO", "El pedido no pertenece al usuario autenticado");
        }

        if ("CANCELACION_SOLICITADA".equalsIgnoreCase(pedido.getEstado()) || "CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new ConflictException("CANCELACION_YA_SOLICITADA", "El pedido no puede volver a solicitar su cancelación");
        }

        pedido.setEstado("CANCELACION_SOLICITADA");

        if (pedido.getEntradas() != null) {
            for (EntradaAdquirida entrada : pedido.getEntradas()) {
                if (!"CANCELADA".equalsIgnoreCase(entrada.getEstado())) {
                    entrada.setEstado("CANCELACION_SOLICITADA");
                }
            }
        }

        Pedido guardado = pedidoRepository.save(pedido);
        return mapToDTO(guardado);
    }

    private PedidoDTO mapToDTO(Pedido p) {
        List<LineaPedidoDTO> lineas = p.getLineas().stream().map(l -> LineaPedidoDTO.builder()
                .productoId(l.getProductoId())
                .nombreProducto(l.getNombreProducto())
                .cantidad(l.getCantidad())
                .precioUnitario(l.getPrecioUnitario())
                .subtotal(l.getSubtotal())
                .eventoId(l.getEventoId())
                .paseId(l.getPaseId())
                .build()).collect(Collectors.toList());

        List<EntradaAdquiridaDTO> entradas = p.getEntradas().stream().map(e -> EntradaAdquiridaDTO.builder()
                .id(e.getId())
                .pedidoId(p.getId())
                .usuarioId(e.getUsuarioId())
                .productoId(e.getProductoId())
                .eventoId(e.getEventoId())
                .paseId(e.getPaseId())
                .fechaCompra(e.getFechaCompra())
                .estado(e.getEstado())
                .build()).collect(Collectors.toList());

        return PedidoDTO.builder()
                .id(p.getId())
                .usuarioId(p.getUsuarioId())
                .lineas(lineas)
                .fechaCompra(p.getFechaCompra())
                .importeTotal(p.getImporteTotal())
                .estado(p.getEstado())
                .entradas(entradas)
                .build();
    }
}
