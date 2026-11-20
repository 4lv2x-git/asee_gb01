/*
 * Recintos y espacios — conectado al Gestor de Espacios.
 * Reutiliza las utilidades compartidas de commerce.js (sesión, JWT, modales y avisos).
 */
const {
    escapeHtml,
    formatPrice,
    api,
    getToken,
    setToken,
    currentUser,
    currentUserRole,
    toast,
    openModal,
    closeModal,
    initModalCloseButtons,
    initLoginModal,
    initHeader
} = window.Subsonic;

const state = {
    grounds: [],
    type: "todos",
    status: "todos",
    query: "",
    sort: "relevancia",
    maxPrice: null,
    pendingRentId: null,
    deepLinkDone: false
};

const STATUS_LABELS = {
    disponible: "Disponible",
    reservado: "Reservado",
    ocupado: "Ocupado"
};

const ROLE_LABELS = {
    admin: "administrador",
    proveedor: "proveedor",
    cliente: "cliente",
    gestor: "gestor"
};

function statusLabel(status) {
    return STATUS_LABELS[String(status || "").toLowerCase()] || "Sin estado";
}

function statusClass(status) {
    const normalized = String(status || "").toLowerCase();
    if (normalized === "disponible") return "status-badge--ok";
    if (normalized === "reservado") return "status-badge--pending";
    return "status-badge--muted";
}

function isManageRole() {
    const role = currentUserRole();
    return role === "admin" || role === "proveedor";
}

function isProvider() {
    return currentUserRole() === "proveedor";
}

function monogram(type) {
    const clean = String(type || "Espacio").trim();
    const words = clean.split(/\s+/).filter(Boolean);
    const letters = words.length > 1
        ? `${words[0][0]}${words[1][0]}`
        : clean.slice(0, 2);
    return letters.toUpperCase();
}

function visualClass(type) {
    const text = String(type || "");
    let hash = 0;
    for (let index = 0; index < text.length; index += 1) {
        hash = (hash + text.charCodeAt(index)) % 3;
    }
    return ["a", "b", "c"][hash];
}

function servicesOf(ground) {
    return Array.isArray(ground?.servicios_incluidos) ? ground.servicios_incluidos : [];
}

function updateSessionUi() {
    const logged = Boolean(getToken());
    const role = currentUserRole();
    const newButton = document.querySelector("#newGroundButton");
    const loginButton = document.querySelector("#groundsLoginButton");
    const logoutButton = document.querySelector("#groundsLogoutButton");
    const session = document.querySelector("#groundsSession");

    if (newButton) newButton.hidden = !isManageRole();
    if (loginButton) loginButton.hidden = logged;
    if (logoutButton) logoutButton.hidden = !logged;
    if (session) {
        session.hidden = !logged;
        session.textContent = logged
            ? `Sesión iniciada como ${ROLE_LABELS[role] || "usuario"}${isProvider() ? " · puedes reservar espacios" : ""}.`
            : "";
    }
}

function updateStats() {
    const available = state.grounds.filter(ground => String(ground.estado || "").toLowerCase() === "disponible");
    const types = new Set(state.grounds.map(ground => String(ground.tipo || "").toLowerCase()).filter(Boolean));
    const prices = state.grounds
        .map(ground => Number(ground.precio))
        .filter(price => Number.isFinite(price));

    document.querySelector("#statGrounds").textContent = state.grounds.length;
    document.querySelector("#statAvailable").textContent = available.length;
    document.querySelector("#statSpacesTypes").textContent = types.size;
    document.querySelector("#statMinPrice").textContent = prices.length
        ? `${formatPrice(Math.min(...prices))} €`
        : "—";
}

function renderTypeChips() {
    const container = document.querySelector("#groundTypes");
    const types = Array.from(new Set(
        state.grounds
            .map(ground => String(ground.tipo || "").trim())
            .filter(Boolean)
    )).sort((a, b) => a.localeCompare(b, "es"));

    if (state.type !== "todos" && !types.some(type => type.toLowerCase() === state.type)) {
        state.type = "todos";
    }

    container.innerHTML = [
        `<button class="${state.type === "todos" ? "active" : ""}" type="button" data-ground-type="todos">Todos</button>`,
        ...types.map(type => {
            const key = type.toLowerCase();
            return `<button class="${state.type === key ? "active" : ""}" type="button" data-ground-type="${escapeHtml(key)}">${escapeHtml(type)}</button>`;
        })
    ].join("");

    container.querySelectorAll("[data-ground-type]").forEach(button => {
        button.addEventListener("click", () => {
            state.type = button.dataset.groundType;
            container.querySelectorAll("[data-ground-type]").forEach(candidate => {
                candidate.classList.toggle("active", candidate === button);
            });
            renderGrounds();
        });
    });
}

