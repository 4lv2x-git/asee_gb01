import {
    api,
    handleSessionError,
    requireSession,
    session,
    ApiError,
} from "./api.js";
import {
    clearAlert,
    clearFormErrors,
    confirmAction,
    escapeHtml,
    focusFirstError,
    initials,
    params,
    roleLabel,
    setFieldError,
    setLoading,
    showAlert,
    stateLabel,
} from "./ui.js";

const pageAlert = document.getElementById("page-alert");
const loadingBox = document.getElementById("profile-loading");
const contentBox = document.getElementById("profile-content");
const headerName = document.getElementById("header-user-name");
const headerAvatar = document.getElementById("header-user-avatar");
const adminNav = document.getElementById("admin-nav");
const identityAvatar = document.getElementById("identity-avatar");
const profileForm = document.getElementById("profile-form");
const providerFields = document.getElementById("profile-provider-fields");
const verifyBlock = document.getElementById("verify-email-block");
const verifyForm = document.getElementById("verify-email-form");
const deleteButton = document.getElementById("delete-account");
const logoutButton = document.getElementById("logout-button");

let currentUser = null;

function roleBadge(role) {
    const key = ["cliente", "proveedor", "admin", "visitante"].includes(role) ? role : "visitante";
    return `<span class="badge badge--${key}">${escapeHtml(roleLabel(role))}</span>`;
}

function stateBadge(state) {
    const key = state === "suspendido" ? "suspendido" : "activo";
    return `<span class="badge badge--${key}">${escapeHtml(stateLabel(state))}</span>`;
}

function renderHeader(user) {
    if (headerName) {
        headerName.textContent = user.nombre;
    }
    if (headerAvatar) {
        headerAvatar.textContent = initials(user.nombre);
    }
    if (adminNav) {
        adminNav.hidden = user.rol !== "admin";
    }
}

function renderIdentity(user) {
    if (identityAvatar) {
        identityAvatar.textContent = initials(user.nombre);
    }

    const setText = (id, value) => {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    };

    setText("identity-name", user.nombre);
    setText("identity-email", user.correo);
    setText("detail-role", roleLabel(user.rol));
    setText("detail-state", stateLabel(user.estado));
    setText("detail-verified", user.emailVerificado ? "Verificado" : "Pendiente");
    setText("detail-id", user.id);

    const roleSlot = document.getElementById("profile-role-badge");
    if (roleSlot) {
        roleSlot.innerHTML = roleBadge(user.rol);
    }
    const stateSlot = document.getElementById("profile-state-badge");
    if (stateSlot) {
        stateSlot.innerHTML = stateBadge(user.estado);
    }
    const verifiedSlot = document.getElementById("detail-verified-badge");
    if (verifiedSlot) {
        verifiedSlot.innerHTML = user.emailVerificado
            ? '<span class="badge badge--activo">Verificado</span>'
            : '<span class="badge badge--warning">Sin verificar</span>';
    }

    const serviceRow = document.getElementById("detail-service-row");
    if (serviceRow) {
        serviceRow.hidden = user.rol !== "proveedor";
    }
    setText("detail-service", user.tipoServicio || "Sin especificar");

    const descriptionBlock = document.getElementById("identity-description");
    if (descriptionBlock) {
        const hasDescription = user.rol === "proveedor" && user.descripcionServicio;
        descriptionBlock.hidden = !hasDescription;
        if (hasDescription) {
            descriptionBlock.textContent = user.descripcionServicio;
        }
    }

    if (verifyBlock) {
        verifyBlock.hidden = Boolean(user.emailVerificado);
    }
}

function renderForm(user) {
    profileForm.elements.nombre.value = user.nombre || "";
    profileForm.elements.direccion.value = user.direccion || "";

    const isProvider = user.rol === "proveedor";
    if (providerFields) {
        providerFields.hidden = !isProvider;
        profileForm.elements.tipoServicio.required = isProvider;
        profileForm.elements.tipoServicio.value = user.tipoServicio || "";
        profileForm.elements.descripcionServicio.value = user.descripcionServicio || "";
    }
}

function renderProfile(user) {
    renderHeader(user);
    renderIdentity(user);
    renderForm(user);
    if (loadingBox) {
        loadingBox.hidden = true;
    }
    if (contentBox) {
        contentBox.hidden = false;
    }
}

