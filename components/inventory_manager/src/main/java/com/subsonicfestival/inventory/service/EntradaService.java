package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.EntradaAdquiridaDTO;

public interface EntradaService {

    EntradaAdquiridaDTO obtenerEntradaPorId(String entradaId, String usuarioId);

    EntradaAdquiridaDTO solicitarCancelacion(String entradaId, String usuarioId);
}
