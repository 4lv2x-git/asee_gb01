package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.UsuariosAuthClient.DatosPerfil;
import com.subsonicfestival.frontend.web.dto.Usuario;
import com.subsonicfestival.frontend.web.form.PerfilForm;
import com.subsonicfestival.frontend.web.form.VerificacionForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Area personal del usuario autenticado.
 */
@Controller
public class PerfilController {

    private final UsuariosAuthClient client;
    private final Sesion sesion;

    public PerfilController(UsuariosAuthClient client, Sesion sesion) {
        this.client = client;
        this.sesion = sesion;
    }

    @GetMapping("/perfil.html")
    public String legado() {
        return "redirect:/perfil";
    }

    @GetMapping("/perfil")
    public String ver(
            @RequestParam(required = false) String denegado,
            HttpSession sesionHttp,
            Model model) {
        Usuario usuario = sesion.usuario(sesionHttp);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Usuario actualizado = client.perfil(sesion.token(sesionHttp));
            if (actualizado != null) {
                usuario = actualizado;
                sesion.actualizarUsuario(sesionHttp, actualizado);
            }
        } catch (UsuariosAuthException ex) {
            if (ex.esSesionInvalida()) {
                sesion.cerrar(sesionHttp);
                return "redirect:/login?motivo=caducada";
            }
            model.addAttribute("error", ex.getMessage());
        }

        if ("1".equals(denegado)) {
            model.addAttribute(
                    "advertencia",
                    "El panel de administración solo está disponible para cuentas con rol de administrador.");
        }

        rellenarModelo(model, usuario, PerfilForm.desde(usuario), new VerificacionForm());
        return "perfil";
    }

    @PostMapping("/perfil")
    public String guardar(
            @Valid @ModelAttribute("perfilForm") PerfilForm form,
            BindingResult resultado,
            HttpSession sesionHttp,
            Model model,
            RedirectAttributes redireccion) {
        Usuario usuario = sesion.usuario(sesionHttp);
        if (usuario == null) {
            return "redirect:/login";
        }

        boolean proveedor = usuario.esProveedor();
        if (proveedor && textoVacio(form.getTipoServicio())) {
            resultado.rejectValue("tipoServicio", "obligatorio", "Indica el tipo de servicio que ofreces.");
        }
        if (resultado.hasErrors()) {
            rellenarModelo(model, usuario, form, new VerificacionForm());
            return "perfil";
        }

        try {
            Usuario actualizado = client.actualizarPerfil(
                    sesion.token(sesionHttp),
                    new DatosPerfil(
                            form.getNombre().trim(),
                            limpiar(form.getDireccion()),
                            proveedor ? limpiar(form.getTipoServicio()) : null,
                            proveedor ? limpiar(form.getDescripcionServicio()) : null));
            sesion.actualizarUsuario(sesionHttp, actualizado);
            redireccion.addFlashAttribute("exito", "Perfil actualizado correctamente.");
            return "redirect:/perfil";
        } catch (UsuariosAuthException ex) {
            if (ex.esSesionInvalida()) {
                sesion.cerrar(sesionHttp);
                return "redirect:/login?motivo=caducada";
            }
            model.addAttribute("error", ex.getMessage());
            rellenarModelo(model, usuario, form, new VerificacionForm());
            return "perfil";
        }
    }

    @PostMapping("/perfil/verificar")
    public String verificar(
            @Valid @ModelAttribute("verificacionForm") VerificacionForm form,
            BindingResult resultado,
            HttpSession sesionHttp,
            Model model,
            RedirectAttributes redireccion) {
        Usuario usuario = sesion.usuario(sesionHttp);
        if (usuario == null) {
            return "redirect:/login";
        }

        if (resultado.hasErrors()) {
            rellenarModelo(model, usuario, PerfilForm.desde(usuario), form);
            return "perfil";
        }

        try {
            client.verificarCorreo(form.getCodigo().trim());
            Usuario actualizado = client.perfil(sesion.token(sesionHttp));
            if (actualizado != null) {
                sesion.actualizarUsuario(sesionHttp, actualizado);
            }
            redireccion.addFlashAttribute("exito", "Correo verificado correctamente.");
            return "redirect:/perfil";
        } catch (UsuariosAuthException ex) {
            if (ex.esSesionInvalida()) {
                sesion.cerrar(sesionHttp);
                return "redirect:/login?motivo=caducada";
            }
            model.addAttribute("error", ex.getMessage());
            rellenarModelo(model, usuario, PerfilForm.desde(usuario), form);
            return "perfil";
        }
    }

    @GetMapping("/perfil/eliminar")
    public String confirmarEliminacion(HttpSession sesionHttp, Model model) {
        Usuario usuario = sesion.usuario(sesionHttp);
        if (usuario == null) {
            return "redirect:/login";
        }
        prepararConfirmacion(model, usuario);
        return "confirmar-eliminar";
    }

    @PostMapping("/perfil/eliminar")
    public String eliminar(HttpSession sesionHttp, Model model) {
        Usuario usuario = sesion.usuario(sesionHttp);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            client.eliminarPerfil(sesion.token(sesionHttp));
            sesion.cerrar(sesionHttp);
            return "redirect:/login?motivo=eliminado";
        } catch (UsuariosAuthException ex) {
            if (ex.esSesionInvalida()) {
                sesion.cerrar(sesionHttp);
                return "redirect:/login?motivo=caducada";
            }
            model.addAttribute("error", ex.getMessage());
            prepararConfirmacion(model, usuario);
            return "confirmar-eliminar";
        }
    }

    private static void prepararConfirmacion(Model model, Usuario usuario) {
        model.addAttribute("objetivo", usuario);
        model.addAttribute("ambito", "perfil");
        model.addAttribute("accion", "/perfil/eliminar");
        model.addAttribute("volver", "/perfil");
        model.addAttribute("titulo", "Eliminar mi cuenta");
        model.addAttribute(
                "aviso",
                "Esta acción es permanente. Se borrarán tus datos de la plataforma y se cerrarán "
                        + "todas tus sesiones. No podrás recuperar la cuenta.");
        model.addAttribute("etiquetaAccion", "Eliminar cuenta");
    }

    private static void rellenarModelo(
            Model model, Usuario usuario, PerfilForm perfilForm, VerificacionForm verificacionForm) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfilForm", perfilForm);
        model.addAttribute("verificacionForm", verificacionForm);
    }

    private static boolean textoVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String limpiar(String valor) {
        return textoVacio(valor) ? null : valor.trim();
    }
}
