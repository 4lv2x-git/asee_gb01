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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;

    public InventarioServiceImpl(ProductoRepository productoRepository, LineaPedidoRepository lineaPedidoRepository) {
        this.productoRepository = productoRepository;
        this.lineaPedidoRepository = lineaPedidoRepository;
    }

    @Override
    @Transactional
    public ProductoDTO crearProducto(ProductoCreacionDTO dto) {
        if (dto == null) {
            throw new BadRequestException("DATOS_INVALIDOS", "Los datos del producto son obligatorios.");
        }

        TipoProducto tipo;
        try {
            tipo = TipoProducto.fromString(dto.getTipo());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("TIPO_INVALIDO", "Tipo de producto no válido: " + dto.getTipo());
        }

        if (dto.getPrecio() == null || dto.getPrecio() < 0) {
            throw new BadRequestException("PRECIO_INVALIDO", "El precio no puede ser negativo ni nulo.");
        }

        if (dto.getStockDisponible() == null || dto.getStockDisponible() < 0) {
            throw new BadRequestException("STOCK_INVALIDO", "El stock disponible no puede ser negativo ni nulo.");
        }

        String id = "prod-" + UUID.randomUUID().toString().substring(0, 8);

        Producto producto = Producto.builder()
                .id(id)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .tipo(tipo)
                .stockDisponible(dto.getStockDisponible())
                .build();

        Producto guardado = productoRepository.save(producto);
        return mapToDTO(guardado);
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
    public ProductoDTO actualizarProducto(String id, ProductoActualizacionDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCTO_NO_ENCONTRADO", "Producto no encontrado con ID: " + id));

        if (dto == null) {
            throw new BadRequestException("DATOS_INVALIDOS", "Los datos de actualización no son válidos.");
        }

        boolean modificado = false;

        if (dto.getNombre() != null) {
            if (dto.getNombre().trim().isEmpty()) {
                throw new BadRequestException("NOMBRE_INVALIDO", "El nombre no puede estar vacío.");
            }
            producto.setNombre(dto.getNombre());
            modificado = true;
        }

        if (dto.getDescripcion() != null) {
            producto.setDescripcion(dto.getDescripcion());
            modificado = true;
        }

        if (dto.getPrecio() != null) {
            if (dto.getPrecio() < 0) {
                throw new BadRequestException("PRECIO_INVALIDO", "El precio no puede ser negativo.");
            }
            producto.setPrecio(dto.getPrecio());
            modificado = true;
        }

        if (dto.getTipo() != null) {
            try {
                TipoProducto tipo = TipoProducto.fromString(dto.getTipo());
                producto.setTipo(tipo);
                modificado = true;
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("TIPO_INVALIDO", "Tipo de producto no válido: " + dto.getTipo());
            }
        }

        if (!modificado) {
            throw new BadRequestException("SIN_CAMBIOS", "Debe proporcionar al menos un campo válido para actualizar.");
        }

        Producto guardado = productoRepository.save(producto);
        return mapToDTO(guardado);
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

    @Override
    @Transactional
    public void eliminarProducto(String id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCTO_NO_ENCONTRADO", "Producto no encontrado con ID: " + id));

        if (lineaPedidoRepository.existsByProductoId(id)) {
            throw new ConflictException("PRODUCTO_ASOCIADO_A_PEDIDOS", "El producto está asociado a pedidos y no se puede eliminar");
        }

        productoRepository.delete(producto);
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