function filteredGrounds() {
    const query = state.query.trim().toLowerCase();
    const maxPrice = Number.isFinite(state.maxPrice) ? state.maxPrice : null;

    const grounds = state.grounds.filter(ground => {
        const status = String(ground.estado || "").toLowerCase();
        const type = String(ground.tipo || "").toLowerCase();
        if (state.type !== "todos" && type !== state.type) return false;
        if (state.status !== "todos" && status !== state.status) return false;
        if (maxPrice !== null && Number(ground.precio) > maxPrice) return false;
        if (!query) return true;

        const haystack = [
            ground.tipo,
            ground.ubicacion,
            ground.dimensiones,
            ground.evento_id,
            ground.proveedor_id,
            ...servicesOf(ground)
        ].join(" ").toLowerCase();
        return haystack.includes(query);
    });

    return [...grounds].sort((a, b) => {
        if (state.sort === "precio-asc") return Number(a.precio) - Number(b.precio);
        if (state.sort === "precio-desc") return Number(b.precio) - Number(a.precio);
        if (state.sort === "tipo") return String(a.tipo || "").localeCompare(String(b.tipo || ""), "es");
        if (state.sort === "ubicacion") return String(a.ubicacion || "").localeCompare(String(b.ubicacion || ""), "es");
        return 0;
    });
}

function servicesMarkup(ground, limit) {
    const services = servicesOf(ground);
    if (!services.length) return "";
    const visible = limit ? services.slice(0, limit) : services;
    const extra = limit && services.length > limit
        ? `<li class="ground-service ground-service--more">+${services.length - limit}</li>`
        : "";
    return `<ul class="ground-services">${visible.map(service => `<li class="ground-service">${escapeHtml(service)}</li>`).join("")}${extra}</ul>`;
}

function renderGrounds() {
    const grid = document.querySelector("#groundGrid");
    const count = document.querySelector("#groundsCount");
    const grounds = filteredGrounds();

    count.textContent = grounds.length
        ? `${grounds.length} ${grounds.length === 1 ? "espacio" : "espacios"} encontrados`
        : "Sin resultados para los filtros seleccionados";

    if (!state.grounds.length) {
        grid.innerHTML = `
            <div class="empty-state empty-state--wide ground-empty">
                <strong>Todavía no hay espacios publicados</strong>
                <p>El Gestor de Espacios está disponible, pero su catálogo está vacío. ${isManageRole() ? "Publica el primero para abrir el mapa del festival." : "Vuelve pronto para descubrir las zonas disponibles."}</p>
                ${isManageRole() ? '<button class="btn btn--primary" type="button" data-new-ground>Publicar el primer espacio</button>' : ""}
            </div>
        `;
        const newButton = grid.querySelector("[data-new-ground]");
        if (newButton) newButton.addEventListener("click", () => openGroundForm());
        return;
    }

    if (!grounds.length) {
        grid.innerHTML = `
            <div class="empty-state empty-state--wide">
                <strong>No hay resultados</strong>
                <p>Prueba con otra búsqueda, cambia el tipo de espacio o amplía el precio máximo.</p>
            </div>
        `;
        return;
    }

    const manage = isManageRole();

    grid.innerHTML = grounds.map(ground => {
        const status = String(ground.estado || "").toLowerCase();
        const available = status === "disponible";
        return `
            <article class="ground-card">
                <div class="ground-card__visual ground-card__visual--${visualClass(ground.tipo)}">
                    <span class="ground-card__type">${escapeHtml(ground.tipo || "Espacio")}</span>
                    <span class="status-badge ${statusClass(status)}">${escapeHtml(statusLabel(status))}</span>
                    <span class="ground-card__monogram" aria-hidden="true">${escapeHtml(monogram(ground.tipo))}</span>
                    <span class="ground-card__event">Evento ${escapeHtml(ground.evento_id || "—")}</span>
                </div>
                <div class="ground-card__body">
                    <p class="ground-card__eyebrow">${escapeHtml(ground.ubicacion || "Ubicación por confirmar")} · ${escapeHtml(ground.dimensiones || "Dimensiones por confirmar")}</p>
                    <h3>${escapeHtml(ground.tipo || "Espacio")}</h3>
                    ${servicesMarkup(ground, 3) || '<p class="ground-card__services-empty">Sin servicios adicionales</p>'}
                    <div class="ground-card__footer">
                        <p class="ground-card__price"><strong>${formatPrice(ground.precio)}</strong><span>€</span></p>
                        <button class="btn btn--ghost btn--small" type="button" data-view-ground="${escapeHtml(ground.id)}">Ver detalle</button>
                    </div>
                    <div class="ground-card__actions">
                        <button class="btn btn--primary btn--small" type="button" data-rent-ground="${escapeHtml(ground.id)}" ${available ? "" : "disabled"}>
                            ${available ? "Alquilar" : "No disponible"}
                        </button>
                        ${manage ? `<button class="btn btn--ghost btn--small" type="button" data-edit-ground="${escapeHtml(ground.id)}">Editar</button>` : ""}
                        ${manage ? `<button class="btn btn--ghost btn--small btn--danger" type="button" data-delete-ground="${escapeHtml(ground.id)}">Eliminar</button>` : ""}
                    </div>
                </div>
            </article>
        `;
    }).join("");

    grid.querySelectorAll("[data-view-ground]").forEach(button => {
        button.addEventListener("click", () => openGroundDetail(button.dataset.viewGround));
    });
    grid.querySelectorAll("[data-rent-ground]").forEach(button => {
        button.addEventListener("click", () => rentGround(button.dataset.rentGround));
    });
    grid.querySelectorAll("[data-edit-ground]").forEach(button => {
        button.addEventListener("click", () => openGroundForm(findGround(button.dataset.editGround)));
    });
    grid.querySelectorAll("[data-delete-ground]").forEach(button => {
        button.addEventListener("click", () => deleteGround(button.dataset.deleteGround));
    });
}

