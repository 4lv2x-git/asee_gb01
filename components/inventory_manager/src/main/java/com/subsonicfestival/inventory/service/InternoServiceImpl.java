package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.ComprobacionPaseDTO;
import com.subsonicfestival.inventory.dto.ComprobacionRolDTO;
import com.subsonicfestival.inventory.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class InternoServiceImpl implements InternoService {

    @Override
    public ComprobacionRolDTO verificarCliente(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResourceNotFoundException("USUARIO_NO_ENCONTRADO", "Usuario no encontrado: " + userId);
        }
        // Simulación de respuesta o validación contra el Gestor de Usuarios
        // Si el userId es "admin" o similar sin rol cliente podría dar false
        boolean esCliente = !userId.toLowerCase().contains("no-cliente");

        return ComprobacionRolDTO.builder()
                .usuarioId(userId)
                .esCliente(esCliente)
                .build();
    }

    @Override
    public ComprobacionPaseDTO verificarEventoPase(String eventoId, String paseId) {
        if (eventoId == null || eventoId.isBlank() || paseId == null || paseId.isBlank()) {
            throw new ResourceNotFoundException("EVENTO_PASE_NO_ENCONTRADO", "El evento o el pase no existe");
        }

        // Si contiene "invalido" o "inexistente", simulamos no encontrado
        if (eventoId.toLowerCase().contains("inexistente") || paseId.toLowerCase().contains("inexistente")) {
            throw new ResourceNotFoundException("EVENTO_PASE_NO_ENCONTRADO", "El evento o el pase no existe");
        }

        return ComprobacionPaseDTO.builder()
                .eventoId(eventoId)
                .paseId(paseId)
                .existe(true)
                .nombrePase("Pase Oficial - " + paseId)
                .precio(75.0)
                .build();
    }
}
