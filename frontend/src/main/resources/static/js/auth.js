import { api, dashboardFor, goToNext, session, ApiError } from "./api.js";
import {
    bindPasswordToggles,
    clearAlert,
    clearFormErrors,
    escapeHtml,
    focusFirstError,
    params,
    setFieldError,
    setLoading,
    showAlert,
} from "./ui.js";

const alertBox = document.getElementById("auth-alert");
const activeSessionBox = document.getElementById("active-session");
const tabs = [...document.querySelectorAll("[data-tab]")];
const panels = {
    login: document.getElementById("panel-login"),
    register: document.getElementById("panel-register"),
    recovery: document.getElementById("panel-recovery"),
};

const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
const forgotForm = document.getElementById("forgot-form");
const resetForm = document.getElementById("reset-form");
const providerFields = document.getElementById("provider-fields");
const roleRadios = [...document.querySelectorAll("input[name='rol']")];

const query = params();
const next = query.get("next");

function showPanel(name) {
    tabs.forEach((tab) => {
        const active = tab.dataset.tab === name;
        tab.setAttribute("aria-selected", String(active));
        tab.tabIndex = active ? 0 : -1;
        tab.classList.toggle("is-active", active);
    });
    Object.entries(panels).forEach(([key, panel]) => {
        if (panel) {
            panel.hidden = key !== name;
        }
    });
}

function isEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function validateEmail(form, value, name = "correo") {
    if (!value) {
        setFieldError(form, name, "Introduce tu correo electrónico.");
        return false;
    }
    if (!isEmail(value)) {
        setFieldError(form, name, "Introduce un correo electrónico válido.");
        return false;
    }
    return true;
}

function validatePassword(form, value, name = "password", label = "La contraseña") {
    if (!value) {
        setFieldError(form, name, "Introduce una contraseña.");
        return false;
    }
    if (value.length < 8) {
        setFieldError(form, name, `${label} debe tener al menos 8 caracteres.`);
        return false;
    }
    return true;
}

function updateRoleUi() {
    const role = roleRadios.find((radio) => radio.checked)?.value || "cliente";
    const isProvider = role === "proveedor";

    if (providerFields) {
        providerFields.hidden = !isProvider;
        const service = providerFields.querySelector("[name='tipoServicio']");
        if (service) {
            service.required = isProvider;
            service.setAttribute("aria-required", String(isProvider));
        }
    }

    document.querySelectorAll(".role-card").forEach((card) => {
        const input = card.querySelector("input[name='rol']");
        card.classList.toggle("is-selected", Boolean(input && input.checked));
    });
}

function renderActiveSession() {
    const current = session.get();
    if (!current || session.isExpired()) {
        if (current) {
            session.clear();
        }
        if (activeSessionBox) {
            activeSessionBox.hidden = true;
        }
        return;
    }

    const user = current.user;
    if (!activeSessionBox) {
        return;
    }

    activeSessionBox.hidden = false;
    activeSessionBox.innerHTML = `
        <div class="active-session__copy">
            <span class="active-session__dot" aria-hidden="true"></span>
            <p>
                Sesión iniciada${user ? ` como <strong>${escapeHtml(user.nombre)}</strong>` : ""}.
                Puedes continuar o entrar con otra cuenta.
            </p>
        </div>
        <div class="active-session__actions">
            <a class="btn btn--ghost btn--small" href="${dashboardFor(user)}">Continuar</a>
            <button class="btn btn--ghost btn--small" type="button" id="active-session-logout">Cerrar sesión</button>
        </div>
    `;

    document.getElementById("active-session-logout")?.addEventListener("click", async (event) => {
        const button = event.currentTarget;
        setLoading(button, true, "Cerrando…");
        try {
            await api("/v1/auth/logout", { method: "POST" });
        } catch (error) {
            // Aunque el token ya no sea válido, se limpia la sesión local.
        }
        session.clear();
        activeSessionBox.hidden = true;
        showAlert(alertBox, "Has cerrado la sesión.", "info");
    });
}

function renderReasonAlert() {
    const reason = query.get("reason");
    const messages = {
        expired: ["Tu sesión ha caducado. Vuelve a iniciar sesión para continuar.", "warning"],
        suspended: [
            "Tu cuenta está suspendida. Contacta con el equipo de Subsonic Festival si necesitas ayuda.",
            "error",
        ],
        deleted: ["Tu cuenta se ha eliminado correctamente.", "success"],
        logout: ["Has cerrado sesión.", "info"],
        denied: ["Necesitas una cuenta de administrador para acceder a esa sección.", "error"],
        password: ["Tu contraseña se ha actualizado. Vuelve a iniciar sesión con tus nuevas credenciales.", "success"],
    };
    if (messages[reason]) {
        showAlert(alertBox, messages[reason][0], messages[reason][1]);
    }
}

