package com.subsonicfestival.inventory.repository;

import com.subsonicfestival.inventory.entity.Producto;
import com.subsonicfestival.inventory.entity.TipoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    List<Producto> findByTipo(TipoProducto tipo);

    List<Producto> findByStockDisponibleGreaterThan(Integer stock);

    List<Producto> findByTipoAndStockDisponibleGreaterThan(TipoProducto tipo, Integer stock);
}
