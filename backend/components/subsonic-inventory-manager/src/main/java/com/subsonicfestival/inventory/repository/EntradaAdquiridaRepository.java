package com.subsonicfestival.inventory.repository;

import com.subsonicfestival.inventory.entity.EntradaAdquirida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntradaAdquiridaRepository extends JpaRepository<EntradaAdquirida, String> {

    List<EntradaAdquirida> findByUsuarioId(String usuarioId);

    List<EntradaAdquirida> findByPedidoId(String pedidoId);
}
