package com.subsonicfestival.frontend.artistsblog;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ArtistsBlogApiClient {

    private static final ParameterizedTypeReference<List<ArtistSummary>> ARTIST_LIST =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Song>> SONG_LIST =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<ArticleSummary>> ARTICLE_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public ArtistsBlogApiClient(
            RestClient.Builder builder,
            @Value("${artists-blog.api.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<ArtistSummary> listArtists() {
        return get("/v1/artistas", ARTIST_LIST, false);
    }

    public ArtistDetail getArtist(String artistId) {
        return get("/v1/artistas/{artistId}", ArtistDetail.class, true, artistId);
    }

    public List<Song> getArtistSongs(String artistId) {
        return get("/v1/artistas/{artistId}/canciones", SONG_LIST, false, artistId);
    }

    public List<ArticleSummary> listArticles(int skip, int limit) {
        return get(
                "/v1/blog?skip={skip}&limit={limit}",
                ARTICLE_LIST,
                false,
                skip,
                limit);
    }

    public ArticleDetail getArticle(String articleId) {
        return get("/v1/blog/{articleId}", ArticleDetail.class, true, articleId);
    }

    private <T> T get(
            String path,
            Class<T> responseType,
            boolean missingIsNull,
            Object... uriVariables) {
        try {
            return restClient.get()
                    .uri(path, uriVariables)
                    .retrieve()
                    .body(responseType);
        } catch (HttpClientErrorException.NotFound exception) {
            if (missingIsNull) {
                return null;
            }
            throw new ArtistsBlogApiException(exception);
        } catch (RestClientException exception) {
            throw new ArtistsBlogApiException(exception);
        }
    }

    private <T> T get(
            String path,
            ParameterizedTypeReference<T> responseType,
            boolean missingIsNull,
            Object... uriVariables) {
        try {
            return restClient.get()
                    .uri(path, uriVariables)
                    .retrieve()
                    .body(responseType);
        } catch (HttpClientErrorException.NotFound exception) {
            if (missingIsNull) {
                return null;
            }
            throw new ArtistsBlogApiException(exception);
        } catch (RestClientException exception) {
            throw new ArtistsBlogApiException(exception);
        }
    }

    public record ArtistSummary(String id, String nombre, String imagen) {}

    public record ArtistDetail(
            String id,
            String nombre,
            String imagen,
            String biografia,
            SocialLinks redesSociales,
            List<String> eventosVinculados,
            List<Song> cancionesDestacadas) {

        public ArtistDetail {
            redesSociales =
                    redesSociales == null ? new SocialLinks(null, null, null) : redesSociales;
            eventosVinculados =
                    eventosVinculados == null ? List.of() : List.copyOf(eventosVinculados);
            cancionesDestacadas =
                    cancionesDestacadas == null ? List.of() : List.copyOf(cancionesDestacadas);
        }
    }

    public record SocialLinks(String instagram, String twitter, String web) {}

    public record Song(String titulo, String album, String uriSpotify) {}

    public record ArticleSummary(
            String id,
            String titulo,
            String resumen,
            String imagen,
            String autor,
            String fechaPublicacion) {}

    public record ArticleDetail(
            String id,
            String titulo,
            String resumen,
            String imagen,
            String autor,
            String fechaPublicacion,
            String contenido) {}
}
