package com.subsonicfestival.frontend.artistsblog;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArticleDetail;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArticleSummary;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArtistDetail;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.ArtistSummary;
import com.subsonicfestival.frontend.artistsblog.ArtistsBlogApiClient.Song;

@Controller
public class ArtistsBlogController {

    private static final int DEFAULT_ARTICLES_PER_PAGE = 10;
    private static final int MAX_ARTICLES_PER_PAGE = 100;

    private final ArtistsBlogApiClient apiClient;

    public ArtistsBlogController(ArtistsBlogApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/artistas")
    public String listArtists(
            @RequestParam(name = "q", defaultValue = "") String query,
            Model model) {
        List<ArtistSummary> artists = apiClient.listArtists();
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        if (!normalizedQuery.isEmpty()) {
            artists = artists.stream()
                    .filter(artist -> artist.nombre() != null
                            && artist.nombre().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                    .toList();
        }
        model.addAttribute("artists", artists);
        model.addAttribute("query", query);
        return "artists-blog/artists";
    }

    @GetMapping("/artistas/{artistId}")
    public String artistDetail(@PathVariable String artistId, Model model) {
        ArtistDetail artist = apiClient.getArtist(artistId);
        if (artist == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista no encontrado");
        }
        List<Song> songs = apiClient.getArtistSongs(artistId);
        model.addAttribute("artist", artist);
        model.addAttribute("songs", songs);
        return "artists-blog/artist-detail";
    }

    @GetMapping("/blog")
    public String listArticles(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = DEFAULT_ARTICLES_PER_PAGE + "") int limit,
            Model model) {
        if (skip < 0 || limit < 1 || limit > MAX_ARTICLES_PER_PAGE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La paginación requiere skip >= 0 y limit entre 1 y 100");
        }

        List<ArticleSummary> articles = apiClient.listArticles(skip, limit);
        model.addAttribute("articles", articles);
        model.addAttribute("skip", skip);
        model.addAttribute("limit", limit);
        model.addAttribute("previousSkip", Math.max(0, skip - limit));
        model.addAttribute("hasPrevious", skip > 0);
        model.addAttribute("hasNext", articles.size() == limit);
        model.addAttribute("nextSkip", skip + limit);
        return "artists-blog/blog";
    }

    @GetMapping("/blog/{articleId}")
    public String articleDetail(@PathVariable String articleId, Model model) {
        ArticleDetail article = apiClient.getArticle(articleId);
        if (article == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artículo no encontrado");
        }
        model.addAttribute("article", article);
        return "artists-blog/article-detail";
    }
}