async function login(event) {
    event.preventDefault();
    clearFormErrors(loginForm);
    clearAlert(alertBox);

    const correo = loginForm.elements.correo.value.trim();
    const password = loginForm.elements.password.value;

    const emailOk = validateEmail(loginForm, correo);
    let passwordOk = true;
    if (!password) {
        setFieldError(loginForm, "password", "Introduce tu contraseña.");
        passwordOk = false;
    }
    if (!emailOk || !passwordOk) {
        focusFirstError(loginForm);
        return;
    }

    const submit = document.getElementById("login-submit");
    setLoading(submit, true, "Entrando…");

    try {
        const tokenResponse = await api("/v1/auth/login", {
            method: "POST",
            auth: false,
            body: { correo, password },
        });

        session.save({
            accessToken: tokenResponse.accessToken,
            tokenType: tokenResponse.tokenType,
            expiresIn: tokenResponse.expiresIn,
        });

        let user = null;
        try {
            user = await api("/v1/usuarios/perfil");
            session.updateUser(user);
        } catch (error) {
            session.clear();
            throw error;
        }

        showAlert(alertBox, `Bienvenido de nuevo${user && user.nombre ? `, ${user.nombre}` : ""}.`, "success");
        window.setTimeout(() => goToNext(next, dashboardFor(user)), 350);
    } catch (error) {
        showAlert(
            alertBox,
            error instanceof ApiError ? error.message : "No se pudo iniciar sesión.",
            "error"
        );
        setLoading(submit, false);
    }
}

async function register(event) {
    event.preventDefault();
    clearFormErrors(registerForm);
    clearAlert(alertBox);

    const nombre = registerForm.elements.nombre.value.trim();
    const correo = registerForm.elements.correo.value.trim();
    const password = registerForm.elements.password.value;
    const confirmPassword = registerForm.elements.passwordConfirm.value;
    const rol = registerForm.elements.rol.value;
    const tipoServicio = registerForm.elements.tipoServicio.value.trim();
    const descripcionServicio = registerForm.elements.descripcionServicio.value.trim();

    let valid = true;
    if (!nombre) {
        setFieldError(registerForm, "nombre", "Introduce tu nombre.");
        valid = false;
    }
    valid = validateEmail(registerForm, correo) && valid;
    valid = validatePassword(registerForm, password) && valid;
    if (!confirmPassword) {
        setFieldError(registerForm, "passwordConfirm", "Repite la contraseña.");
        valid = false;
    } else if (password !== confirmPassword) {
        setFieldError(registerForm, "passwordConfirm", "Las contraseñas no coinciden.");
        valid = false;
    }
    if (rol === "proveedor" && !tipoServicio) {
        setFieldError(registerForm, "tipoServicio", "Indica el tipo de servicio que ofreces.");
        valid = false;
    }
    if (!valid) {
        focusFirstError(registerForm);
        return;
    }

    const submit = document.getElementById("register-submit");
    setLoading(submit, true, "Creando cuenta…");

    const body = { nombre, correo, password, rol };
    if (rol === "proveedor") {
        body.tipoServicio = tipoServicio;
        if (descripcionServicio) {
            body.descripcionServicio = descripcionServicio;
        }
    }

    try {
        await api("/v1/auth/register", { method: "POST", auth: false, body });
        registerForm.reset();
        updateRoleUi();
        setLoading(submit, false);
        showPanel("login");
        loginForm.elements.correo.value = correo;
        loginForm.elements.password.focus();
        showAlert(
            alertBox,
            "Cuenta creada correctamente. Ya puedes iniciar sesión. Si el envío de correo está configurado, recibirás un código para verificar tu dirección.",
            "success"
        );
    } catch (error) {
        showAlert(
            alertBox,
            error instanceof ApiError ? error.message : "No se pudo crear la cuenta.",
            "error"
        );
        setLoading(submit, false);
    }
}

