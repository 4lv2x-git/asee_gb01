package com.subsonicfestival.frontend.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    /** Formatea una fecha ISO (por ejemplo {@code 2027-06-18T00:00:00}). */
    public String fechaCorta(String iso) {
        if (iso == null || iso.length() < 10) {
            return "Fecha por confirmar";
        }
        try {
            LocalDate fecha = LocalDate.parse(iso.substring(0, 10));
            return fecha.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")));
        } catch (RuntimeException ex) {
            return iso.substring(0, 10);
        }
    }

    public String estadoEvento(String estado) {
        if (estado == null) {
            return "Sin estado";
        }
        return switch (estado.toLowerCase(Locale.ROOT)) {
            case "publicado" -> "Publicado";
            case "borrador" -> "Borrador";
            case "cancelado" -> "Cancelado";
            case "finalizado" -> "Finalizado";
            default -> estado;
        };
    }

    public String claseEstadoEvento(String estado) {
        if (estado == null) {
            return "visitante";
        }
        return switch (estado.toLowerCase(Locale.ROOT)) {
            case "publicado" -> "activo";
            case "borrador" -> "warning";
            case "cancelado" -> "suspendido";
            default -> "visitante";
        };
    }

    public String tipoProducto(String tipo) {
        if (tipo == null) {
            return "Producto";
        }
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "entrada" -> "Entrada";
            case "pase" -> "Pase";
            case "merchandising" -> "Merchandising";
            default -> tipo;
        };
    }

    public String claseTipoProducto(String tipo) {
        if (tipo == null) {
            return "visitante";
        }
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "entrada" -> "cliente";
            case "pase" -> "proveedor";
            case "merchandising" -> "admin";
            default -> "visitante";
        };
    }

    public String moneda(Double valor) {
        double importe = valor == null ? 0 : Math.max(0, valor);
        return java.text.NumberFormat
                .getCurrencyInstance(Locale.forLanguageTag("es-ES"))
                .format(importe);
    }

    public String claseStock(Integer stock) {
        int unidades = stock == null ? 0 : Math.max(0, stock);
        if (unidades <= 0) {
            return "suspendido";
        }
        if (unidades <= 10) {
            return "warning";
        }
        return "activo";
    }
}
