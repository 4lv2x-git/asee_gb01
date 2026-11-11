package com.subsonicfestival.frontend.web;

import java.util.Arrays;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Textos derivados que se usan en las plantillas Thymeleaf.
 */
@Component("textos")
public class Textos {

    public String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] partes = Arrays.stream(nombre.trim().split("\\s+"))
                .filter(parte -> !parte.isBlank())
                .limit(2)
                .toArray(String[]::new);
        StringBuilder resultado = new StringBuilder();
        for (String parte : partes) {
            resultado.append(parte.substring(0, 1).toUpperCase(Locale.ROOT));
        }
        return resultado.toString();
    }

    public String rol(String rol) {
        if (rol == null) {
            return "Visitante";
        }
        return switch (rol) {
            case "cliente" -> "Cliente";
            case "proveedor" -> "Proveedor";
            case "admin" -> "Administrador";
            default -> "Visitante";
        };
    }

    public String estado(String estado) {
        return "suspendido".equals(estado) ? "Suspendido" : "Activo";
    }
}
