package com.subsonicfestival.frontend;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxy same-origin hacia el microservicio {@code subsonic-users-auth}.
 *
 * <p>El frontend consume la API bajo {@code /api/users-auth/**}; este controlador
 * reenvía la petición al servicio de usuarios sin necesidad de configurar CORS
 * en el microservicio. La URL de destino se configura con la propiedad
 * {@code subsonic.users-auth.url} o la variable de entorno
 * {@code USERS_AUTH_API_URL}.</p>
 */
@RestController
public class UsersAuthProxyController {

    private static final String PREFIX = "/api/users-auth";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final String baseUrl;

    public UsersAuthProxyController(
            @Value("${subsonic.users-auth.url:http://localhost:8000}") String baseUrl) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @RequestMapping(PREFIX + "/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        path = path.substring(PREFIX.length());
        if (path.isBlank()) {
            path = "/";
        }
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            path += "?" + query;
        }

        HttpRequest.Builder builder;
        try {
            builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(REQUEST_TIMEOUT);
        } catch (IllegalArgumentException ex) {
            return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Ruta de API no valida");
        }

        byte[] body;
        try {
            body = request.getInputStream().readAllBytes();
        } catch (IOException ex) {
            return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "No se pudo leer la peticion");
        }

        HttpRequest.BodyPublisher publisher = body.length == 0
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(body);

        builder.method(request.getMethod().toUpperCase(Locale.ROOT), publisher);
        copyHeader(request, builder, HttpHeaders.AUTHORIZATION);
        copyHeader(request, builder, HttpHeaders.CONTENT_TYPE);
        copyHeader(request, builder, HttpHeaders.ACCEPT);

        try {
            HttpResponse<byte[]> response = httpClient.send(
                    builder.build(), HttpResponse.BodyHandlers.ofByteArray());

            HttpHeaders headers = new HttpHeaders();
            response.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                    .ifPresent(value -> headers.set(HttpHeaders.CONTENT_TYPE, value));
            response.headers().firstValue(HttpHeaders.WWW_AUTHENTICATE)
                    .ifPresent(value -> headers.set(HttpHeaders.WWW_AUTHENTICATE, value));

            return new ResponseEntity<>(
                    response.body(), headers, HttpStatusCode.valueOf(response.statusCode()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return error(HttpStatus.BAD_GATEWAY, "BAD_GATEWAY",
                    "La peticion al servicio de usuarios fue interrumpida");
        } catch (IOException | IllegalArgumentException ex) {
            return error(HttpStatus.BAD_GATEWAY, "BAD_GATEWAY",
                    "No se pudo contactar con el servicio de usuarios (subsonic-users-auth)");
        }
    }

    private static void copyHeader(HttpServletRequest request, HttpRequest.Builder builder, String header) {
        String value = request.getHeader(header);
        if (value != null && !value.isBlank()) {
            builder.setHeader(header, value);
        }
    }

    private static ResponseEntity<byte[]> error(HttpStatus status, String code, String message) {
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        String json = "{\"code\":\"" + code + "\",\"message\":\"" + escaped + "\",\"details\":{}}";
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String stripTrailingSlash(String value) {
        String result = value == null || value.isBlank() ? "http://localhost:8000" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
