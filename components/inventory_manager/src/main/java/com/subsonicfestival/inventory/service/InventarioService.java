package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.ProductoDTO;

import java.util.List;

public interface InventarioService {

    List<ProductoDTO> listarProductos(String tipo, Boolean disponible);

    ProductoDTO obtenerProductoPorId(String id);

    ProductoDTO actualizarStock(String id, Integer nuevoStock);
}
