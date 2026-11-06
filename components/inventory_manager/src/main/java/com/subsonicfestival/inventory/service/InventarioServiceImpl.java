package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.ProductoDTO;
import com.subsonicfestival.inventory.entity.Producto;
import com.subsonicfestival.inventory.entity.TipoProducto;
import com.subsonicfestival.inventory.exception.BadRequestException;
import com.subsonicfestival.inventory.exception.ResourceNotFoundException;
import com.subsonicfestival.inventory.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;

    public InventarioServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos(String tipoStr, Boolean disponible) {
        TipoProducto tipo = null;
        if (tipoStr != null && !tipoStr.isBlank()) {
            try {
                tipo = TipoProducto.fromString(tipoStr);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("TIPO_INVALIDO", "Tipo de producto no válido: " + tipoStr);
            }
        }

        List<Producto> productos;

        if (tipo != null && Boolean.TRUE.equals(disponible)) {
            productos = productoRepository.findByTipoAndStockDisponibleGreaterThan(tipo, 0);
        } else if (tipo != null) {
            productos = productoRepository.findByTipo(tipo);
        } else if (Boolean.TRUE.equals(disponible)) {
            productos = productoRepository.findByStockDisponibleGreaterThan(0);
        } else {
            productos = productoRepository.findAll();
        }

        return productos.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerProductoPorId(String id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCTO_NO_ENCONTRADO", "Producto no encontrado con ID: " + id));
        return mapToDTO(producto);
    }

    @Override
    @Transactional
    public ProductoDTO actualizarStock(String id, Integer nuevoStock) {
        if (nuevoStock == null || nuevoStock < 0) {
            throw new BadRequestException("STOCK_INVALIDO", "La cantidad de stock no puede ser negativa ni nula.");
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCTO_NO_ENCONTRADO", "Producto no encontrado con ID: " + id));

        producto.setStockDisponible(nuevoStock);
        Producto guardado = productoRepository.save(producto);
        return mapToDTO(guardado);
    }

    private ProductoDTO mapToDTO(Producto p) {
        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .tipo(p.getTipo() != null ? p.getTipo().getValor() : null)
                .stockDisponible(p.getStockDisponible())
                .build();
    }
}