function findGround(groundId) {
    return state.grounds.find(ground => ground.id === groundId) || null;
}

function openDeepLink() {
    if (state.deepLinkDone) return;
    const groundId = new URLSearchParams(window.location.search).get("id");
    if (!groundId) return;
    const ground = findGround(groundId);
    if (!ground) return;
    state.deepLinkDone = true;
    openGroundDetail(groundId);
}

function openGroundDetail(groundId) {
    const ground = findGround(groundId);
    if (!ground) {
        toast("El espacio ya no está disponible");
        loadGrounds();
        return;
    }

    const status = String(ground.estado || "").toLowerCase();
    const available = status === "disponible";
    const manage = isManageRole();
    const logged = Boolean(getToken());

    document.querySelector("#groundModalTitle").textContent = ground.tipo || "Espacio";

    const rentAction = available
        ? (logged && isProvider()
            ? `<button class="btn btn--primary btn--full" type="button" data-rent-ground="${escapeHtml(ground.id)}">Reservar este espacio</button>`
            : `<button class="btn btn--primary btn--full" type="button" data-rent-ground="${escapeHtml(ground.id)}">${logged ? "Reservar este espacio" : "Inicia sesión para reservar"}</button>`)
        : "";

    const rentNote = !available
        ? `<p class="ground-detail__hint">Este espacio está ${escapeHtml(statusLabel(status).toLowerCase())}${ground.proveedor_id ? ` por el proveedor <strong>${escapeHtml(ground.proveedor_id)}</strong>` : ""} y no admite nuevas reservas.</p>`
        : (logged && !isProvider()
            ? '<p class="ground-detail__hint">Solo las cuentas con rol <strong>proveedor</strong> pueden alquilar espacios. Puedes registrarte como proveedor desde la página de registro.</p>'
            : "");

    document.querySelector("#groundDetail").innerHTML = `
        <div class="ground-detail__summary">
            <div class="ground-detail__price">
                <span>Precio de alquiler</span>
                <strong>${formatPrice(ground.precio)} €</strong>
            </div>
            <span class="status-badge ${statusClass(status)}">${escapeHtml(statusLabel(status))}</span>
        </div>

        <div class="ground-meta">
            <article><span>Evento</span><strong>${escapeHtml(ground.evento_id || "—")}</strong></article>
            <article><span>Tipo</span><strong>${escapeHtml(ground.tipo || "—")}</strong></article>
            <article><span>Dimensiones</span><strong>${escapeHtml(ground.dimensiones || "—")}</strong></article>
            <article><span>Ubicación</span><strong>${escapeHtml(ground.ubicacion || "Por confirmar")}</strong></article>
            <article><span>Proveedor</span><strong>${escapeHtml(ground.proveedor_id || "Sin asignar")}</strong></article>
            <article><span>Referencia</span><strong>${escapeHtml(ground.id)}</strong></article>
        </div>

        <section class="ground-detail__section">
            <h3>Servicios incluidos</h3>
            ${servicesMarkup(ground) || '<p class="ground-detail__hint">Este espacio no declara servicios adicionales.</p>'}
        </section>

        <div class="ground-detail__actions">
            ${rentAction}
            ${rentNote}
            ${manage ? `
                <div class="ground-detail__manage">
                    <button class="btn btn--ghost btn--small" type="button" data-edit-ground="${escapeHtml(ground.id)}">Editar espacio</button>
                    <button class="btn btn--ghost btn--small btn--danger" type="button" data-delete-ground="${escapeHtml(ground.id)}">Eliminar espacio</button>
                </div>
            ` : ""}
        </div>
    `;

    const detail = document.querySelector("#groundDetail");
    const rentButton = detail.querySelector("[data-rent-ground]");
    if (rentButton) rentButton.addEventListener("click", () => rentGround(ground.id));

    const editButton = detail.querySelector("[data-edit-ground]");
    if (editButton) editButton.addEventListener("click", () => {
        closeModal("groundModal");
        openGroundForm(ground);
    });

    const deleteButton = detail.querySelector("[data-delete-ground]");
    if (deleteButton) deleteButton.addEventListener("click", () => deleteGround(ground.id));

    openModal("groundModal");
}

