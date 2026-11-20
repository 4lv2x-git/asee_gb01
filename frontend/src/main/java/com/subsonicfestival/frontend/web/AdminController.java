package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.UsuariosAuthClient.DatosPerfil;
import com.subsonicfestival.frontend.web.dto.Usuario;
import com.subsonicfestival.frontend.web.form.AdminEdicionForm;
import com.subsonicfestival.frontend.web.form.AdminPasswordForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Panel de administracion para la gestion de usuarios.
 */
@Controller
public class AdminController {

    private static final Set<String> ROLES = Set.of("cliente", "proveedor", "admin");
    private static final Set<String> ESTADOS = Set.of("activo", "suspendido");
    private static final List<String> ORDEN_ROLES = List.of("admin", "proveedor", "cliente", "visitante");
    private static final Collator COLADOR_ES = Collator.getInstance(Locale.forLanguageTag("es"));

    private final UsuariosAuthClient client;
    private final Sesion sesion;

    public AdminController(UsuariosAuthClient client, Sesion sesion) {
        this.client = client;
        this.sesion = sesion;
    }

    @GetMapping("/admin.html")
    public String legado() {
        return "redirect:/admin";
    }

    @GetMapping("/admin")
    public String panel(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "all") String rol,
            @RequestParam(required = false, defaultValue = "all") String estado,
            HttpSession sesionHttp,
            Model model) {
        String rolFiltro = ROLES.contains(rol) || "visitante".equals(rol) ? rol : "all";
        String estadoFiltro = ESTADOS.contains(estado) ? estado : "all";

        List<Usuario> usuarios = List.of();
        try {
            List<Usuario> respuesta = client.listarUsuarios(sesion.token(sesionHttp));
            usuarios = respuesta == null ? List.of() : respuesta;
        } catch (UsuariosAuthException ex) {
            if (ex.esSesionInvalida()) {
                sesion.cerrar(sesionHttp);
                return "redirect:/login?motivo=caducada";
            }
            model.addAttribute("error", ex.getMessage());
        }

        List<Usuario> filtrados = filtrar(usuarios, q, rolFiltro, estadoFiltro);

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuariosFiltrados", filtrados);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("rolFiltro", rolFiltro);
        model.addAttribute("estadoFiltro", estadoFiltro);
        model.addAttribute(
                "filtrado", !(q == null || q.isBlank()) || !"all".equals(rolFiltro) || !"all".equals(estadoFiltro));
        model.addAttribute("total", usuarios.size());
        model.addAttribute("clientes", contarRol(usuarios, "cliente"));
        model.addAttribute("proveedores", contarRol(usuarios, "proveedor"));
        model.addAttribute("activos", contarEstado(usuarios, "activo"));
        model.addAttribute("suspendidos", contarEstado(usuarios, "suspendido"));
        return "admin";
    }

    @GetMapping("/admin/{id}/editar")
    public String editar(
            @PathVariable String id,
            HttpSession sesionHttp,
            Model model,
            RedirectAttributes redireccion) {
        try {
            Usuario objetivo = buscarUsuario(id, sesion.token(sesionHttp));
            if (objetivo == null) {
                redireccion.addFlashAttribute("error", "No se ha encontrado el usuario solicitado.");
                return "redirect:/admin";
            }
            model.addAttribute("objetivo", objetivo);
            model.addAttribute("adminEdicionForm", AdminEdicionForm.desde(objetivo));
            return "admin-editar";
        } catch (UsuariosAuthException ex) {
            return gestionarError(ex, sesionHttp, redireccion, "/admin");
        }
    }

    @PostMapping("/admin/{id}/editar")
    public String guardarEdicion(
            @PathVariable String id,
            @Valid @ModelAttribute("adminEdicionForm") AdminEdicionForm form,
            BindingResult resultado,
            HttpSession sesionHttp,
            Model model,
            RedirectAttributes redireccion) {
        Usuario objetivo;
        try {
            objetivo = buscarUsuario(id, sesion.token(sesionHttp));
        } catch (UsuariosAuthException ex) {
            return gestionarError(ex, sesionHttp, redireccion, "/admin");
        }
        if (objetivo == null) {
            redireccion.addFlashAttribute("error", "No se ha encontrado el usuario solicitado.");
            return "redirect:/admin";
        }

        if (objetivo.esProveedor() && textoVacio(form.getTipoServicio())) {
            resultado.rejectValue("tipoServicio", "obligatorio", "Indica el tipo de servicio.");
        }
        if (resultado.hasErrors()) {
            model.addAttribute("objetivo", objetivo);
            return "admin-editar";
        }

        try {
            Usuario actualizado = client.actualizarUsuario(
                    sesion.token(sesionHttp),
                    id,
                    new DatosPerfil(
                            form.getNombre().trim(),
                            limpiar(form.getDireccion()),
                            objetivo.esProveedor() ? limpiar(form.getTipoServicio()) : null,
                            objetivo.esProveedor() ? limpiar(form.getDescripcionServicio()) : null));

            if (Objects.equals(id, idDe(sesion.usuario(sesionHttp)))) {
                sesion.actualizarUsuario(sesionHttp, actualizado);
            }
            redireccion.addFlashAttribute(
                    "exito", "Usuario «" + actualizado.nombre() + "» actualizado correctamente.");
            return "redirect:/admin";
        } catch (UsuariosAuthException ex) {
            if (ex.esSesionInvalida()) {
                sesion.cerrar(sesionHttp);
                return "redirect:/login?motivo=caducada";
            }
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("objetivo", objetivo);
            return "admin-editar";
        }
    }

    @PostMapping("/admin/{id}/estado")
    public String cambiarEstado(
            @PathVariable String id,
            HttpSession sesionHttp,
            RedirectAttributes redireccion) {
        if (Objects.equals(id, idDe(sesion.usuario(sesionHttp)))) {
            redireccion.addFlashAttribute("error", "No puedes suspender ni activar tu propia cuenta.");
            return "redirect:/admin";
        }

        try {
            Usuario objetivo = buscarUsuario(id, sesion.token(sesionHttp));
            if (objetivo == null) {
                redireccion.addFlashAttribute("error", "No se ha encontrado el usuario solicitado.");
                return "redirect:/admin";
            }

            String nuevoEstado = objetivo.estaActivo() ? "suspendido" : "activo";
            client.cambiarEstado(sesion.token(sesionHttp), id, nuevoEstado);
            redireccion.addFlashAttribute(
                    "exito",
                    "Cuenta de " + objetivo.nombre()
                            + ("suspendido".equals(nuevoEstado) ? " suspendida" : " activada")
                            + " correctamente.");
        } catch (UsuariosAuthException ex) {
            return gestionarError(ex, sesionHttp, redireccion, "/admin");
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/{id}/password")
    public String password(
            @PathVariable String id,
            HttpSession sesionHttp,
            Model model,
            RedirectAttributes redireccion) {
        try {
            Usuario objetivo = buscarUsuario(id, sesion.token(sesionHttp));
            if (objetivo == null) {
                redireccion.addFlashAttribute("error", "No se ha encontrado el usuario solicitado.");
                return "redirect:/admin";
            }
            model.addAttribute("objetivo", objetivo);
            model.addAttribute("adminPasswordForm", new AdminPasswordForm());
            return "admin-password";
        } catch (UsuariosAuthException ex) {
            return gestionarError(ex, sesionHttp, redireccion, "/admin");
        }
    }

    @PostMapping("/admin/{id}/password")
    public String cambiarPassword(
            @PathVariable String id,
            @Valid @ModelAttribute("adminPasswordForm") AdminPasswordForm form,
            BindingResult resultado,
            HttpSession sesionHttp,
            Model model,
            RedirectAttributes redireccion) {
        if (form.getNuevaPassword() != null
                && !form.getNuevaPassword().equals(form.getNuevaPasswordConfirm())) {
            resultado.rejectValue(
                    "nuevaPasswordConfirm", "no-coincide", "Las contraseñas no coinciden.");
        }

        Usuario objetivo;
        try {
            objetivo = buscarUsuario(id, sesion.token(sesionHttp));
        } catch (UsuariosAuthException ex) {
            return gestionarError(ex, sesionHttp, redireccion, "/admin");
        }
        if (objetivo == null) {
            redireccion.addFlashAttribute("error", "No se ha encontrado el usuario solicitado.");
            return "redirect:/admin";
        }

        if (resultado.hasErrors()) {
            model.addAttribute("objetivo", objetivo);
            return "admin-password";
        }

        try {
            client.restablecerPasswordDeUsuario(sesion.token(sesionHttp), id, form.getNuevaPassword());
            if (Objects.equals(id, idDe(sesion.usuario(sesionHttp)))) {
                sesion.cerrar(sesionHttp);
                return "redirect:/login?motivo=password";
            }
            redireccion.addFlashAttribute(
                    "exito",
                    "Contraseña de " + objetivo.nombre()
                            + " actualizada. Se han invalidado sus sesiones anteriores.");
            return "redirect:/admin";
        } catch (UsuariosAuthException ex) {
            if (ex.esSesionInvalida()) {
                sesion.cerrar(sesionHttp);
                return "redirect:/login?motivo=caducada";
            }
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("objetivo", objetivo);
            return "admin-password";
        }
    }

    @GetMapping("/admin/{id}/eliminar")
    public String confirmarEliminacion(
            @PathVariable String id,
            HttpSession sesionHttp,
            Model model,
            RedirectAttributes redireccion) {
        if (Objects.equals(id, idDe(sesion.usuario(sesionHttp)))) {
            redireccion.addFlashAttribute("error", "No puedes eliminar tu propia cuenta desde el panel.");
            return "redirect:/admin";
        }

        try {
            Usuario objetivo = buscarUsuario(id, sesion.token(sesionHttp));
            if (objetivo == null) {
                redireccion.addFlashAttribute("error", "No se ha encontrado el usuario solicitado.");
                return "redirect:/admin";
            }
            model.addAttribute("objetivo", objetivo);
            model.addAttribute("ambito", "admin");
            model.addAttribute("accion", "/admin/" + id + "/eliminar");
            model.addAttribute("volver", "/admin");
            model.addAttribute("titulo", "Eliminar usuario");
            model.addAttribute(
                    "aviso",
                    "La cuenta de " + objetivo.nombre() + " (" + objetivo.correo()
                            + ") se eliminará de forma permanente. Esta acción no se puede deshacer.");
            model.addAttribute("etiquetaAccion", "Eliminar definitivamente");
            return "confirmar-eliminar";
        } catch (UsuariosAuthException ex) {
            return gestionarError(ex, sesionHttp, redireccion, "/admin");
        }
    }

    @PostMapping("/admin/{id}/eliminar")
    public String eliminar(
            @PathVariable String id,
            HttpSession sesionHttp,
            RedirectAttributes redireccion) {
        if (Objects.equals(id, idDe(sesion.usuario(sesionHttp)))) {
            redireccion.addFlashAttribute("error", "No puedes eliminar tu propia cuenta desde el panel.");
            return "redirect:/admin";
        }

        try {
            Usuario objetivo = buscarUsuario(id, sesion.token(sesionHttp));
            if (objetivo == null) {
                redireccion.addFlashAttribute("error", "No se ha encontrado el usuario solicitado.");
                return "redirect:/admin";
            }
            client.eliminarUsuario(sesion.token(sesionHttp), id);
            redireccion.addFlashAttribute("exito", "Usuario «" + objetivo.nombre() + "» eliminado correctamente.");
        } catch (UsuariosAuthException ex) {
            return gestionarError(ex, sesionHttp, redireccion, "/admin");
        }
        return "redirect:/admin";
    }

    private Usuario buscarUsuario(String id, String token) {
        List<Usuario> usuarios = client.listarUsuarios(token);
        if (usuarios == null) {
            return null;
        }
        return usuarios.stream().filter(usuario -> Objects.equals(id, usuario.id())).findFirst().orElse(null);
    }

    private static List<Usuario> filtrar(List<Usuario> usuarios, String q, String rol, String estado) {
        String termino = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return usuarios.stream()
                .filter(usuario -> coincideRol(usuario, rol))
                .filter(usuario -> coincideEstado(usuario, estado))
                .filter(usuario -> coincideTexto(usuario, termino))
                .sorted((a, b) -> {
                    int orden = Integer.compare(ordenRol(a.rol()), ordenRol(b.rol()));
                    if (orden != 0) {
                        return orden;
                    }
                    String nombreA = a.nombre() == null ? "" : a.nombre();
                    String nombreB = b.nombre() == null ? "" : b.nombre();
                    return COLADOR_ES.compare(nombreA, nombreB);
                })
                .toList();
    }

    private static boolean coincideRol(Usuario usuario, String rol) {
        if ("all".equals(rol)) {
            return true;
        }
        if ("visitante".equals(rol)) {
            return usuario.rol() == null || !ROLES.contains(usuario.rol());
        }
        return Objects.equals(rol, usuario.rol());
    }

    private static boolean coincideEstado(Usuario usuario, String estado) {
        return "all".equals(estado) || Objects.equals(estado, usuario.estado());
    }

    private static boolean coincideTexto(Usuario usuario, String termino) {
        if (termino.isEmpty()) {
            return true;
        }
        return Stream.of(
                        usuario.nombre(),
                        usuario.correo(),
                        usuario.tipoServicio(),
                        usuario.id())
                .anyMatch(valor -> valor != null && valor.toLowerCase(Locale.ROOT).contains(termino));
    }

    private static int ordenRol(String rol) {
        int indice = ORDEN_ROLES.indexOf(rol == null ? "visitante" : rol);
        return indice < 0 ? ORDEN_ROLES.size() : indice;
    }

    private static long contarRol(List<Usuario> usuarios, String rol) {
        return usuarios.stream().filter(usuario -> Objects.equals(rol, usuario.rol())).count();
    }

    private static long contarEstado(List<Usuario> usuarios, String estado) {
        return usuarios.stream().filter(usuario -> Objects.equals(estado, usuario.estado())).count();
    }

    private static String idDe(Usuario usuario) {
        return usuario == null ? null : usuario.id();
    }

    private String gestionarError(
            UsuariosAuthException ex, HttpSession sesionHttp, RedirectAttributes redireccion, String destino) {
        if (ex.esSesionInvalida()) {
            sesion.cerrar(sesionHttp);
            return "redirect:/login?motivo=caducada";
        }
        redireccion.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + destino;
    }

    private static boolean textoVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String limpiar(String valor) {
        return textoVacio(valor) ? null : valor.trim();
    }
}
