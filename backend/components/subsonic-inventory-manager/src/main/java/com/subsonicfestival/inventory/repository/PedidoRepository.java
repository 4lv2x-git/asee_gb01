package com.subsonicfestival.inventory.repository;

import com.subsonicfestival.inventory.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, String> {

    List<Pedido> findByUsuarioIdOrderByFechaCompraDesc(String usuarioId);
}
