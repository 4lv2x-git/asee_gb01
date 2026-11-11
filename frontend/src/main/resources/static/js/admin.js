import { api, handleSessionError, requireSession, session, ApiError } from "./api.js";
import {
    bindPasswordToggles,
    clearFormErrors,
    confirmAction,
    escapeHtml,
    focusFirstError,
    initials,
    roleLabel,
    setFieldError,
    setLoading,
    showAlert,
    stateLabel,
} from "./ui.js";

const pageAlert = document.getElementById("page-alert");
const loadingBox = document.getElementById("admin-loading");
const contentBox = document.getElementById("admin-content");
const headerName = document.getElementById("header-user-name");
const headerAvatar = document.getElementById("header-user-avatar");
const usersBody = document.getElementById("users-body");
const usersTableWrap = document.getElementById("users-table-wrap");
const usersEmpty = document.getElementById("users-empty");
const usersSummary = document.getElementById("users-summary");
const userCount = document.getElementById("user-count");
const searchInput = document.getElementById("user-search");
const roleFilter = document.getElementById("filter-role");
const stateFilter = document.getElementById("filter-state");
const clearFiltersButton = document.getElementById("clear-filters");
const refreshButton = document.getElementById("refresh-users");
const logoutButton = document.getElementById("logout-button");

const editDialog = document.getElementById("edit-user-dialog");
const editForm = document.getElementById("edit-user-form");
const passwordDialog = document.getElementById("password-dialog");
const passwordForm = document.getElementById("password-form");

let currentUser = null;
let users = [];
let editingUser = null;
let passwordUser = null;

const filters = {
    search: "",
    role: "all",
    state: "all",
};

function safeRole(role) {
    return ["cliente", "proveedor", "admin", "visitante"].includes(role) ? role : "visitante";
}

function roleBadge(role) {
    return `<span class="badge badge--${safeRole(role)}">${escapeHtml(roleLabel(role))}</span>`;
}

function stateBadge(state) {
    const key = state === "suspendido" ? "suspendido" : "activo";
    return `<span class="badge badge--${key}">${escapeHtml(stateLabel(state))}</span>`;
}

function renderHeader(user) {
    headerName.textContent = user.nombre;
    headerAvatar.textContent = initials(user.nombre);
}

function renderStats() {
    const set = (id, value) => {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    };

    set("stat-total", users.length);
    set("stat-clientes", users.filter((user) => user.rol === "cliente").length);
    set("stat-proveedores", users.filter((user) => user.rol === "proveedor").length);
    set("stat-activos", users.filter((user) => user.estado === "activo").length);
    set("stat-suspendidos", users.filter((user) => user.estado === "suspendido").length);
    set("user-count", users.length);
}

function filteredUsers() {
    const term = filters.search.trim().toLowerCase();
    const roleRank = { admin: 0, proveedor: 1, cliente: 2, visitante: 3 };

    return users
        .filter((user) => {
            if (filters.role !== "all" && user.rol !== filters.role) {
                return false;
            }
            if (filters.state !== "all" && user.estado !== filters.state) {
                return false;
            }
            if (!term) {
                return true;
            }
            return [user.nombre, user.correo, user.tipoServicio, user.id]
                .some((value) => String(value || "").toLowerCase().includes(term));
        })
        .sort((a, b) => {
            const order = (roleRank[a.rol] ?? 9) - (roleRank[b.rol] ?? 9);
            return order || String(a.nombre).localeCompare(String(b.nombre), "es");
        });
}

function actionButtons(user) {
    const isSelf = user.id === currentUser.id;
    const stateLabelText = user.estado === "activo" ? "Suspender" : "Activar";
    const stateDisabled = isSelf
        ? 'disabled title="No puedes suspender tu propia cuenta"'
        : "";
    const deleteDisabled = isSelf
        ? 'disabled title="No puedes eliminar tu propia cuenta"'
        : "";

    return `
        <div class="row-actions">
            <button type="button" class="btn btn--table" data-action="edit" data-user-id="${escapeHtml(user.id)}">
                Editar
            </button>
            <button type="button" class="btn btn--table" data-action="state" data-user-id="${escapeHtml(user.id)}" ${stateDisabled}>
                ${stateLabelText}
            </button>
            <button type="button" class="btn btn--table" data-action="password" data-user-id="${escapeHtml(user.id)}">
                Contraseña
            </button>
            <button type="button" class="btn btn--table btn--danger-ghost" data-action="delete" data-user-id="${escapeHtml(user.id)}" ${deleteDisabled}>
                Eliminar
            </button>
        </div>
    `;
}

