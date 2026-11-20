package com.subsonicfestival.frontend.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente de solo lectura del microservicio {@code service-festivals}.
 */
@Service
public class EventosClient {

    private static final String SERVICIO_NO_DISPONIBLE =
            "No se pudo contactar con el servicio de festivales. Comprueba que service-festivals está en marcha.";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public EventosClient(RestTemplate restTemplate,
                         @Value("${gateway.events-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = sinBarraFinal(baseUrl);
    }

    public List<Evento> listar() {
        try {
            PaginaEventos pagina = restTemplate.getForObject(
                    baseUrl + "/eventos?page=1&limit=100", PaginaEventos.class);
            if (pagina == null || pagina.data() == null) {
                return List.of();
            }
            return List.copyOf(pagina.data());
        } catch (RestClientException ex) {
            throw new ServicioExternoException(SERVICIO_NO_DISPONIBLE, ex);
        }
    }

    private static String sinBarraFinal(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public record PaginaEventos(List<Evento> data, Paginacion paginacion) {
    }

    public record Paginacion(Integer page, Integer limit, Integer total, Integer totalPages) {
    }

    public record Evento(
            String id,
            String nombre,
            String descripcion,
            String fechaInicio,
            String fechaFin,
            Ubicacion ubicacion,
            String imagenUrl,
            String estado,
            Integer aforo) {
    }

    public record Ubicacion(String nombre, String direccion, String ciudad, String pais) {
    }
}
