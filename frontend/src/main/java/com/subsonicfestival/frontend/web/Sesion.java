package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.dto.TokenResponse;
import com.subsonicfestival.frontend.web.dto.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * Guarda el token y el usuario autenticado en la sesion HTTP del servidor.
 */
@Component
public class Sesion {

    private static final String CLAVE_TOKEN = "subsonic.token";
    private static final String CLAVE_USUARIO = "subsonic.usuario";
    private static final String CLAVE_EXPIRA = "subsonic.expira";

    public void iniciar(HttpSession sesion, TokenResponse token, Usuario usuario) {
        sesion.setAttribute(CLAVE_TOKEN, token.accessToken());
        sesion.setAttribute(CLAVE_USUARIO, usuario);
        sesion.setAttribute(CLAVE_EXPIRA, System.currentTimeMillis() + Math.max(token.expiresIn(), 1) * 1000L);
    }

    public boolean activa(HttpSession sesion) {
        return sesion != null
                && sesion.getAttribute(CLAVE_TOKEN) instanceof String token
                && !token.isBlank()
                && sesion.getAttribute(CLAVE_USUARIO) instanceof Usuario
                && !haCaducado(sesion);
    }

    public String token(HttpSession sesion) {
        return sesion != null && sesion.getAttribute(CLAVE_TOKEN) instanceof String token ? token : null;
    }

    public Usuario usuario(HttpSession sesion) {
        return sesion != null && sesion.getAttribute(CLAVE_USUARIO) instanceof Usuario usuario ? usuario : null;
    }

    public void actualizarUsuario(HttpSession sesion, Usuario usuario) {
        if (sesion != null) {
            sesion.setAttribute(CLAVE_USUARIO, usuario);
        }
    }

    public void cerrar(HttpSession sesion) {
        if (sesion != null) {
            sesion.invalidate();
        }
    }

    private static boolean haCaducado(HttpSession sesion) {
        Object expira = sesion.getAttribute(CLAVE_EXPIRA);
        return expira instanceof Long momento && System.currentTimeMillis() >= momento;
    }
}
