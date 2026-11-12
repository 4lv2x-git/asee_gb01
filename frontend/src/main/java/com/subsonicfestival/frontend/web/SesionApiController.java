package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.dto.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Estado de sesion para la portada estatica.
 *
 * <p>La portada ({@code index.html}) se sirve como recurso estatico, asi que no
 * puede resolver la sesion con Thymeleaf. Este endpoint devuelve solo los datos
 * publicos necesarios para mostrar el acceso al perfil en su cabecera. El token
 * nunca se expone.</p>
 */
@RestController
public class SesionApiController {

    private final Sesion sesion;
    private final Textos textos;

    public SesionApiController(Sesion sesion, Textos textos) {
        this.sesion = sesion;
        this.textos = textos;
    }

    @GetMapping("/api/sesion")
    public ResponseEntity<EstadoSesion> estado(HttpServletRequest request) {
        HttpSession sesionHttp = request.getSession(false);
        CacheControl sinCache = CacheControl.noStore();

        if (!sesion.activa(sesionHttp)) {
            return ResponseEntity.ok()
                    .cacheControl(sinCache)
                    .body(new EstadoSesion(false, null, null));
        }

        Usuario usuario = sesion.usuario(sesionHttp);
        return ResponseEntity.ok()
                .cacheControl(sinCache)
                .body(new EstadoSesion(
                        true,
                        usuario == null ? null : usuario.nombre(),
                        usuario == null ? null : textos.iniciales(usuario.nombre())));
    }

    /** Datos publicos de la sesion que necesita la portada. */
    public record EstadoSesion(boolean autenticado, String nombre, String iniciales) {
    }
}
