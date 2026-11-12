/**
 * Mejoras progresivas de las páginas de cuenta.
 *
 * Muestra u oculta el contenido de los campos de contraseña marcados con
 * `data-password-toggle`. El botón permanece oculto hasta que este script
 * se carga, de modo que sin JavaScript no aparece un control inútil.
 */
(function () {
    "use strict";

    const botones = document.querySelectorAll("[data-password-toggle]");

    botones.forEach(function (boton) {
        const input = document.getElementById(boton.getAttribute("aria-controls"));
        if (!input) {
            return;
        }

        function aplicar(visible) {
            input.type = visible ? "text" : "password";
            boton.setAttribute("aria-pressed", visible ? "true" : "false");
            boton.setAttribute("aria-label", visible ? "Ocultar contraseña" : "Mostrar contraseña");
            boton.textContent = visible ? "Ocultar" : "Mostrar";
        }

        boton.addEventListener("click", function () {
            aplicar(input.type === "password");
        });

        boton.hidden = false;
        aplicar(false);
    });
})();
