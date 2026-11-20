package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.dto.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone datos de la sesion a todas las plantillas sin crear sesiones nuevas.
 */
@ControllerAdvice
public class ModeloGlobal {

    private final Sesion sesion;
    private final Textos textos;

    public ModeloGlobal(Sesion sesion, Textos textos) {
        this.sesion = sesion;
        this.textos = textos;
    }

    @ModelAttribute
    public void usuarioActual(HttpServletRequest request, Model model) {
        HttpSession sesionHttp = request.getSession(false);
        if (!sesion.activa(sesionHttp)) {
            return;
        }
        Usuario usuario = sesion.usuario(sesionHttp);
        model.addAttribute("usuarioActual", usuario);
        model.addAttribute("iniciales", textos.iniciales(usuario == null ? null : usuario.nombre()));
    }
}