async function requestPasswordReset(event) {
    event.preventDefault();
    clearFormErrors(forgotForm);
    clearAlert(alertBox);

    const correo = forgotForm.elements.correo.value.trim();
    if (!validateEmail(forgotForm, correo)) {
        focusFirstError(forgotForm);
        return;
    }

    const submit = document.getElementById("forgot-submit");
    setLoading(submit, true, "Enviando…");

    try {
        await api("/v1/auth/forgot-password", {
            method: "POST",
            auth: false,
            body: { correo },
        });
        showAlert(
            alertBox,
            "Si el correo está registrado, se habrá generado un código de recuperación. Introduce el código recibido para elegir una nueva contraseña.",
            "success"
        );
        resetForm.elements.codigo.focus();
    } catch (error) {
        showAlert(
            alertBox,
            error instanceof ApiError ? error.message : "No se pudo solicitar la recuperación.",
            "error"
        );
    } finally {
        setLoading(submit, false);
    }
}

async function resetPassword(event) {
    event.preventDefault();
    clearFormErrors(resetForm);
    clearAlert(alertBox);

    const codigo = resetForm.elements.codigo.value.trim();
    const nuevaPassword = resetForm.elements.nuevaPassword.value;
    const confirmPassword = resetForm.elements.nuevaPasswordConfirm.value;

    let valid = true;
    if (!codigo) {
        setFieldError(resetForm, "codigo", "Introduce el código de recuperación.");
        valid = false;
    } else if (codigo.length < 16) {
        setFieldError(resetForm, "codigo", "El código no es válido.");
        valid = false;
    }
    valid = validatePassword(resetForm, nuevaPassword, "nuevaPassword", "La nueva contraseña") && valid;
    if (!confirmPassword) {
        setFieldError(resetForm, "nuevaPasswordConfirm", "Repite la nueva contraseña.");
        valid = false;
    } else if (nuevaPassword !== confirmPassword) {
        setFieldError(resetForm, "nuevaPasswordConfirm", "Las contraseñas no coinciden.");
        valid = false;
    }
    if (!valid) {
        focusFirstError(resetForm);
        return;
    }

    const submit = document.getElementById("reset-submit");
    setLoading(submit, true, "Cambiando…");

    try {
        await api("/v1/auth/reset-password", {
            method: "POST",
            auth: false,
            body: { codigo, nuevaPassword },
        });
        resetForm.reset();
        setLoading(submit, false);
        session.clear();
        renderActiveSession();
        showPanel("login");
        showAlert(
            alertBox,
            "Contraseña actualizada. Ya puedes iniciar sesión con tus nuevas credenciales.",
            "success"
        );
        loginForm.elements.correo.focus();
    } catch (error) {
        showAlert(
            alertBox,
            error instanceof ApiError ? error.message : "No se pudo cambiar la contraseña.",
            "error"
        );
        setLoading(submit, false);
    }
}

tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
        clearAlert(alertBox);
        showPanel(tab.dataset.tab);
    });
    tab.addEventListener("keydown", (event) => {
        if (event.key !== "ArrowLeft" && event.key !== "ArrowRight") {
            return;
        }
        event.preventDefault();
        const index = tabs.indexOf(tab);
        const direction = event.key === "ArrowRight" ? 1 : -1;
        const target = tabs[(index + direction + tabs.length) % tabs.length];
        target.focus();
        target.click();
    });
});

document.querySelectorAll("[data-tab-target]").forEach((button) => {
    button.addEventListener("click", () => {
        clearAlert(alertBox);
        showPanel(button.dataset.tabTarget);
    });
});

document.querySelectorAll("[data-open-recovery]").forEach((button) => {
    button.addEventListener("click", () => {
        clearAlert(alertBox);
        showPanel("recovery");
        forgotForm.elements.correo.focus();
    });
});

document.querySelectorAll("[data-back-to-login]").forEach((button) => {
    button.addEventListener("click", () => {
        clearAlert(alertBox);
        showPanel("login");
    });
});

roleRadios.forEach((radio) => radio.addEventListener("change", updateRoleUi));

loginForm.addEventListener("submit", login);
registerForm.addEventListener("submit", register);
forgotForm.addEventListener("submit", requestPasswordReset);
resetForm.addEventListener("submit", resetPassword);

bindPasswordToggles(document);
updateRoleUi();
renderReasonAlert();
renderActiveSession();

if (query.get("recuperar") === "1" || query.get("recuperar") === "true") {
    showPanel("recovery");
    forgotForm.elements.correo.focus();
} else {
    showPanel("login");
}
