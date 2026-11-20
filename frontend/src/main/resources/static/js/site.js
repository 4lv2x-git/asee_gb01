/**
 * Muestra el acceso al perfil en la cabecera de la portada cuando hay sesión.
 *
 * La portada se sirve como HTML estático, así que el estado de la sesión se
 * consulta al endpoint `/api/sesion`. Si no hay sesión o el frontend no está
 * disponible, el enlace «Acceso» se mantiene apuntando a `/cuenta`, que ya
 * redirige al formulario de acceso o al área privada según corresponda.
 */
(function () {
    "use strict";

    const enlace = document.querySelector("[data-auth-link]");
    if (!enlace || typeof window.fetch !== "function") {
        return;
    }

    window.fetch("/api/sesion", {
        headers: { Accept: "application/json" },
        credentials: "same-origin",
    })
        .then(function (respuesta) {
            return respuesta.ok ? respuesta.json() : null;
        })
        .then(function (estado) {
            if (!estado || !estado.autenticado) {
                return;
            }

            enlace.href = "/perfil";
            enlace.classList.add("nav-user");
            enlace.setAttribute("title", "Ir a mi perfil");
            enlace.setAttribute("aria-label", "Ir al perfil de " + estado.nombre);
            enlace.textContent = "";

            const avatar = document.createElement("span");
            avatar.className = "nav-user__avatar";
            avatar.setAttribute("aria-hidden", "true");
            avatar.textContent = estado.iniciales || "?";

            const nombre = document.createElement("span");
            nombre.className = "nav-user__name";
            nombre.textContent = estado.nombre;

            enlace.append(avatar, nombre);
        })
        .catch(function () {
            // Sin conexión con el servidor se conserva el enlace de acceso.
        });
})();
