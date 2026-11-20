package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.ProductoActualizacionDTO;
import com.subsonicfestival.inventory.dto.ProductoCreacionDTO;
import com.subsonicfestival.inventory.dto.ProductoDTO;
import com.subsonicfestival.inventory.entity.Producto;
import com.subsonicfestival.inventory.entity.TipoProducto;
import com.subsonicfestival.inventory.exception.BadRequestException;
import com.subsonicfestival.inventory.exception.ConflictException;
import com.subsonicfestival.inventory.exception.ResourceNotFoundException;
import com.subsonicfestival.inventory.repository.LineaPedidoRepository;
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
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private LineaPedidoRepository lineaPedidoRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Producto productoPrueba;

    @BeforeEach
    void setUp() {
        productoPrueba = Producto.builder()
                .id("prod-001")
                .nombre("Entrada General")
                .descripcion("Acceso al festival")
                .precio(50.0)
                .tipo(TipoProducto.ENTRADA)
                .stockDisponible(100)
                .build();
    }

    @Test
    void testCrearProductoValido() {
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        ProductoCreacionDTO dto = ProductoCreacionDTO.builder()
                .nombre("Camiseta Oficial")
                .descripcion("Camiseta negra")
                .precio(25.0)
                .tipo("merchandising")
                .stockDisponible(50)
                .build();

        ProductoDTO resultado = inventarioService.crearProducto(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals("Camiseta Oficial", resultado.getNombre());
        assertEquals(25.0, resultado.getPrecio());
        assertEquals("merchandising", resultado.getTipo());
        assertEquals(50, resultado.getStockDisponible());
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void testCrearProductoTipoInvalido() {
        ProductoCreacionDTO dto = ProductoCreacionDTO.builder()
                .nombre("Producto Invalido")
                .precio(10.0)
                .tipo("tipo_inexistente")
                .stockDisponible(10)
                .build();

        assertThrows(BadRequestException.class, () -> inventarioService.crearProducto(dto));
    }

    @Test
    void testListarProductosSinFiltros() {
        when(productoRepository.findAll()).thenReturn(List.of(productoPrueba));

        List<ProductoDTO> resultado = inventarioService.listarProductos(null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("prod-001", resultado.get(0).getId());
        verify(productoRepository).findAll();
    }

    @Test
    void testObtenerProductoPorIdExistente() {
        when(productoRepository.findById("prod-001")).thenReturn(Optional.of(productoPrueba));

        ProductoDTO resultado = inventarioService.obtenerProductoPorId("prod-001");

        assertNotNull(resultado);
        assertEquals("prod-001", resultado.getId());
        assertEquals(100, resultado.getStockDisponible());
    }

    @Test
    void testObtenerProductoPorIdNoExistente() {
        when(productoRepository.findById("no-existe")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                inventarioService.obtenerProductoPorId("no-existe"));
    }

    @Test
    void testActualizarProductoValido() {
        when(productoRepository.findById("prod-001")).thenReturn(Optional.of(productoPrueba));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        ProductoActualizacionDTO actualizacionDTO = ProductoActualizacionDTO.builder()
                .nombre("Entrada VIP Modificada")
                .precio(80.0)
                .build();

        ProductoDTO resultado = inventarioService.actualizarProducto("prod-001", actualizacionDTO);

        assertNotNull(resultado);
        assertEquals("Entrada VIP Modificada", resultado.getNombre());
        assertEquals(80.0, resultado.getPrecio());
        assertEquals(100, resultado.getStockDisponible()); // Stock no modificado
        verify(productoRepository).save(productoPrueba);
    }

    @Test
    void testActualizarStockValido() {
        when(productoRepository.findById("prod-001")).thenReturn(Optional.of(productoPrueba));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        ProductoDTO resultado = inventarioService.actualizarStock("prod-001", 150);

        assertNotNull(resultado);
        assertEquals(150, resultado.getStockDisponible());
        verify(productoRepository).save(productoPrueba);
    }

    @Test
    void testActualizarStockNegativo() {
        assertThrows(BadRequestException.class, () ->
                inventarioService.actualizarStock("prod-001", -5));
    }

    @Test
    void testEliminarProductoExitoso() {
        when(productoRepository.findById("prod-001")).thenReturn(Optional.of(productoPrueba));
        when(lineaPedidoRepository.existsByProductoId("prod-001")).thenReturn(false);

        inventarioService.eliminarProducto("prod-001");

        verify(productoRepository).delete(productoPrueba);
    }

    @Test
    void testEliminarProductoAsociadoAPedidos() {
        when(productoRepository.findById("prod-001")).thenReturn(Optional.of(productoPrueba));
        when(lineaPedidoRepository.existsByProductoId("prod-001")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                inventarioService.eliminarProducto("prod-001"));
        verify(productoRepository, never()).delete(any());
    }
}
