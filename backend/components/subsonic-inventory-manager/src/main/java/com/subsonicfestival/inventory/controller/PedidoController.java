package com.subsonicfestival.inventory.controller;

import com.subsonicfestival.inventory.config.UserContextHelper;
import com.subsonicfestival.inventory.dto.PedidoCreacionDTO;
import com.subsonicfestival.inventory.dto.PedidoDTO;
import com.subsonicfestival.inventory.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Creación y consulta de pedidos asociados al cliente autenticado")
@SecurityRequirement(name = "OAuth2")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UserContextHelper userContextHelper;

    public PedidoController(PedidoService pedidoService, UserContextHelper userContextHelper) {
        this.pedidoService = pedidoService;
        this.userContextHelper = userContextHelper;
    }

    //POST /pedidos
    @PostMapping
    @Operation(summary = "Crear un pedido",
            description = "Crea un pedido a partir de los productos seleccionados por el cliente.")
    public ResponseEntity<PedidoDTO> crearPedido(
            @Valid @RequestBody PedidoCreacionDTO pedidoCreacionDTO,
            HttpServletRequest request) {
        String usuarioId = userContextHelper.obtenerUsuarioOpcional(request, "usr-cliente-001");
        PedidoDTO creado = pedidoService.crearPedido(usuarioId, pedidoCreacionDTO);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    //GET /pedidos
    @GetMapping
    @Operation(summary = "Consultar los pedidos del cliente autenticado",
            description = "Devuelve el historial de pedidos asociado al usuario de la sesión.")
    public ResponseEntity<List<PedidoDTO>> listarPedidos(HttpServletRequest request) {
        String usuarioId = userContextHelper.obtenerUsuarioOpcional(request, "usr-cliente-001");
        List<PedidoDTO> pedidos = pedidoService.listarPedidosUsuario(usuarioId);
        return ResponseEntity.ok(pedidos);
    }

    //GET /pedidos/{pedido_id}
    @GetMapping("/{pedido_id}")
    @Operation(summary = "Consultar el detalle de un pedido propio",
            description = "Devuelve un pedido concreto del cliente autenticado.")
    public ResponseEntity<PedidoDTO> obtenerPedidoPorId(
            @Parameter(description = "Identificador único del pedido", required = true)
            @PathVariable("pedido_id") String pedidoId,
            HttpServletRequest request) {
        String usuarioId = userContextHelper.obtenerUsuarioOpcional(request, "usr-cliente-001");
        PedidoDTO pedido = pedidoService.obtenerPedidoPorId(pedidoId, usuarioId);
        return ResponseEntity.ok(pedido);
    }

    //POST /pedidos/{pedido_id}/cancelacion
    @PostMapping("/{pedido_id}/cancelacion")
    @Operation(summary = "Solicitar la cancelación de un pedido",
            description = "Registra una solicitud de cancelación para un pedido del cliente autenticado.")
    public ResponseEntity<PedidoDTO> solicitarCancelacionPedido(
            @Parameter(description = "Identificador único del pedido", required = true)
            @PathVariable("pedido_id") String pedidoId,
            HttpServletRequest request) {
        String usuarioId = userContextHelper.obtenerUsuarioOpcional(request, "usr-cliente-001");
        PedidoDTO pedido = pedidoService.solicitarCancelacionPedido(pedidoId, usuarioId);
        return ResponseEntity.ok(pedido);
    }
}