function openGroundForm(ground = null) {
    const form = document.querySelector("#groundForm");
    form.reset();
    document.querySelector("#groundFormMessage").textContent = "";
    document.querySelector("#groundFormTitle").textContent = ground ? "Editar espacio" : "Publicar espacio";
    document.querySelector("#groundFormSubmit").textContent = ground ? "Guardar cambios" : "Publicar espacio";
    document.querySelector("#groundId").value = ground ? ground.id : "";
    document.querySelector("#groundEvent").value = ground ? (ground.evento_id || "") : "";
    document.querySelector("#groundType").value = ground ? (ground.tipo || "") : "";
    document.querySelector("#groundDimensions").value = ground ? (ground.dimensiones || "") : "";
    document.querySelector("#groundLocation").value = ground ? (ground.ubicacion || "") : "";
    document.querySelector("#groundPrice").value = ground ? ground.precio : "";
    document.querySelector("#groundStatusInput").value = ground ? (ground.estado || "disponible") : "disponible";
    document.querySelector("#groundServices").value = ground ? servicesOf(ground).join(", ") : "";
    openModal("groundFormModal");
}

function splitServices(value) {
    return String(value || "")
        .split(",")
        .map(service => service.trim())
        .filter(Boolean);
}

async function submitGroundForm(event) {
    event.preventDefault();

    if (!isManageRole()) {
        document.querySelector("#groundFormMessage").textContent = "Inicia sesión como administrador o proveedor para gestionar espacios.";
        return;
    }

    const groundId = document.querySelector("#groundId").value.trim();
    const payload = {
        evento_id: document.querySelector("#groundEvent").value.trim(),
        tipo: document.querySelector("#groundType").value.trim(),
        dimensiones: document.querySelector("#groundDimensions").value.trim(),
        ubicacion: document.querySelector("#groundLocation").value.trim() || null,
        servicios_incluidos: splitServices(document.querySelector("#groundServices").value),
        precio: Number(document.querySelector("#groundPrice").value),
        estado: document.querySelector("#groundStatusInput").value
    };

    if (!payload.evento_id || !payload.tipo || !payload.dimensiones || !Number.isFinite(payload.precio)) {
        document.querySelector("#groundFormMessage").textContent = "Completa evento, tipo, dimensiones y un precio válido.";
        return;
    }

    const button = document.querySelector("#groundFormSubmit");
    const message = document.querySelector("#groundFormMessage");
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = "Guardando...";
    message.textContent = "";

    try {
        const url = groundId
            ? `/api/gateway/grounds/${encodeURIComponent(groundId)}`
            : "/api/gateway/grounds";
        await api(url, {
            method: groundId ? "PUT" : "POST",
            body: JSON.stringify(payload)
        });
        closeModal("groundFormModal");
        toast(groundId ? "Espacio actualizado" : "Espacio publicado");
        await loadGrounds();
    } catch (error) {
        message.textContent = error.message || "No se pudo guardar el espacio";
    } finally {
        button.disabled = false;
        button.textContent = originalText;
    }
}

