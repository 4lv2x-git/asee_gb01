package com.subsonicfestival.frontend.artistsblog;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArticleDetail;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArticleSummary;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArtistDetail;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArtistSummary;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.SocialLinks;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.Song;

@WebMvcTest(ArtistsBlogController.class)
class ArtistsBlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArtistsBlogApiClient apiClient;

    @Test
    void artistCatalogRendersLinksToArtistDetails() throws Exception {
        when(apiClient.listArtists())
                .thenReturn(List.of(new ArtistSummary("artist-1", "Nova Kills", null)));

        mockMvc.perform(get("/artistas"))
                .andExpect(status().isOk())
                .andExpect(view().name("artists-blog/artists"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nova Kills")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/artistas/artist-1")));
    }

    @Test
    void artistDetailRendersBiographySongsAndEvents() throws Exception {
        when(apiClient.getArtist("artist-1"))
                .thenReturn(new ArtistDetail(
                        "artist-1",
                        "Nova Kills",
                        null,
                        "Biografía de prueba",
                        new SocialLinks(null, null, null),
                        List.of("event-1"),
                        List.of()));
        when(apiClient.getArtistSongs("artist-1"))
                .thenReturn(List.of(new Song("Primer sencillo", "Álbum", null)));

        mockMvc.perform(get("/artistas/artist-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("artists-blog/artist-detail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Biografía de prueba")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Primer sencillo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("event-1")));
    }

    @Test
    void blogCatalogRendersArticlesAndPagination() throws Exception {
        when(apiClient.listArticles(0, 10))
                .thenReturn(List.of(new ArticleSummary(
                        "article-1",
                        "Novedades del festival",
                        "Resumen",
                        null,
                        "Redacción",
                        "2026-01-01T10:00:00Z")));

        mockMvc.perform(get("/blog").param("skip", "0").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("artists-blog/blog"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Novedades del festival")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/blog/article-1")));
    }

    @Test
    void articleDetailRendersFullContent() throws Exception {
        when(apiClient.getArticle("article-1"))
                .thenReturn(new ArticleDetail(
                        "article-1",
                        "Novedades del festival",
                        "Resumen",
                        null,
                        "Redacción",
                        "2026-01-01T10:00:00Z",
                        "Contenido completo"));

        mockMvc.perform(get("/blog/article-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("artists-blog/article-detail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Contenido completo")));
    }

    @Test
    void blogRejectsPaginationOutsideTheOpenApiLimits() throws Exception {
        mockMvc.perform(get("/blog").param("skip", "0").param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("artists-blog/request-error"));
    }

    @Test
    void artistSearchFiltersTheCatalogByName() throws Exception {
        when(apiClient.listArtists())
                .thenReturn(List.of(
                        new ArtistSummary("artist-1", "Nova Kills", null),
                        new ArtistSummary("artist-2", "Lunar Tide", null)));

        mockMvc.perform(get("/artistas").param("q", "nova"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nova Kills")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Lunar Tide"))));
    }

    @Test
    void missingArtistReturnsNotFound() throws Exception {
        when(apiClient.getArtist("missing")).thenReturn(null);

        mockMvc.perform(get("/artistas/missing"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("artists-blog/request-error"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Artista no encontrado")));
        verify(apiClient).getArtist("missing");
    }
}
