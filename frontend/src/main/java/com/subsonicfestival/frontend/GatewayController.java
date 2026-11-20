package com.subsonicfestival.frontend;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    public GatewayController(RestTemplate restTemplate,
                             @Value("${gateway.events-url}") String eventsUrl,
                             @Value("${gateway.artists-url}") String artistsUrl,
                             @Value("${gateway.auth-url}") String authUrl,
                             @Value("${gateway.inventory-url}") String inventoryUrl) {
        this.restTemplate = restTemplate;
        this.eventsUrl = eventsUrl;
        this.artistsUrl = artistsUrl;
        this.authUrl = authUrl;
        this.inventoryUrl = inventoryUrl;
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

    @GetMapping("/orders")
    public ResponseEntity<?> orders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return proxy(inventoryUrl + "/pedidos", HttpMethod.GET, null, authorization);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Object body,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        return proxy(inventoryUrl + "/pedidos", HttpMethod.POST, body, authorization);
    }

    private ResponseEntity<?> proxy(String url, HttpMethod method, Object body, String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (authorization != null) {
                headers.set("Authorization", authorization);
            }
            HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, method, entity, Object.class);
        } catch (RestClientException exception) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "SERVICE_UNAVAILABLE",
                    "message", "El microservicio no esta disponible",
                    "url", url
            ));
        }
    }
}