async function rentGround(groundId) {
    const ground = findGround(groundId);
    if (!ground) {
        toast("El espacio ya no está disponible");
        await loadGrounds();
        return;
    }

    if (!getToken()) {
        state.pendingRentId = groundId;
        closeModal("groundModal");
        openModal("loginModal");
        toast("Inicia sesión con tu cuenta de proveedor para reservar");
        return;
    }

    if (!isProvider()) {
        toast("Necesitas una cuenta de proveedor para alquilar un espacio");
        return;
    }

    if (String(ground.estado || "").toLowerCase() !== "disponible") {
        toast("Este espacio ya no está disponible");
        await loadGrounds();
        return;
    }

    if (!window.confirm(`¿Reservar "${ground.tipo}" por ${formatPrice(ground.precio)} €?`)) return;

    try {
        await api(`/api/gateway/grounds/${encodeURIComponent(groundId)}/rent`, {
            method: "POST",
            body: JSON.stringify({ proveedor_id: currentUser()?.sub })
        });
        closeModal("groundModal");
        toast("Espacio reservado correctamente");
        await loadGrounds();
    } catch (error) {
        toast(error.message || "No se pudo reservar el espacio");
    }
}

async function deleteGround(groundId) {
    const ground = findGround(groundId);
    if (!ground) return;
    if (!window.confirm(`¿Eliminar el espacio "${ground.tipo}"? Esta acción no se puede deshacer.`)) return;

    try {
        await api(`/api/gateway/grounds/${encodeURIComponent(groundId)}`, { method: "DELETE" });
        closeModal("groundModal");
        toast("Espacio eliminado");
        await loadGrounds();
    } catch (error) {
        toast(error.message || "No se pudo eliminar el espacio");
    }
}

async function loadGrounds() {
    const grid = document.querySelector("#groundGrid");
    const count = document.querySelector("#groundsCount");
    grid.innerHTML = '<div class="loading-card loading-card--wide">Cargando espacios...</div>';
    count.textContent = "Cargando espacios...";

    try {
        const grounds = await api("/api/gateway/grounds");
        state.grounds = Array.isArray(grounds) ? grounds : [];
        updateStats();
        renderTypeChips();
        renderGrounds();
        openDeepLink();
    } catch (error) {
        count.textContent = "Catálogo no disponible";
        grid.innerHTML = `
            <div class="empty-state empty-state--wide empty-state--error ground-empty">
                <strong>No se pudo conectar con el Gestor de Espacios</strong>
                <p>Comprueba que el microservicio de recintos esté iniciado y que el gateway pueda acceder a él.</p>
                <button class="btn btn--ghost" type="button" id="retryGrounds">Reintentar</button>
            </div>
        `;
        document.querySelector("#retryGrounds").addEventListener("click", loadGrounds);
    }
}

function initEvents() {
    document.querySelector("#groundQuery").addEventListener("input", event => {
        state.query = event.target.value;
        renderGrounds();
    });

    document.querySelector("#groundStatus").addEventListener("change", event => {
        state.status = event.target.value;
        renderGrounds();
    });

    document.querySelector("#groundSort").addEventListener("change", event => {
        state.sort = event.target.value;
        renderGrounds();
    });

    document.querySelector("#groundMaxPrice").addEventListener("input", event => {
        const value = Number(event.target.value);
        state.maxPrice = event.target.value !== "" && Number.isFinite(value) ? value : null;
        renderGrounds();
    });

    document.querySelector("#newGroundButton").addEventListener("click", () => openGroundForm());
    document.querySelector("#groundForm").addEventListener("submit", submitGroundForm);

    document.querySelector("#groundsLogoutButton").addEventListener("click", () => {
        setToken(null);
        updateSessionUi();
        toast("Sesión cerrada");
        renderGrounds();
    });

    document.querySelector("#cartButton").addEventListener("click", () => {
        window.location.href = "/tienda.html";
    });

    initModalCloseButtons();
    initHeader();
    initLoginModal(() => {
        updateSessionUi();
        renderGrounds();
        if (state.pendingRentId) {
            const groundId = state.pendingRentId;
            state.pendingRentId = null;
            window.setTimeout(() => openGroundDetail(groundId), 400);
        }
    });
}

updateSessionUi();
initEvents();
loadGrounds();
