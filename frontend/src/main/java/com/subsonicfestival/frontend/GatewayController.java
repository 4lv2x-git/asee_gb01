package com.subsonicfestival.frontend;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    private final RestTemplate restTemplate;
    private final String eventsUrl;
    private final String artistsUrl;
    private final String authUrl;
    private final String inventoryUrl;
    private final String groundsUrl;

    public GatewayController(RestTemplate restTemplate,
                             @Value("${gateway.events-url}") String eventsUrl,
                             @Value("${gateway.artists-url}") String artistsUrl,
                             @Value("${gateway.auth-url}") String authUrl,
                             @Value("${gateway.inventory-url}") String inventoryUrl,
                             @Value("${gateway.grounds-url}") String groundsUrl) {
        this.restTemplate = restTemplate;
        this.eventsUrl = eventsUrl;
        this.artistsUrl = artistsUrl;
        this.authUrl = authUrl;
        this.inventoryUrl = inventoryUrl;
        this.groundsUrl = groundsUrl;
    }

    @GetMapping("/events")
    public ResponseEntity<?> events(@RequestParam Map<String, String> parameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(eventsUrl + "/eventos");
        parameters.forEach(builder::queryParam);
        return proxy(builder.build().encode().toUriString(), HttpMethod.GET, null, null);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<?> event(@PathVariable("id") String id) {
        return proxy(eventsUrl + "/eventos/" + id, HttpMethod.GET, null, null);
    }

    @GetMapping("/events/{id}/passes")
    public ResponseEntity<?> passes(@PathVariable("id") String id) {
        return proxy(eventsUrl + "/eventos/" + id + "/pases", HttpMethod.GET, null, null);
    }

    @GetMapping("/events/{id}/schedule")
    public ResponseEntity<?> schedule(@PathVariable("id") String id) {
        return proxy(eventsUrl + "/eventos/" + id + "/escaleta", HttpMethod.GET, null, null);
    }

    @GetMapping("/artists")
    public ResponseEntity<?> artists() {
        return proxy(artistsUrl + "/v1/artistas", HttpMethod.GET, null, null);
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<?> artist(@PathVariable("id") String id) {
        return proxy(artistsUrl + "/v1/artistas/" + id, HttpMethod.GET, null, null);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Object body) {
        return proxy(authUrl + "/v1/auth/login", HttpMethod.POST, body, null);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Object body) {
        return proxy(authUrl + "/v1/auth/register", HttpMethod.POST, body, null);
    }

    @GetMapping("/products")
    public ResponseEntity<?> products(@RequestParam Map<String, String> parameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(inventoryUrl + "/inventario/productos");
        parameters.forEach(builder::queryParam);
        return proxy(builder.build().encode().toUriString(), HttpMethod.GET, null, null);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> product(@PathVariable("id") String id) {
        return proxy(inventoryUrl + "/inventario/productos/" + id, HttpMethod.GET, null, null);
    }

    @GetMapping("/grounds")
    public ResponseEntity<?> grounds(@RequestParam Map<String, String> parameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(groundsUrl + "/api/grounds");
        parameters.forEach(builder::queryParam);
        return proxy(builder.build().encode().toUriString(), HttpMethod.GET, null, null);
    }

    @PostMapping("/grounds")
    public ResponseEntity<?> createGround(@RequestBody Object body,
                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                          @RequestHeader(value = "X-User-Id", required = false) String userId,
                                          @RequestHeader(value = "X-User-Role", required = false) String role) {
        return proxy(groundsUrl + "/api/grounds", HttpMethod.POST, body, authorization, userId, role);
    }

    @PutMapping("/grounds/{id}")
    public ResponseEntity<?> updateGround(@PathVariable("id") String id,
                                          @RequestBody Object body,
                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                          @RequestHeader(value = "X-User-Id", required = false) String userId,
                                          @RequestHeader(value = "X-User-Role", required = false) String role) {
        return proxy(groundsUrl + "/api/grounds/" + id, HttpMethod.PUT, body, authorization, userId, role);
    }

    @DeleteMapping("/grounds/{id}")
    public ResponseEntity<?> deleteGround(@PathVariable("id") String id,
                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                          @RequestHeader(value = "X-User-Id", required = false) String userId,
                                          @RequestHeader(value = "X-User-Role", required = false) String role) {
        return proxy(groundsUrl + "/api/grounds/" + id, HttpMethod.DELETE, null, authorization, userId, role);
    }

    @PostMapping("/grounds/{id}/rent")
    public ResponseEntity<?> rentGround(@PathVariable("id") String id,
                                        @RequestBody Object body,
                                        @RequestHeader(value = "Authorization", required = false) String authorization,
                                        @RequestHeader(value = "X-User-Id", required = false) String userId,
                                        @RequestHeader(value = "X-User-Role", required = false) String role) {
        return proxy(groundsUrl + "/api/grounds/" + id + "/alquilar", HttpMethod.POST, body, authorization, userId, role);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> orders(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return proxy(inventoryUrl + "/pedidos", HttpMethod.GET, null, authorization, userId);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Object body,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return proxy(inventoryUrl + "/pedidos", HttpMethod.POST, body, authorization, userId);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> order(@PathVariable("id") String id,
                                   @RequestHeader(value = "Authorization", required = false) String authorization,
                                   @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return proxy(inventoryUrl + "/pedidos/" + id, HttpMethod.GET, null, authorization, userId);
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("id") String id,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return proxy(inventoryUrl + "/pedidos/" + id + "/cancelacion", HttpMethod.POST, null, authorization, userId);
    }

    @GetMapping("/entries/{id}")
    public ResponseEntity<?> entry(@PathVariable("id") String id,
                                   @RequestHeader(value = "Authorization", required = false) String authorization,
                                   @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return proxy(inventoryUrl + "/entradas/" + id, HttpMethod.GET, null, authorization, userId);
    }

    @PostMapping("/entries/{id}/cancel")
    public ResponseEntity<?> cancelEntry(@PathVariable("id") String id,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return proxy(inventoryUrl + "/entradas/" + id + "/cancelacion", HttpMethod.POST, null, authorization, userId);
    }

    private ResponseEntity<?> proxy(String url, HttpMethod method, Object body, String authorization) {
        return proxy(url, method, body, authorization, null);
    }

    private ResponseEntity<?> proxy(String url, HttpMethod method, Object body, String authorization, String userId) {
        return proxy(url, method, body, authorization, userId, null);
    }

    private ResponseEntity<?> proxy(String url, HttpMethod method, Object body, String authorization, String userId, String role) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (authorization != null) {
                headers.set("Authorization", authorization);
            }
            if (userId != null && !userId.isBlank()) {
                headers.set("X-User-Id", userId.trim());
            }
            if (role != null && !role.isBlank()) {
                headers.set("X-User-Role", role.trim());
            }
            HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, method, entity, Object.class);
        } catch (HttpStatusCodeException exception) {
            String responseBody = exception.getResponseBodyAsString();
            if (responseBody == null || responseBody.isBlank()) {
                return ResponseEntity.status(exception.getStatusCode()).body(Map.of(
                        "error", "UPSTREAM_ERROR",
                        "message", "El microservicio respondio con un error",
                        "url", url
                ));
            }
            return ResponseEntity.status(exception.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseBody);
        } catch (RestClientException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "error", "SERVICE_UNAVAILABLE",
                    "message", "El microservicio no esta disponible",
                    "url", url
            ));
        }
    }
}
