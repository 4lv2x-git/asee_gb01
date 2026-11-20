package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.PedidoCreacionDTO;
import com.subsonicfestival.inventory.dto.PedidoDTO;

import java.util.List;

public interface PedidoService {

    PedidoDTO crearPedido(String usuarioId, PedidoCreacionDTO pedidoCreacionDTO);

    List<PedidoDTO> listarPedidosUsuario(String usuarioId);

    PedidoDTO obtenerPedidoPorId(String pedidoId, String usuarioId);

    PedidoDTO solicitarCancelacionPedido(String pedidoId, String usuarioId);
}
