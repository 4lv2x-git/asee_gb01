package com.subsonicfestival.frontend.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subsonicfestival.frontend.web.dto.TokenResponse;
import com.subsonicfestival.frontend.web.dto.Usuario;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente Java del microservicio {@code subsonic-users-auth}.
 *
 * <p>Todas las llamadas se hacen desde el servidor; el navegador nunca habla
 * directamente con la API de usuarios.</p>
 */
@Service
public class UsuariosAuthClient {

    private static final String SERVICIO_NO_DISPONIBLE =
            "No se pudo contactar con el servicio de usuarios. Comprueba que subsonic-users-auth está en marcha.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public UsuariosAuthClient(
            @Value("${subsonic.users-auth.url:http://localhost:8000}") String baseUrl,
            ObjectMapper objectMapper) {
        // Se fuerza HTTP/1.1: el intento de upgrade h2c de java.net.http envía el
        // cuerpo en chunked y uvicorn (FastAPI) lo rechaza con un 422.
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(sinBarraFinal(baseUrl))
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
        this.objectMapper = objectMapper;
    }

    public TokenResponse login(String correo, String password) {
        return ejecutar(() -> restClient.post()
                .uri("/v1/auth/login")
                .body(Map.of("correo", correo, "password", password))
                .retrieve()
                .body(TokenResponse.class));
    }

    public void logout(String token) {
        ejecutar(() -> {
            restClient.post()
                    .uri("/v1/auth/logout")
                    .header(HttpHeaders.AUTHORIZATION, portador(token))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    public Usuario registrar(
            String nombre,
            String correo,
            String password,
            String rol,
            String tipoServicio,
            String descripcionServicio) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("correo", correo);
        cuerpo.put("password", password);
        cuerpo.put("rol", rol);
        cuerpo.put("tipoServicio", tipoServicio);
        cuerpo.put("descripcionServicio", descripcionServicio);
        return ejecutar(() -> restClient.post()
                .uri("/v1/auth/register")
                .body(cuerpo)
                .retrieve()
                .body(Usuario.class));
    }

    public void recuperarPassword(String correo) {
        ejecutar(() -> {
            restClient.post()
                    .uri("/v1/auth/forgot-password")
                    .body(Map.of("correo", correo))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    public void restablecerPassword(String codigo, String nuevaPassword) {
        ejecutar(() -> {
            restClient.post()
                    .uri("/v1/auth/reset-password")
                    .body(Map.of("codigo", codigo, "nuevaPassword", nuevaPassword))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    public Usuario verificarCorreo(String codigo) {
        return ejecutar(() -> restClient.post()
                .uri("/v1/auth/verify-email")
                .body(Map.of("codigo", codigo))
                .retrieve()
                .body(Usuario.class));
    }

    public Usuario perfil(String token) {
        return ejecutar(() -> restClient.get()
                .uri("/v1/usuarios/perfil")
                .header(HttpHeaders.AUTHORIZATION, portador(token))
                .retrieve()
                .body(Usuario.class));
    }

    public Usuario actualizarPerfil(String token, DatosPerfil datos) {
        return ejecutar(() -> restClient.patch()
                .uri("/v1/usuarios/perfil")
                .header(HttpHeaders.AUTHORIZATION, portador(token))
                .body(cuerpoPerfil(datos))
                .retrieve()
                .body(Usuario.class));
    }

    public void eliminarPerfil(String token) {
        ejecutar(() -> {
            restClient.delete()
                    .uri("/v1/usuarios/perfil")
                    .header(HttpHeaders.AUTHORIZATION, portador(token))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    public List<Usuario> listarUsuarios(String token) {
        return ejecutar(() -> restClient.get()
                .uri("/v1/usuarios/admin")
                .header(HttpHeaders.AUTHORIZATION, portador(token))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Usuario>>() {
                }));
    }

    public Usuario actualizarUsuario(String token, String id, DatosPerfil datos) {
        return ejecutar(() -> restClient.put()
                .uri("/v1/usuarios/admin/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, portador(token))
                .body(cuerpoPerfil(datos))
                .retrieve()
                .body(Usuario.class));
    }

    public Usuario cambiarEstado(String token, String id, String estado) {
        return ejecutar(() -> restClient.patch()
                .uri("/v1/usuarios/admin/{id}/estado", id)
                .header(HttpHeaders.AUTHORIZATION, portador(token))
                .body(Map.of("estado", estado))
                .retrieve()
                .body(Usuario.class));
    }

    public void restablecerPasswordDeUsuario(String token, String id, String nuevaPassword) {
        ejecutar(() -> {
            restClient.put()
                    .uri("/v1/usuarios/admin-password-reset/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, portador(token))
                    .body(Map.of("nuevaPassword", nuevaPassword))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    public void eliminarUsuario(String token, String id) {
        ejecutar(() -> {
            restClient.delete()
                    .uri("/v1/usuarios/admin/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, portador(token))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    private <T> T ejecutar(Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (RestClientResponseException ex) {
            throw new UsuariosAuthException(ex.getStatusCode().value(), mensajeDeError(ex), ex);
        } catch (ResourceAccessException ex) {
            throw new UsuariosAuthException(502, SERVICIO_NO_DISPONIBLE, ex);
        }
    }

    private String mensajeDeError(RestClientResponseException ex) {
        String cuerpo = ex.getResponseBodyAsString();
        if (cuerpo != null && !cuerpo.isBlank()) {
            try {
                JsonNode raiz = objectMapper.readTree(cuerpo);
                if (raiz.hasNonNull("message")) {
                    return raiz.get("message").asText();
                }
                JsonNode detalle = raiz.get("detail");
                if (detalle != null) {
                    if (detalle.isTextual()) {
                        return detalle.asText();
                    }
                    if (detalle.isArray() && !detalle.isEmpty()) {
                        JsonNode primero = detalle.get(0);
                        return mensajeDeValidacion(campoDe(primero), primero.path("msg").asText());
                    }
                }
            } catch (JsonProcessingException ignored) {
                // Se usa el mensaje generico segun el codigo de estado.
            }
        }
        return switch (ex.getStatusCode().value()) {
            case 400 -> "Los datos enviados no son válidos.";
            case 401 -> "Credenciales inválidas o sesión caducada.";
            case 403 -> "No tienes permisos para realizar esta acción.";
            case 404 -> "No se ha encontrado el recurso solicitado.";
            case 409 -> "Ya existe una cuenta con ese correo electrónico.";
            case 422 -> "Revisa los datos del formulario.";
            default -> "Ha ocurrido un error inesperado. Inténtalo de nuevo.";
        };
    }

    private static String campoDe(JsonNode error) {
        JsonNode ubicacion = error.get("loc");
        String campo = "";
        if (ubicacion != null && ubicacion.isArray()) {
            for (JsonNode parte : ubicacion) {
                if (parte.isTextual() && !"body".equals(parte.asText())) {
                    campo = parte.asText();
                }
            }
        }
        return campo;
    }

    private static String mensajeDeValidacion(String campo, String mensajeTecnico) {
        String tecnico = mensajeTecnico == null ? "" : mensajeTecnico.toLowerCase();
        if (tecnico.contains("valid email")) {
            return "Introduce un correo electrónico válido.";
        }
        if (tecnico.contains("at least 8 characters")) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (tecnico.contains("at least 16 characters")) {
            return "El código no es válido.";
        }
        if (tecnico.contains("field required")) {
            return "Hay campos obligatorios sin completar.";
        }
        return switch (campo) {
            case "correo" -> "Introduce un correo electrónico válido.";
            case "password", "nuevaPassword" -> "La contraseña debe tener al menos 8 caracteres.";
            case "codigo" -> "El código no es válido o ha caducado.";
            case "nombre" -> "El nombre es obligatorio.";
            case "tipoServicio" -> "Indica el tipo de servicio.";
            default -> "Revisa los datos del formulario.";
        };
    }

    private static String portador(String token) {
        return "Bearer " + token;
    }

    private static Map<String, Object> cuerpoPerfil(DatosPerfil datos) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", datos.nombre());
        cuerpo.put("direccion", datos.direccion());
        cuerpo.put("tipoServicio", datos.tipoServicio());
        cuerpo.put("descripcionServicio", datos.descripcionServicio());
        return cuerpo;
    }

    private static String sinBarraFinal(String valor) {
        String resultado = valor == null || valor.isBlank() ? "http://localhost:8000" : valor.trim();
        while (resultado.endsWith("/")) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado;
    }

    /** Datos editables de un usuario (perfil propio o edicion desde administracion). */
    public record DatosPerfil(
            String nombre,
            String direccion,
            String tipoServicio,
            String descripcionServicio) {
    }
}
