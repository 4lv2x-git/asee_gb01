package com.subsonicfestival.inventory.config;

import com.subsonicfestival.inventory.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class UserContextHelper {

    public String obtenerUsuarioAutenticado(HttpServletRequest request) {
        String xUserId = request.getHeader("X-User-Id");
        if (xUserId != null && !xUserId.isBlank()) {
            return xUserId.trim();
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) {
                // Si el token es un ID directo o contiene información de usuario
                if (token.contains(".")) {
                    // Token JWT simplificado: si es decodificable o simulación
                    return "usr-cliente-001";
                }
                return token;
            }
        }

        // Si no se envió cabecera de autenticación requerida
        throw new UnauthorizedException("Token no recibido o inválido");
    }

    public String obtenerUsuarioOpcional(HttpServletRequest request, String fallbackDefault) {
        try {
            return obtenerUsuarioAutenticado(request);
        } catch (UnauthorizedException e) {
            return fallbackDefault != null ? fallbackDefault : "usr-cliente-001";
        }
    }
}