function renderTable() {
    const list = filteredUsers();

    usersBody.innerHTML = list
        .map((user) => {
            const isSelf = user.id === currentUser.id;
            const service = user.rol === "proveedor"
                ? escapeHtml(user.tipoServicio || "Sin especificar")
                : '<span class="muted">—</span>';
            const verified = user.emailVerificado
                ? '<span class="verify-dot verify-dot--ok" role="img" title="Correo verificado" aria-label="Correo verificado"></span>'
                : '<span class="verify-dot" role="img" title="Correo sin verificar" aria-label="Correo sin verificar"></span>';

            return `
                <tr>
                    <td>
                        <div class="user-cell">
                            <span class="user-cell__avatar role-${safeRole(user.rol)}" aria-hidden="true">${escapeHtml(initials(user.nombre))}</span>
                            <span class="user-cell__text">
                                <strong>${escapeHtml(user.nombre)}${isSelf ? ' <span class="you-tag">Tú</span>' : ""}</strong>
                                <span class="mono" title="${escapeHtml(user.id)}">${escapeHtml(String(user.id).slice(-8))}</span>
                            </span>
                        </div>
                    </td>
                    <td>${roleBadge(user.rol)}</td>
                    <td>${stateBadge(user.estado)}</td>
                    <td>
                        <span class="email-cell">
                            <span>${escapeHtml(user.correo)}</span>
                            ${verified}
                        </span>
                    </td>
                    <td>${service}</td>
                    <td>${actionButtons(user)}</td>
                </tr>
            `;
        })
        .join("");

    usersTableWrap.hidden = list.length === 0;
    usersEmpty.hidden = list.length !== 0;

    if (list.length === users.length) {
        usersSummary.textContent = `${users.length} ${users.length === 1 ? "usuario registrado" : "usuarios registrados"}.`;
    } else {
        usersSummary.textContent = `Mostrando ${list.length} de ${users.length} usuarios.`;
    }
}

function renderAll() {
    renderStats();
    renderTable();
}

function userById(id) {
    return users.find((user) => user.id === id) || null;
}

async function loadUsers({ announce = false } = {}) {
    setLoading(refreshButton, true, "Actualizando…");
    try {
        const result = await api("/v1/usuarios/admin");
        users = Array.isArray(result) ? result : [];
        renderAll();
        if (announce) {
            showAlert(pageAlert, "Listado de usuarios actualizado.", "success");
        }
    } catch (error) {
        if (handleSessionError(error)) {
            return;
        }
        users = [];
        renderAll();
        showAlert(
            pageAlert,
            error instanceof ApiError ? error.message : "No se pudo cargar el listado de usuarios.",
            "error"
        );
    } finally {
        setLoading(refreshButton, false);
    }
}

function openEditDialog(user) {
    editingUser = user;
    clearFormErrors(editForm);

    editForm.elements.userId.value = user.id;
    editForm.elements.nombre.value = user.nombre || "";
    editForm.elements.direccion.value = user.direccion || "";

    const providerFields = document.getElementById("edit-provider-fields");
    const isProvider = user.rol === "proveedor";
    providerFields.hidden = !isProvider;
    editForm.elements.tipoServicio.required = isProvider;
    editForm.elements.tipoServicio.value = user.tipoServicio || "";
    editForm.elements.descripcionServicio.value = user.descripcionServicio || "";

    document.getElementById("edit-user-summary").innerHTML = `
        <span class="dialog-user__avatar role-${safeRole(user.rol)}" aria-hidden="true">${escapeHtml(initials(user.nombre))}</span>
        <span class="dialog-user__text">
            <strong>${escapeHtml(user.nombre)}</strong>
            <span>${escapeHtml(user.correo)} · ${escapeHtml(roleLabel(user.rol))}</span>
        </span>
    `;

    editDialog.showModal();
    editForm.elements.nombre.focus();
}

