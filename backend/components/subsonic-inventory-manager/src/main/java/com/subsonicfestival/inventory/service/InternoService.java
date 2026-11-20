package com.subsonicfestival.inventory.service;

import com.subsonicfestival.inventory.dto.ComprobacionPaseDTO;
import com.subsonicfestival.inventory.dto.ComprobacionRolDTO;

public interface InternoService {

    ComprobacionRolDTO verificarCliente(String userId);

    ComprobacionPaseDTO verificarEventoPase(String eventoId, String paseId);
}
