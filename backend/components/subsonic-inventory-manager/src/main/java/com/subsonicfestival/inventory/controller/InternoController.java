package com.subsonicfestival.inventory.controller;

import com.subsonicfestival.inventory.dto.ComprobacionPaseDTO;
import com.subsonicfestival.inventory.dto.ComprobacionRolDTO;
import com.subsonicfestival.inventory.service.InternoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interno")
@Tag(name = "Uso Interno", description = "Operaciones utilizadas para la comunicación entre gestores")
public class InternoController {

    private final InternoService internoService;

    public InternoController(InternoService internoService) {
        this.internoService = internoService;
    }

    //GET /interno/usuarios {user_id}/verificar-cliente
    @GetMapping("/usuarios/{user_id}/verificar-cliente")
    @Operation(summary = "Comprobar que un usuario tiene rol cliente",
            description = "Endpoint interno consumido antes de crear un pedido.")
    public ResponseEntity<ComprobacionRolDTO> verificarCliente(
            @Parameter(description = "Identificador del usuario", required = true)
            @PathVariable("user_id") String userId) {
        ComprobacionRolDTO resultado = internoService.verificarCliente(userId);
        return ResponseEntity.ok(resultado);
    }

    //GET /interno/eventos {evento_id}/pases/{pase_id}
    @GetMapping("/eventos/{evento_id}/pases/{pase_id}")
    @Operation(summary = "Comprobar la existencia de un evento y un pase",
            description = "Endpoint interno utilizado antes de registrar la compra de un pase.")
    public ResponseEntity<ComprobacionPaseDTO> verificarEventoPase(
            @Parameter(description = "Identificador del festival/evento", required = true)
            @PathVariable("evento_id") String eventoId,
            @Parameter(description = "Identificador del pase dentro del evento", required = true)
            @PathVariable("pase_id") String paseId) {
        ComprobacionPaseDTO resultado = internoService.verificarEventoPase(eventoId, paseId);
        return ResponseEntity.ok(resultado);
    }
}