async function saveEditedUser(event) {
    event.preventDefault();
    clearFormErrors(editForm);

    const id = editForm.elements.userId.value;
    const user = userById(id);
    if (!user) {
        editDialog.close();
        return;
    }

    const nombre = editForm.elements.nombre.value.trim();
    const direccion = editForm.elements.direccion.value.trim();
    const isProvider = user.rol === "proveedor";
    const tipoServicio = isProvider ? editForm.elements.tipoServicio.value.trim() : "";
    const descripcionServicio = isProvider
        ? editForm.elements.descripcionServicio.value.trim()
        : "";

    let valid = true;
    if (!nombre) {
        setFieldError(editForm, "nombre", "El nombre es obligatorio.");
        valid = false;
    }
    if (isProvider && !tipoServicio) {
        setFieldError(editForm, "tipoServicio", "Indica el tipo de servicio.");
        valid = false;
    }
    if (!valid) {
        focusFirstError(editForm);
        return;
    }

    const submit = document.getElementById("edit-user-submit");
    setLoading(submit, true, "Guardando…");

    const body = { nombre, direccion: direccion || null };
    if (isProvider) {
        body.tipoServicio = tipoServicio || null;
        body.descripcionServicio = descripcionServicio || null;
    }

    try {
        const updated = await api(`/v1/usuarios/admin/${encodeURIComponent(id)}`, {
            method: "PUT",
            body,
        });

        users = users.map((item) => (item.id === id ? updated : item));
        renderAll();
        editDialog.close();
        showAlert(pageAlert, `Usuario «${updated.nombre}» actualizado correctamente.`, "success");

        if (id === currentUser.id) {
            currentUser = updated;
            session.updateUser(updated);
            renderHeader(updated);
        }
    } catch (error) {
        if (handleSessionError(error)) {
            return;
        }
        showAlert(
            pageAlert,
            error instanceof ApiError ? error.message : "No se pudo actualizar el usuario.",
            "error"
        );
    } finally {
        setLoading(submit, false);
    }
}

async function toggleState(user, button) {
    const nextState = user.estado === "activo" ? "suspendido" : "activo";
    const confirmed = await confirmAction({
        title: nextState === "suspendido" ? "Suspender cuenta" : "Activar cuenta",
        message: nextState === "suspendido"
            ? `La cuenta de ${user.nombre} quedará bloqueada y se cerrarán todas sus sesiones activas.`
            : `La cuenta de ${user.nombre} podrá volver a iniciar sesión.`,
        confirmLabel: nextState === "suspendido" ? "Suspender" : "Activar",
        danger: nextState === "suspendido",
    });
    if (!confirmed) {
        return;
    }

    setLoading(button, true, nextState === "suspendido" ? "Suspendiendo…" : "Activando…");
    try {
        const updated = await api(`/v1/usuarios/admin/${encodeURIComponent(user.id)}/estado`, {
            method: "PATCH",
            body: { estado: nextState },
        });
        users = users.map((item) => (item.id === user.id ? updated : item));
        renderAll();
        showAlert(
            pageAlert,
            `Cuenta de ${user.nombre} ${nextState === "suspendido" ? "suspendida" : "activada"} correctamente.`,
            "success"
        );
    } catch (error) {
        if (handleSessionError(error)) {
            return;
        }
        showAlert(
            pageAlert,
            error instanceof ApiError ? error.message : "No se pudo cambiar el estado de la cuenta.",
            "error"
        );
        setLoading(button, false);
    }
}

function openPasswordDialog(user) {
    passwordUser = user;
    passwordForm.reset();
    clearFormErrors(passwordForm);
    document.getElementById("password-user-summary").innerHTML = `
        <strong>${escapeHtml(user.nombre)}</strong>
        <span>${escapeHtml(user.correo)}</span>
    `;
    passwordDialog.showModal();
    passwordForm.elements.nuevaPassword.focus();
}

