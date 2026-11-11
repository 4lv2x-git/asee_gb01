/** Utilidades de interfaz compartidas por las páginas de cuenta. */

export function escapeHtml(value) {
    return String(value === undefined || value === null ? "" : value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

export function showAlert(container, message, type = "info") {
    if (!container) {
        return;
    }
    const icons = {
        error: "!",
        success: "✓",
        warning: "!",
        info: "i",
    };
    container.hidden = false;
    container.className = `alert alert--${type}`;
    container.setAttribute("role", type === "error" ? "alert" : "status");
    container.innerHTML = `
        <span class="alert__icon" aria-hidden="true">${icons[type] || icons.info}</span>
        <p>${escapeHtml(message)}</p>
    `;
}

export function clearAlert(container) {
    if (!container) {
        return;
    }
    container.hidden = true;
    container.className = "alert";
    container.textContent = "";
}

export function setLoading(button, loading, loadingLabel = "Guardando…") {
    if (!button) {
        return;
    }
    if (loading) {
        if (!button.dataset.originalLabel) {
            button.dataset.originalLabel = button.textContent.trim();
        }
        button.disabled = true;
        button.classList.add("is-loading");
        button.textContent = loadingLabel;
    } else {
        button.disabled = false;
        button.classList.remove("is-loading");
        if (button.dataset.originalLabel) {
            button.textContent = button.dataset.originalLabel;
            delete button.dataset.originalLabel;
        }
    }
}

export function setFieldError(form, name, message) {
    if (!form) {
        return;
    }
    const field = form.querySelector(`[name="${CSS.escape(name)}"]`);
    const error = form.querySelector(`[data-error-for="${CSS.escape(name)}"]`);

    if (field) {
        if (message) {
            field.setAttribute("aria-invalid", "true");
        } else {
            field.removeAttribute("aria-invalid");
        }
    }
    if (error) {
        error.textContent = message || "";
        error.hidden = !message;
    }
}

export function clearFormErrors(form) {
    if (!form) {
        return;
    }
    form.querySelectorAll("[data-error-for]").forEach((element) => {
        element.textContent = "";
        element.hidden = true;
    });
    form.querySelectorAll("[aria-invalid='true']").forEach((element) => {
        element.removeAttribute("aria-invalid");
    });
}

export function focusFirstError(form) {
    if (!form) {
        return;
    }
    const invalid = form.querySelector("[aria-invalid='true']");
    if (invalid && typeof invalid.focus === "function") {
        invalid.focus();
    }
}

export function bindPasswordToggles(root = document) {
    root.querySelectorAll("[data-toggle-password]").forEach((button) => {
        if (button.dataset.toggleBound === "true") {
            return;
        }
        button.dataset.toggleBound = "true";
        button.setAttribute("aria-pressed", "false");

        button.addEventListener("click", () => {
            const target = document.getElementById(button.dataset.togglePassword);
            if (!target) {
                return;
            }
            const show = target.type === "password";
            target.type = show ? "text" : "password";
            button.textContent = show ? "Ocultar" : "Ver";
            button.setAttribute("aria-pressed", String(show));
            button.setAttribute(
                "aria-label",
                show ? "Ocultar contraseña" : "Mostrar contraseña"
            );
        });
    });
}

export function initials(name) {
    const clean = String(name || "").trim();
    if (!clean) {
        return "?";
    }
    const parts = clean.split(/\s+/).filter(Boolean);
    if (parts.length === 1) {
        return parts[0].slice(0, 1).toUpperCase();
    }
    return (parts[0].slice(0, 1) + parts[parts.length - 1].slice(0, 1)).toUpperCase();
}

export const ROLE_LABELS = {
    cliente: "Cliente",
    proveedor: "Proveedor",
    admin: "Administrador",
    visitante: "Visitante",
};

export function roleLabel(role) {
    return ROLE_LABELS[role] || role || "Sin rol";
}

export function stateLabel(state) {
    return state === "suspendido" ? "Suspendido" : "Activo";
}

export function formatDate(value) {
    if (!value) {
        return "—";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "—";
    }
    return new Intl.DateTimeFormat("es-ES", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    }).format(date);
}

/**
 * Diálogo de confirmación reutilizable. Resuelve true si el usuario confirma.
 */
export function confirmAction({
    title = "¿Confirmar acción?",
    message = "",
    confirmLabel = "Confirmar",
    cancelLabel = "Cancelar",
    danger = false,
} = {}) {
    return new Promise((resolve) => {
        const dialog = document.createElement("dialog");
        dialog.className = "dialog dialog--confirm";
        dialog.innerHTML = `
            <div class="dialog__head">
                <h2>${escapeHtml(title)}</h2>
                <button type="button" class="dialog__close" data-cancel aria-label="Cerrar">×</button>
            </div>
            <div class="dialog__body">
                <p>${escapeHtml(message)}</p>
            </div>
            <div class="dialog__foot">
                <button type="button" class="btn btn--ghost" data-cancel>${escapeHtml(cancelLabel)}</button>
                <button type="button" class="btn ${danger ? "btn--danger" : "btn--primary"}" data-confirm>
                    ${escapeHtml(confirmLabel)}
                </button>
            </div>
        `;

        let confirmed = false;

        dialog.querySelectorAll("[data-cancel]").forEach((button) => {
            button.addEventListener("click", () => dialog.close());
        });
        dialog.querySelector("[data-confirm]").addEventListener("click", () => {
            confirmed = true;
            dialog.close();
        });
        dialog.addEventListener("click", (event) => {
            if (event.target === dialog) {
                dialog.close();
            }
        });
        dialog.addEventListener(
            "close",
            () => {
                dialog.remove();
                resolve(confirmed);
            },
            { once: true }
        );

        document.body.appendChild(dialog);
        dialog.showModal();
        dialog.querySelector("[data-confirm]").focus();
    });
}

export function params() {
    return new URLSearchParams(window.location.search);
}
