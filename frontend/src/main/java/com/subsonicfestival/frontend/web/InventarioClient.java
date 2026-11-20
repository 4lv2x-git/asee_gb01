package com.subsonicfestival.frontend.web;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente de solo lectura del microservicio {@code subsonic-inventory-manager}.
 */
@Service
public class InventarioClient {

    private static final String SERVICIO_NO_DISPONIBLE =
            "No se pudo contactar con el gestor de inventario. Comprueba que subsonic-inventory-manager está en marcha.";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InventarioClient(RestTemplate restTemplate,
                            @Value("${gateway.inventory-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = sinBarraFinal(baseUrl);
    }

    public List<Producto> listar() {
        try {
            Producto[] productos = restTemplate.getForObject(
                    baseUrl + "/inventario/productos", Producto[].class);
            return productos == null ? List.of() : Arrays.asList(productos);
        } catch (RestClientException ex) {
            throw new ServicioExternoException(SERVICIO_NO_DISPONIBLE, ex);
        }
    }

    private static String sinBarraFinal(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public record Producto(
            String id,
            String nombre,
            String descripcion,
            Double precio,
            String tipo,
            Integer stockDisponible) {
    }
}
