package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArtistDetail;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArtistSummary;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.SocialLinks;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Panel de administracion de artistas, alimentado por el servicio Artists & Blog.
 *
 * <p>Es de solo lectura: el alta, la edicion y la baja de fichas se hacen en el
 * microservicio. Si este no responde, la pagina se muestra con un aviso en vez
 * de caer con un error 500.</p>
 */
@Controller
public class AdminArtistasController {

    private static final String SERVICIO_NO_DISPONIBLE =
            "No se pudo contactar con el servicio de artistas. Comprueba que service-artists-blog está en marcha.";
    private static final Collator COLADOR_ES = Collator.getInstance(Locale.forLanguageTag("es"));

    private final ArtistsBlogApiClient client;

    public AdminArtistasController(ArtistsBlogApiClient client) {
        this.client = client;
    }

    @GetMapping("/admin/artistas")
    public String panel(@RequestParam(required = false, defaultValue = "") String q, Model model) {
        List<Ficha> fichas = List.of();
        try {
            fichas = cargarFichas();
        } catch (ArtistsBlogApiException ex) {
            model.addAttribute("error", SERVICIO_NO_DISPONIBLE);
        }

        String termino = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<Ficha> filtradas = fichas.stream()
                .filter(ficha -> coincide(ficha, termino))
                .sorted(Comparator.comparing(Ficha::nombre, COLADOR_ES))
                .toList();

        model.addAttribute("fichas", filtradas);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("filtrado", !termino.isEmpty());
        model.addAttribute("totalArtistas", fichas.size());
        model.addAttribute("conImagen", fichas.stream().filter(ficha -> !ficha.imagen().isEmpty()).count());
        model.addAttribute("conBiografia", fichas.stream().filter(ficha -> !ficha.biografia().isEmpty()).count());
        model.addAttribute("conEventos", fichas.stream().filter(ficha -> ficha.eventosVinculados() > 0).count());
        model.addAttribute("conCanciones", fichas.stream().filter(ficha -> ficha.canciones() > 0).count());
        return "admin-artistas";
    }

    private List<Ficha> cargarFichas() {
        List<Ficha> fichas = new ArrayList<>();
        List<ArtistSummary> artistas = client.listArtists();
        if (artistas == null) {
            return fichas;
        }
        for (ArtistSummary artista : artistas) {
            // El listado publico solo trae id, nombre e imagen. La ficha completa
            // se consulta aparte; si falla una ficha concreta se conserva el resumen.
            ArtistDetail detalle = null;
            try {
                detalle = client.getArtist(artista.id());
            } catch (ArtistsBlogApiException ex) {
                detalle = null;
            }
            fichas.add(new Ficha(
                    valor(artista.id()),
                    valor(artista.nombre()),
                    urlSegura(artista.imagen()),
                    detalle == null ? "" : valor(detalle.biografia()),
                    detalle == null ? 0 : detalle.eventosVinculados().size(),
                    detalle == null ? 0 : detalle.cancionesDestacadas().size(),
                    enlacesSociales(detalle == null ? null : detalle.redesSociales())));
        }
        return fichas;
    }

    private static boolean coincide(Ficha ficha, String termino) {
        if (termino.isEmpty()) {
            return true;
        }
        return Stream.of(ficha.nombre(), ficha.id(), ficha.biografia())
                .anyMatch(valor -> valor.toLowerCase(Locale.ROOT).contains(termino));
    }

    private static List<Enlace> enlacesSociales(SocialLinks redes) {
        if (redes == null) {
            return List.of();
        }
        List<Enlace> enlaces = new ArrayList<>();
        anadirEnlace(enlaces, "Instagram", redes.instagram());
        anadirEnlace(enlaces, "Twitter", redes.twitter());
        anadirEnlace(enlaces, "Web", redes.web());
        return List.copyOf(enlaces);
    }

    private static void anadirEnlace(List<Enlace> enlaces, String etiqueta, String url) {
        String segura = urlSegura(url);
        if (!segura.isEmpty()) {
            enlaces.add(new Enlace(etiqueta, segura));
        }
    }

    private static String urlSegura(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String recortado = valor.trim();
        return recortado.startsWith("http://") || recortado.startsWith("https://") ? recortado : "";
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }

    public record Ficha(
            String id,
            String nombre,
            String imagen,
            String biografia,
            int eventosVinculados,
            int canciones,
            List<Enlace> enlaces) {
    }

    public record Enlace(String etiqueta, String url) {
    }
}
