package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.dto.TokenResponse;
import com.subsonicfestival.frontend.web.dto.Usuario;
import com.subsonicfestival.frontend.web.form.LoginForm;
import com.subsonicfestival.frontend.web.form.RecuperacionForm;
import com.subsonicfestival.frontend.web.form.RegistroForm;
import com.subsonicfestival.frontend.web.form.RestablecimientoForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Acceso, registro y recuperacion de contrasena.
 */
@Controller
public class AuthController {

    private static final Set<String> VISTAS = Set.of("login", "registro", "recuperar");

    private final UsuariosAuthClient client;
    private final Sesion sesion;

    public AuthController(UsuariosAuthClient client, Sesion sesion) {
        this.client = client;
        this.sesion = sesion;
    }

    @GetMapping({"/login", "/login.html"})
    public String login(
            @RequestParam(required = false) String vista,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) String next,
            HttpServletRequest request,
            HttpSession sesionHttp,
            Model model) {
        LoginForm loginForm = new LoginForm();
        loginForm.setNext(next);
        Object correoPrellenado = request.getAttribute("correoPrellenado");
        if (correoPrellenado instanceof String correo && !correo.isBlank()) {
            loginForm.setCorreo(correo);
        }
        model.addAttribute("loginForm", loginForm);

        if (sesion.activa(sesionHttp)) {
            model.addAttribute("sesionActiva", sesion.usuario(sesionHttp));
        }
        mensajePorMotivo(motivo, model);
        prepararFormularios(model, normalizarVista(vista));
        return "login";
    }

    @PostMapping("/login")
    public String entrar(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult resultado,
            HttpSession sesionHttp,
            Model model) {
        if (resultado.hasErrors()) {
            prepararFormularios(model, "login");
            return "login";
        }

        try {
            TokenResponse token = client.login(form.getCorreo().trim(), form.getPassword());
            Usuario usuario = client.perfil(token.accessToken());
            sesion.iniciar(sesionHttp, token, usuario);
            return "redirect:" + destinoSeguro(form.getNext(), usuario);
        } catch (UsuariosAuthException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararFormularios(model, "login");
            return "login";
        }
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute("registroForm") RegistroForm form,
            BindingResult resultado,
            Model model,
            RedirectAttributes redireccion) {
        if (form.getPassword() != null
                && !form.getPassword().equals(form.getPasswordConfirm())) {
            resultado.rejectValue("passwordConfirm", "no-coincide", "Las contraseñas no coinciden.");
        }
        if ("proveedor".equals(form.getRol()) && textoVacio(form.getTipoServicio())) {
            resultado.rejectValue("tipoServicio", "obligatorio", "Indica el tipo de servicio que ofreces.");
        }
        if (resultado.hasErrors()) {
            prepararFormularios(model, "registro");
            return "login";
        }

        try {
            client.registrar(
                    form.getNombre().trim(),
                    form.getCorreo().trim(),
                    form.getPassword(),
                    form.getRol(),
                    "proveedor".equals(form.getRol()) ? limpiar(form.getTipoServicio()) : null,
                    "proveedor".equals(form.getRol()) ? limpiar(form.getDescripcionServicio()) : null);
            redireccion.addFlashAttribute(
                    "exito",
                    "Cuenta creada correctamente. Ya puedes iniciar sesión. Si el envío de correo está "
                            + "configurado, recibirás un código para verificar tu dirección.");
            redireccion.addFlashAttribute("correoPrellenado", form.getCorreo().trim());
            return "redirect:/login";
        } catch (UsuariosAuthException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararFormularios(model, "registro");
            return "login";
        }
    }

    @PostMapping("/recuperar")
    public String solicitarRecuperacion(
            @Valid @ModelAttribute("recuperacionForm") RecuperacionForm form,
            BindingResult resultado,
            Model model,
            RedirectAttributes redireccion) {
        if (resultado.hasErrors()) {
            prepararFormularios(model, "recuperar");
            return "login";
        }

        try {
            client.recuperarPassword(form.getCorreo().trim());
            redireccion.addFlashAttribute(
                    "exito",
                    "Si el correo está registrado, se ha generado un código de recuperación. "
                            + "Introduce el código recibido para elegir una nueva contraseña.");
            redireccion.addFlashAttribute("correoRecuperacion", form.getCorreo().trim());
            return "redirect:/login?vista=recuperar";
        } catch (UsuariosAuthException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararFormularios(model, "recuperar");
            return "login";
        }
    }

    @PostMapping("/restablecer")
    public String restablecer(
            @Valid @ModelAttribute("restablecimientoForm") RestablecimientoForm form,
            BindingResult resultado,
            Model model,
            RedirectAttributes redireccion) {
        if (form.getNuevaPassword() != null
                && !form.getNuevaPassword().equals(form.getNuevaPasswordConfirm())) {
            resultado.rejectValue(
                    "nuevaPasswordConfirm", "no-coincide", "Las contraseñas no coinciden.");
        }
        if (resultado.hasErrors()) {
            prepararFormularios(model, "recuperar");
            return "login";
        }

        try {
            client.restablecerPassword(form.getCodigo().trim(), form.getNuevaPassword());
            redireccion.addFlashAttribute(
                    "exito", "Contraseña actualizada. Ya puedes iniciar sesión con tus nuevas credenciales.");
            return "redirect:/login";
        } catch (UsuariosAuthException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararFormularios(model, "recuperar");
            return "login";
        }
    }

    @PostMapping("/logout")
    public String salir(HttpSession sesionHttp) {
        String token = sesion.token(sesionHttp);
        if (token != null) {
            try {
                client.logout(token);
            } catch (UsuariosAuthException ignored) {
                // La sesion local se cierra aunque la API no responda.
            }
        }
        sesion.cerrar(sesionHttp);
        return "redirect:/index.html";
    }

    /**
     * Destino del enlace "Acceso" de la pagina principal: lleva al area privada
     * correspondiente si ya hay sesion iniciada o al formulario de acceso si no.
     */
    @GetMapping("/cuenta")
    public String cuenta(HttpServletRequest request) {
        HttpSession sesionHttp = request.getSession(false);
        if (!sesion.activa(sesionHttp)) {
            return "redirect:/login";
        }
        Usuario usuario = sesion.usuario(sesionHttp);
        return "redirect:" + (usuario != null && usuario.esAdmin() ? "/admin" : "/perfil");
    }

    private void prepararFormularios(Model model, String vista) {
        model.addAttribute("vista", vista);
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        if (!model.containsAttribute("registroForm")) {
            model.addAttribute("registroForm", new RegistroForm());
        }
        if (!model.containsAttribute("recuperacionForm")) {
            model.addAttribute("recuperacionForm", new RecuperacionForm());
        }
        if (!model.containsAttribute("restablecimientoForm")) {
            model.addAttribute("restablecimientoForm", new RestablecimientoForm());
        }
    }

    private static String normalizarVista(String vista) {
        return vista != null && VISTAS.contains(vista) ? vista : "login";
    }

    private static String destinoSeguro(String next, Usuario usuario) {
        if (next != null
                && next.startsWith("/")
                && !next.startsWith("//")
                && !next.startsWith("/login")) {
            return next;
        }
        return usuario.esAdmin() ? "/admin" : "/perfil";
    }

    private static void mensajePorMotivo(String motivo, Model model) {
        if (motivo == null) {
            return;
        }
        switch (motivo) {
            case "caducada" -> alerta(
                    model, "warning", "Tu sesión ha caducado. Vuelve a iniciar sesión para continuar.");
            case "suspendido" -> alerta(
                    model,
                    "error",
                    "Tu cuenta está suspendida. Contacta con el equipo de Subsonic Festival si necesitas ayuda.");
            case "eliminado" -> alerta(model, "success", "Tu cuenta se ha eliminado correctamente.");
            case "password" -> alerta(
                    model,
                    "success",
                    "Tu contraseña se ha actualizado. Vuelve a iniciar sesión con tus nuevas credenciales.");
            case "denegado" -> alerta(
                    model,
                    "error",
                    "Necesitas una cuenta de administrador para acceder a esa sección.");
            default -> {
                // Sin mensaje adicional.
            }
        }
    }

    private static void alerta(Model model, String tipo, String texto) {
        model.addAttribute("tipoMensaje", tipo);
        model.addAttribute("mensaje", texto);
    }

    private static boolean textoVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String limpiar(String valor) {
        return textoVacio(valor) ? null : valor.trim();
    }
}
