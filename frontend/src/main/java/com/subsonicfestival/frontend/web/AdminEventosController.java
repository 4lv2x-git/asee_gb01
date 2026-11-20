package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.EventosClient.Evento;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Panel de administracion para consultar los festivales publicados por
 * {@code service-festivals}.
 */
@Controller
public class AdminEventosController {

    private static final Set<String> ESTADOS =
            Set.of("publicado", "borrador", "cancelado", "finalizado");
    private static final Collator COLADOR_ES = Collator.getInstance(Locale.forLanguageTag("es"));

    private final EventosClient client;

    public AdminEventosController(EventosClient client) {
        this.client = client;
    }

    @GetMapping("/admin/eventos")
    public String panel(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "all") String estado,
            Model model) {
        List<Evento> eventos = List.of();
        try {
            eventos = client.listar();
        } catch (ServicioExternoException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        String estadoFiltro = ESTADOS.contains(estado) ? estado : "all";
        List<Evento> filtrados = filtrar(eventos, q, estadoFiltro);

        model.addAttribute("eventosFiltrados", filtrados);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("estadoFiltro", estadoFiltro);
        model.addAttribute(
                "filtrado",
                !(q == null || q.isBlank()) || !"all".equals(estadoFiltro));
        model.addAttribute("totalEventos", eventos.size());
        model.addAttribute("publicados", contarEstado(eventos, "publicado"));
        model.addAttribute("borradores", contarEstado(eventos, "borrador"));
        model.addAttribute("cancelados", contarEstado(eventos, "cancelado"));
        model.addAttribute("aforoTotal", eventos.stream()
                .map(Evento::aforo)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum());
        return "admin-eventos";
    }

    private static List<Evento> filtrar(List<Evento> eventos, String q, String estado) {
        String termino = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return eventos.stream()
                .filter(evento -> "all".equals(estado) || estado.equalsIgnoreCase(evento.estado()))
                .filter(evento -> coincideTexto(evento, termino))
                .sorted(Comparator
                        .comparing((Evento evento) -> evento.fechaInicio() == null ? "" : evento.fechaInicio())
                        .thenComparing(evento -> evento.nombre() == null ? "" : evento.nombre(), COLADOR_ES))
                .toList();
    }

    private static boolean coincideTexto(Evento evento, String termino) {
        if (termino.isEmpty()) {
            return true;
        }
        return Stream.of(
                        evento.nombre(),
                        evento.descripcion(),
                        evento.id(),
                        evento.ubicacion() == null ? null : evento.ubicacion().ciudad(),
                        evento.ubicacion() == null ? null : evento.ubicacion().pais())
                .anyMatch(valor -> valor != null && valor.toLowerCase(Locale.ROOT).contains(termino));
    }

    private static long contarEstado(List<Evento> eventos, String estado) {
        return eventos.stream()
                .filter(evento -> estado.equalsIgnoreCase(evento.estado()))
                .count();
    }
}