async function savePassword(event) {
    event.preventDefault();
    clearFormErrors(passwordForm);

    const nuevaPassword = passwordForm.elements.nuevaPassword.value;
    const confirmPassword = passwordForm.elements.nuevaPasswordConfirm.value;

    let valid = true;
    if (!nuevaPassword || nuevaPassword.length < 8) {
        setFieldError(passwordForm, "nuevaPassword", "La contraseña debe tener al menos 8 caracteres.");
        valid = false;
    }
    if (!confirmPassword) {
        setFieldError(passwordForm, "nuevaPasswordConfirm", "Repite la contraseña.");
        valid = false;
    } else if (nuevaPassword !== confirmPassword) {
        setFieldError(passwordForm, "nuevaPasswordConfirm", "Las contraseñas no coinciden.");
        valid = false;
    }
    if (!valid) {
        focusFirstError(passwordForm);
        return;
    }

    const submit = document.getElementById("password-submit");
    setLoading(submit, true, "Actualizando…");

    try {
        await api(`/v1/usuarios/admin-password-reset/${encodeURIComponent(passwordUser.id)}`, {
            method: "PUT",
            body: { nuevaPassword },
        });
        passwordDialog.close();
        showAlert(
            pageAlert,
            `Contraseña de ${passwordUser.nombre} actualizada. Se han invalidado sus sesiones anteriores.`,
            "success"
        );

        if (passwordUser.id === currentUser.id) {
            session.clear();
            window.setTimeout(() => {
                window.location.replace("login.html?reason=password");
            }, 900);
        }
    } catch (error) {
        if (handleSessionError(error)) {
            return;
        }
        showAlert(
            pageAlert,
            error instanceof ApiError ? error.message : "No se pudo actualizar la contraseña.",
            "error"
        );
    } finally {
        setLoading(submit, false);
    }
}

async function deleteUser(user, button) {
    const confirmed = await confirmAction({
        title: "Eliminar usuario",
        message: `La cuenta de ${user.nombre} (${user.correo}) se eliminará de forma permanente. Esta acción no se puede deshacer.`,
        confirmLabel: "Eliminar definitivamente",
        danger: true,
    });
    if (!confirmed) {
        return;
    }

    setLoading(button, true, "Eliminando…");
    try {
        await api(`/v1/usuarios/admin/${encodeURIComponent(user.id)}`, { method: "DELETE" });
        users = users.filter((item) => item.id !== user.id);
        renderAll();
        showAlert(pageAlert, `Usuario «${user.nombre}» eliminado correctamente.`, "success");
    } catch (error) {
        if (handleSessionError(error)) {
            return;
        }
        showAlert(
            pageAlert,
            error instanceof ApiError ? error.message : "No se pudo eliminar el usuario.",
            "error"
        );
        setLoading(button, false);
    }
}

function syncFilters() {
    filters.search = searchInput.value;
    filters.role = roleFilter.value;
    filters.state = stateFilter.value;
    renderTable();
}

function clearFilters() {
    searchInput.value = "";
    roleFilter.value = "all";
    stateFilter.value = "all";
    filters.search = "";
    filters.role = "all";
    filters.state = "all";
    renderTable();
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

function bindDialog(dialog) {
    dialog.querySelectorAll("[data-close-dialog]").forEach((button) => {
        button.addEventListener("click", () => dialog.close());
    });
    dialog.addEventListener("click", (event) => {
        if (event.target === dialog) {
            dialog.close();
        }
    });
}

async function init() {
    bindPasswordToggles(document);
    bindDialog(editDialog);
    bindDialog(passwordDialog);

    try {
        currentUser = await requireSession({ adminOnly: true });
    } catch (error) {
        if (loadingBox) {
            loadingBox.hidden = true;
        }
        if (error instanceof ApiError && error.status !== 401 && error.status !== 403) {
            showAlert(pageAlert, error.message, "error");
        }
        return;
    }

    renderHeader(currentUser);
    loadingBox.hidden = true;
    contentBox.hidden = false;

    usersBody.addEventListener("click", (event) => {
        const button = event.target.closest("[data-action]");
        if (!button || button.disabled) {
            return;
        }
        const user = userById(button.dataset.userId);
        if (!user) {
            return;
        }

        switch (button.dataset.action) {
            case "edit":
                openEditDialog(user);
                break;
            case "state":
                toggleState(user, button);
                break;
            case "password":
                openPasswordDialog(user);
                break;
            case "delete":
                deleteUser(user, button);
                break;
            default:
                break;
        }
    });

    editForm.addEventListener("submit", saveEditedUser);
    passwordForm.addEventListener("submit", savePassword);
    searchInput.addEventListener("input", syncFilters);
    roleFilter.addEventListener("change", syncFilters);
    stateFilter.addEventListener("change", syncFilters);
    clearFiltersButton.addEventListener("click", clearFilters);
    refreshButton.addEventListener("click", () => loadUsers({ announce: true }));
    logoutButton.addEventListener("click", logout);

    await loadUsers();
}

init();
