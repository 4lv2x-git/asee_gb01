package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.*;
import com.subsonicfestival.inventory.entity.*;
import com.subsonicfestival.inventory.exception.ForbiddenException;
import com.subsonicfestival.inventory.exception.InsufficientStockException;
import com.subsonicfestival.inventory.repository.PedidoRepository;
import com.subsonicfestival.inventory.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
