package com.subsonicfestival.inventory.controller;

import com.subsonicfestival.inventory.dto.ProductoActualizacionDTO;
import com.subsonicfestival.inventory.dto.ProductoCreacionDTO;
import com.subsonicfestival.inventory.dto.ProductoDTO;
import com.subsonicfestival.inventory.dto.StockActualizacionDTO;
import com.subsonicfestival.inventory.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario/productos")
@Tag(name = "Inventario", description = "Consulta y mantenimiento del catálogo de productos y su stock")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    //POST /inventario/productos
    @PostMapping
    @Operation(summary = "Crear un producto",
            description = "Registra un nuevo producto en el catálogo del inventario.")
    public ResponseEntity<ProductoDTO> crearProducto(
            @Valid @RequestBody ProductoCreacionDTO productoCreacionDTO) {
        ProductoDTO creado = inventarioService.crearProducto(productoCreacionDTO);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    //GET /inventario/productos
    @GetMapping
    @Operation(summary = "Consultar productos y stock disponible",
            description = "Devuelve los productos gestionados por el componente y la cantidad de stock disponible.")
    public ResponseEntity<List<ProductoDTO>> listarProductos(
            @Parameter(description = "Filtra los productos por tipo (entrada, pase, merchandising)")
            @RequestParam(required = false) String tipo,
            @Parameter(description = "Si se indica true, devuelve únicamente productos con stock mayor que cero")
            @RequestParam(required = false) Boolean disponible) {
        List<ProductoDTO> productos = inventarioService.listarProductos(tipo, disponible);
        return ResponseEntity.ok(productos);
    }

    //GET /inventario/productos/{producto_id}
    @GetMapping("/{producto_id}")
    @Operation(summary = "Consultar un producto y su stock",
            description = "Devuelve el detalle de un producto, incluyendo su stock disponible.")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable("producto_id") String productoId) {
        ProductoDTO producto = inventarioService.obtenerProductoPorId(productoId);
        return ResponseEntity.ok(producto);
    }

    //PATCH /inventario/productos/{producto_id}
    @PatchMapping("/{producto_id}")
    @Operation(summary = "Actualizar los datos de un producto",
            description = "Modifica uno o varios datos descriptivos o comerciales de un producto.")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable("producto_id") String productoId,
            @Valid @RequestBody ProductoActualizacionDTO actualizacionDTO) {
        ProductoDTO actualizado = inventarioService.actualizarProducto(productoId, actualizacionDTO);
        return ResponseEntity.ok(actualizado);
    }

    //DELETE /inventario/productos/{producto_id}
    @DeleteMapping("/{producto_id}")
    @Operation(summary = "Eliminar un producto",
            description = "Elimina un producto del catálogo.")
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable("producto_id") String productoId) {
        inventarioService.eliminarProducto(productoId);
        return ResponseEntity.noContent().build();
    }

    //PATCH /inventario/productos/{producto_id}/stock
    @PatchMapping("/{producto_id}/stock")
    @Operation(summary = "Actualizar el stock disponible de un producto",
            description = "Modifica la cantidad de stock disponible de un producto.")
    public ResponseEntity<ProductoDTO> actualizarStock(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable("producto_id") String productoId,
            @Valid @RequestBody StockActualizacionDTO stockDTO) {
        ProductoDTO actualizado = inventarioService.actualizarStock(productoId, stockDTO.getStockDisponible());
        return ResponseEntity.ok(actualizado);
    }
}
