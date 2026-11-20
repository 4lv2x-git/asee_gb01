package com.subsonicfestival.inventory.repository;

import com.subsonicfestival.inventory.entity.LineaPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineaPedidoRepository extends JpaRepository<LineaPedido, Long> {

    List<LineaPedido> findByPedidoId(String pedidoId);

    boolean existsByProductoId(String productoId);
}
