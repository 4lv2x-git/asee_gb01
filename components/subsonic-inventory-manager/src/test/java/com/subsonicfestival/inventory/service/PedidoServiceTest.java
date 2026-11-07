package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.*;
import com.subsonicfestival.inventory.entity.*;
import com.subsonicfestival.inventory.exception.ConflictException;
import com.subsonicfestival.inventory.exception.ForbiddenException;
import com.subsonicfestival.inventory.exception.InsufficientStockException;
import com.subsonicfestival.inventory.exception.ResourceNotFoundException;
import com.subsonicfestival.inventory.repository.PedidoRepository;
import com.subsonicfestival.inventory.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InternoService internoService;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = Producto.builder()
                .id("prod-001")
                .nombre("Entrada General")
                .precio(40.0)
                .tipo(TipoProducto.ENTRADA)
                .stockDisponible(10)
                .build();
    }

    @Test
    void testCrearPedidoExitoso() {
        when(internoService.verificarCliente("usr-001"))
                .thenReturn(new ComprobacionRolDTO("usr-001", true));
        when(productoRepository.findById("prod-001"))
                .thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(i -> i.getArgument(0));

        PedidoCreacionDTO creacionDTO = PedidoCreacionDTO.builder()
                .lineas(List.of(LineaPedidoCreacionDTO.builder()
                        .productoId("prod-001")
                        .cantidad(2)
                        .build()))
                .build();

        PedidoDTO pedidoCreado = pedidoService.crearPedido("usr-001", creacionDTO);

        assertNotNull(pedidoCreado);
        assertEquals("usr-001", pedidoCreado.getUsuarioId());
        assertEquals(80.0, pedidoCreado.getImporteTotal());
        assertEquals(1, pedidoCreado.getLineas().size());
        assertEquals(2, pedidoCreado.getEntradas().size()); // 2 entradas generadas
        assertEquals(8, producto.getStockDisponible()); // Stock decrementado
    }

    @Test
    void testCrearPedidoSinRolCliente() {
        when(internoService.verificarCliente("usr-admin"))
                .thenReturn(new ComprobacionRolDTO("usr-admin", false));

        PedidoCreacionDTO creacionDTO = PedidoCreacionDTO.builder()
                .lineas(List.of(LineaPedidoCreacionDTO.builder()
                        .productoId("prod-001")
                        .cantidad(1)
                        .build()))
                .build();

        assertThrows(ForbiddenException.class, () ->
                pedidoService.crearPedido("usr-admin", creacionDTO));
    }

    @Test
    void testCrearPedidoStockInsuficiente() {
        when(internoService.verificarCliente("usr-001"))
                .thenReturn(new ComprobacionRolDTO("usr-001", true));
        when(productoRepository.findById("prod-001"))
                .thenReturn(Optional.of(producto));

        PedidoCreacionDTO creacionDTO = PedidoCreacionDTO.builder()
                .lineas(List.of(LineaPedidoCreacionDTO.builder()
                        .productoId("prod-001")
                        .cantidad(20) // mayor que el stock de 10
                        .build()))
                .build();

        assertThrows(InsufficientStockException.class, () ->
                pedidoService.crearPedido("usr-001", creacionDTO));
    }

    @Test
    void testSolicitarCancelacionPedidoExitoso() {
        EntradaAdquirida entrada = EntradaAdquirida.builder()
                .id("ent-001")
                .usuarioId("usr-001")
                .productoId("prod-001")
                .estado("VALIDA")
                .build();

        Pedido pedido = Pedido.builder()
                .id("ped-001")
                .usuarioId("usr-001")
                .fechaCompra(LocalDateTime.now())
                .importeTotal(40.0)
                .estado("COMPLETADO")
                .lineas(new ArrayList<>())
                .entradas(new ArrayList<>(List.of(entrada)))
                .build();
        entrada.setPedido(pedido);

        when(pedidoRepository.findById("ped-001")).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        PedidoDTO resultado = pedidoService.solicitarCancelacionPedido("ped-001", "usr-001");

        assertNotNull(resultado);
        assertEquals("CANCELACION_SOLICITADA", resultado.getEstado());
        assertEquals(1, resultado.getEntradas().size());
        assertEquals("CANCELACION_SOLICITADA", resultado.getEntradas().get(0).getEstado());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void testSolicitarCancelacionPedidoNoPerteneceAUsuario() {
        Pedido pedido = Pedido.builder()
                .id("ped-001")
                .usuarioId("usr-otro")
                .estado("COMPLETADO")
                .build();

        when(pedidoRepository.findById("ped-001")).thenReturn(Optional.of(pedido));

        assertThrows(ForbiddenException.class, () ->
                pedidoService.solicitarCancelacionPedido("ped-001", "usr-001"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void testSolicitarCancelacionPedidoYaCancelado() {
        Pedido pedido = Pedido.builder()
                .id("ped-001")
                .usuarioId("usr-001")
                .estado("CANCELACION_SOLICITADA")
                .build();

        when(pedidoRepository.findById("ped-001")).thenReturn(Optional.of(pedido));

        assertThrows(ConflictException.class, () ->
                pedidoService.solicitarCancelacionPedido("ped-001", "usr-001"));
        verify(pedidoRepository, never()).save(any());
    }
}
