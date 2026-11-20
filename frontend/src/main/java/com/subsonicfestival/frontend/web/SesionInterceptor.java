package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.dto.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protege las paginas privadas y el panel de administracion.
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    private final Sesion sesion;

    public SesionInterceptor(Sesion sesion) {
        this.sesion = sesion;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        HttpSession sesionHttp = request.getSession(false);

        if (!sesion.activa(sesionHttp)) {
            response.sendRedirect(request.getContextPath() + "/login?motivo=caducada");
            return false;
        }

        if (ruta.startsWith("/admin")) {
            Usuario usuario = sesion.usuario(sesionHttp);
            if (usuario == null || !usuario.esAdmin()) {
                response.sendRedirect(request.getContextPath() + "/perfil?denegado=1");
                return false;
            }
        }

        return true;
    }
}
