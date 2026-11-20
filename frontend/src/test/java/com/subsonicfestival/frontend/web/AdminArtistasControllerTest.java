package com.subsonicfestival.frontend.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArtistDetail;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArtistSummary;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.SocialLinks;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.Song;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiException;
import com.subsonicfestival.frontend.web.dto.TokenResponse;
import com.subsonicfestival.frontend.web.dto.Usuario;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminArtistasController.class)
@Import({Sesion.class, Textos.class})
class AdminArtistasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Sesion sesion;

    @MockBean
    private ArtistsBlogApiClient apiClient;

    private MockHttpSession adminSession() {
        MockHttpSession httpSession = new MockHttpSession();
        sesion.iniciar(
                httpSession,
                new TokenResponse("token", "Bearer", 3600),
                new Usuario("admin-1", "Admin", "admin@example.com", "admin", "activo", true,
                        null, null, null));
        return httpSession;
    }

    @Test
    void panelListsArtistsAndHighlightsTheActiveModule() throws Exception {
        when(apiClient.listArtists()).thenReturn(List.of(
                new ArtistSummary("id-zeta", "Zeta", "https://example.com/zeta.jpg"),
                new ArtistSummary("id-alpha", "Alpha", null)));
        when(apiClient.getArtist("id-zeta")).thenReturn(new ArtistDetail(
                "id-zeta",
                "Zeta",
                "https://example.com/zeta.jpg",
                "Biografía de Zeta",
                new SocialLinks("https://instagram.com/zeta", null, null),
                List.of("event-1"),
                List.of(new Song("Canción", null, null))));

        mockMvc.perform(get("/admin/artistas").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-artistas"))
                .andExpect(content().string(containsString("Artistas del cartel")))
                .andExpect(content().string(containsString("Zeta")))
                .andExpect(content().string(containsString("Alpha")))
                .andExpect(content().string(containsString("Biografía de Zeta")))
                .andExpect(content().string(containsString("/artistas/id-zeta")))
                .andExpect(content().string(containsString("/admin/artistas")))
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                .andExpect(content().string(containsString("2 artistas registrados")));
    }

    @Test
    void panelFiltersArtistsOnTheServer() throws Exception {
        when(apiClient.listArtists()).thenReturn(List.of(
                new ArtistSummary("id-zeta", "Zeta", null),
                new ArtistSummary("id-alpha", "Alpha", null)));

        mockMvc.perform(get("/admin/artistas").param("q", "zeta").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Zeta")))
                .andExpect(content().string(not(containsString(">Alpha<"))))
                .andExpect(content().string(containsString("Mostrando 1 de 2 artistas.")));
    }

    @Test
    void panelShowsAnUnavailableStateWhenTheServiceFails() throws Exception {
        when(apiClient.listArtists())
                .thenThrow(new ArtistsBlogApiException(new IllegalStateException("down")));

        mockMvc.perform(get("/admin/artistas").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-artistas"))
                .andExpect(content().string(containsString("No se han podido cargar los artistas.")))
                .andExpect(content().string(containsString("0 artistas registrados")));
    }
}
