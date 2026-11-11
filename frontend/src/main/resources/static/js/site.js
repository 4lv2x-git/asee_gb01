/**
 * Actualiza el enlace de acceso de la página principal según la sesión local.
 * La validación real del token se hace en las páginas de cuenta contra la API.
 */
(function () {
    "use strict";

    const link = document.querySelector("[data-auth-link]");
    if (!link) {
        return;
    }

    try {
        const raw = window.localStorage.getItem("subsonic.session");
        if (!raw) {
            return;
        }

        const session = JSON.parse(raw);
        if (!session || !session.accessToken) {
            return;
        }
        if (typeof session.expiresAt === "number" && Date.now() >= session.expiresAt) {
            window.localStorage.removeItem("subsonic.session");
            return;
        }

        const isAdmin = session.user && session.user.rol === "admin";
        link.href = isAdmin ? "admin.html" : "perfil.html";
        link.textContent = isAdmin ? "Panel admin" : "Mi perfil";
    } catch (error) {
        // localStorage puede no estar disponible; se mantiene el enlace de acceso.
    }
})();
