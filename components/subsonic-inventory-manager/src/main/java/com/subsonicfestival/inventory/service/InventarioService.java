package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.ProductoActualizacionDTO;
import com.subsonicfestival.inventory.dto.ProductoCreacionDTO;
import com.subsonicfestival.inventory.dto.ProductoDTO;

import java.util.List;

public interface InventarioService {

    ProductoDTO crearProducto(ProductoCreacionDTO dto);

    List<ProductoDTO> listarProductos(String tipo, Boolean disponible);

    ProductoDTO obtenerProductoPorId(String id);

    ProductoDTO actualizarProducto(String id, ProductoActualizacionDTO dto);

    ProductoDTO actualizarStock(String id, Integer nuevoStock);

    void eliminarProducto(String id);
}
