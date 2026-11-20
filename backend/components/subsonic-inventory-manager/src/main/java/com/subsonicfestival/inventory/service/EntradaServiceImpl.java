package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.EntradaAdquiridaDTO;
import com.subsonicfestival.inventory.entity.EntradaAdquirida;
import com.subsonicfestival.inventory.exception.ConflictException;
import com.subsonicfestival.inventory.exception.ForbiddenException;
import com.subsonicfestival.inventory.exception.ResourceNotFoundException;
import com.subsonicfestival.inventory.repository.EntradaAdquiridaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntradaServiceImpl implements EntradaService {

    private final EntradaAdquiridaRepository entradaRepository;

    public EntradaServiceImpl(EntradaAdquiridaRepository entradaRepository) {
        this.entradaRepository = entradaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EntradaAdquiridaDTO obtenerEntradaPorId(String entradaId, String usuarioId) {
        EntradaAdquirida entrada = entradaRepository.findById(entradaId)
                .orElseThrow(() -> new ResourceNotFoundException("ENTRADA_NO_ENCONTRADA", "Entrada no encontrada: " + entradaId));

        if (!entrada.getUsuarioId().equals(usuarioId)) {
            throw new ForbiddenException("ENTRADA_NO_PERMANECE_A_USUARIO", "La entrada no pertenece al usuario autenticado");
        }

        return mapToDTO(entrada);
    }

    @Override
    @Transactional
    public EntradaAdquiridaDTO solicitarCancelacion(String entradaId, String usuarioId) {
        EntradaAdquirida entrada = entradaRepository.findById(entradaId)
                .orElseThrow(() -> new ResourceNotFoundException("ENTRADA_NO_ENCONTRADA", "Entrada no encontrada: " + entradaId));

        if (!entrada.getUsuarioId().equals(usuarioId)) {
            throw new ForbiddenException("ENTRADA_NO_PERMANECE_A_USUARIO", "La entrada no pertenece al usuario autenticado");
        }

        if ("CANCELACION_SOLICITADA".equalsIgnoreCase(entrada.getEstado()) || "CANCELADA".equalsIgnoreCase(entrada.getEstado())) {
            throw new ConflictException("CANCELACION_YA_SOLICITADA", "La entrada no puede volver a solicitar su cancelación");
        }

        entrada.setEstado("CANCELACION_SOLICITADA");
        EntradaAdquirida guardada = entradaRepository.save(entrada);

        return mapToDTO(guardada);
    }

    private EntradaAdquiridaDTO mapToDTO(EntradaAdquirida e) {
        return EntradaAdquiridaDTO.builder()
                .id(e.getId())
                .pedidoId(e.getPedido() != null ? e.getPedido().getId() : null)
                .usuarioId(e.getUsuarioId())
                .productoId(e.getProductoId())
                .eventoId(e.getEventoId())
                .paseId(e.getPaseId())
                .fechaCompra(e.getFechaCompra())
                .estado(e.getEstado())
                .build();
    }
}