async function saveProfile(event) {
    event.preventDefault();
    clearFormErrors(profileForm);
    clearAlert(pageAlert);

    const nombre = profileForm.elements.nombre.value.trim();
    const direccion = profileForm.elements.direccion.value.trim();
    const isProvider = currentUser.rol === "proveedor";
    const tipoServicio = isProvider ? profileForm.elements.tipoServicio.value.trim() : "";
    const descripcionServicio = isProvider
        ? profileForm.elements.descripcionServicio.value.trim()
        : "";

    let valid = true;
    if (!nombre) {
        setFieldError(profileForm, "nombre", "El nombre es obligatorio.");
        valid = false;
    }
    if (isProvider && !tipoServicio) {
        setFieldError(profileForm, "tipoServicio", "Indica el tipo de servicio que ofreces.");
        valid = false;
    }
    if (!valid) {
        focusFirstError(profileForm);
        return;
    }

    const submit = document.getElementById("profile-submit");
    setLoading(submit, true, "Guardando…");

    const body = {
        nombre,
        direccion: direccion || null,
    };
    if (isProvider) {
        body.tipoServicio = tipoServicio || null;
        body.descripcionServicio = descripcionServicio || null;
    }

    try {
        const updated = await api("/v1/usuarios/perfil", { method: "PATCH", body });
        currentUser = updated;
        session.updateUser(updated);
        renderProfile(updated);
        showAlert(pageAlert, "Perfil actualizado correctamente.", "success");
    } catch (error) {
        if (handleSessionError(error)) {
            return;
        }
        showAlert(
            pageAlert,
            error instanceof ApiError ? error.message : "No se pudo actualizar el perfil.",
            "error"
        );
    } finally {
        setLoading(submit, false);
    }
}

async function verifyEmail(event) {
    event.preventDefault();
    clearFormErrors(verifyForm);
    pageAlert.hidden = true;

    const codigo = verifyForm.elements.codigo.value.trim();
    if (!codigo) {
        setFieldError(verifyForm, "codigo", "Introduce el código recibido.");
        focusFirstError(verifyForm);
        return;
    }

    const submit = document.getElementById("verify-submit");
    setLoading(submit, true, "Verificando…");

    try {
        const updated = await api("/v1/auth/verify-email", {
            method: "POST",
            auth: false,
            body: { codigo },
        });
        currentUser = { ...currentUser, ...updated };
        session.updateUser(currentUser);
        renderProfile(currentUser);
        showAlert(pageAlert, "Correo verificado correctamente.", "success");
    } catch (error) {
        if (handleSessionError(error)) {
            return;
        }
        showAlert(
            pageAlert,
            error instanceof ApiError ? error.message : "No se pudo verificar el correo.",
            "error"
        );
    } finally {
        setLoading(submit, false);
    }
}

async function deleteAccount() {
    const confirmed = await confirmAction({
        title: "Eliminar mi cuenta",
        message:
            "Esta acción es permanente. Se borrarán tus datos de la plataforma y se cerrarán todas tus sesiones. No podrás recuperar la cuenta.",
        confirmLabel: "Eliminar cuenta",
        danger: true,
    });
    if (!confirmed) {
        return;
    }

    setLoading(deleteButton, true, "Eliminando…");
    try {
        await api("/v1/usuarios/perfil", { method: "DELETE" });
        session.clear();
        window.location.replace("login.html?reason=deleted");
    } catch (error) {
        if (handleSessionError(error)) {
            return;
        }
        showAlert(
            pageAlert,
            error instanceof ApiError ? error.message : "No se pudo eliminar la cuenta.",
            "error"
        );
        setLoading(deleteButton, false);
    }
}

async function logout() {
    setLoading(logoutButton, true, "Saliendo…");
    try {
        await api("/v1/auth/logout", { method: "POST" });
    } catch (error) {
        // La sesión local se elimina siempre.
    }
    session.clear();
    window.location.replace("index.html");
}

async function init() {
    try {
        currentUser = await requireSession();
    } catch (error) {
        if (loadingBox) {
            loadingBox.hidden = true;
        }
        if (error instanceof ApiError && error.status !== 401 && error.status !== 403) {
            showAlert(pageAlert, error.message, "error");
        }
        return;
    }

    renderProfile(currentUser);

    if (params().get("denied") === "1") {
        showAlert(
            pageAlert,
            "El panel de administración solo está disponible para cuentas con rol de administrador.",
            "warning"
        );
    }

    profileForm.addEventListener("submit", saveProfile);
    verifyForm.addEventListener("submit", verifyEmail);
    deleteButton.addEventListener("click", deleteAccount);
    logoutButton.addEventListener("click", logout);
}

init();
