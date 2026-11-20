package com.subsonicfestival.inventory.controller;

import com.subsonicfestival.inventory.config.UserContextHelper;
import com.subsonicfestival.inventory.dto.EntradaAdquiridaDTO;
import com.subsonicfestival.inventory.service.EntradaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entradas")
@Tag(name = "Entradas", description = "Gestión de entradas o pases adquiridos y solicitudes de cancelación")
@SecurityRequirement(name = "OAuth2")
public class EntradaController {

    private final EntradaService entradaService;
    private final UserContextHelper userContextHelper;

    public EntradaController(EntradaService entradaService, UserContextHelper userContextHelper) {
        this.entradaService = entradaService;
        this.userContextHelper = userContextHelper;
    }

    //GET /entradas {entrada_id}
    @GetMapping("/{entrada_id}")
    @Operation(summary = "Consultar una entrada o pase adquirido",
            description = "Devuelve los datos de una entrada o pase asociado al cliente autenticado.")
    public ResponseEntity<EntradaAdquiridaDTO> obtenerEntradaPorId(
            @Parameter(description = "Identificador único de la entrada adquirida", required = true)
            @PathVariable("entrada_id") String entradaId,
            HttpServletRequest request) {
        String usuarioId = userContextHelper.obtenerUsuarioOpcional(request, "usr-cliente-001");
        EntradaAdquiridaDTO entrada = entradaService.obtenerEntradaPorId(entradaId, usuarioId);
        return ResponseEntity.ok(entrada);
    }

    //POST /entradas {entrada_id}/cancelacion
    @PostMapping("/{entrada_id}/cancelacion")
    @Operation(summary = "Solicitar la cancelación de una entrada adquirida",
            description = "Registra una solicitud de cancelación para una entrada o pase comprado.")
    public ResponseEntity<EntradaAdquiridaDTO> solicitarCancelacion(
            @Parameter(description = "Identificador único de la entrada adquirida", required = true)
            @PathVariable("entrada_id") String entradaId,
            HttpServletRequest request) {
        String usuarioId = userContextHelper.obtenerUsuarioOpcional(request, "usr-cliente-001");
        EntradaAdquiridaDTO entrada = entradaService.solicitarCancelacion(entradaId, usuarioId);
        return ResponseEntity.ok(entrada);
    }
}
