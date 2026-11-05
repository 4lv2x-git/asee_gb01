package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.ProductoDTO;
import com.subsonicfestival.inventory.entity.Producto;
import com.subsonicfestival.inventory.entity.TipoProducto;
import com.subsonicfestival.inventory.exception.BadRequestException;
import com.subsonicfestival.inventory.exception.ResourceNotFoundException;
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
}
