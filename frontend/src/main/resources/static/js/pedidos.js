const {
    escapeHtml,
    formatPrice,
    formatDateTime,
    statusLabel,
    statusClass,
    api,
    getToken,
    setToken,
    toast,
    openModal,
    closeModal,
    initModalCloseButtons,
    initLoginModal,
    initHeader
} = window.Subsonic;

const state = {
    orders: [],
    filter: "todos"
};

function showLoginPrompt() {
    document.querySelector("#statOrders").textContent = "—";
    document.querySelector("#statSpent").textContent = "—";
    document.querySelector("#statEntries").textContent = "—";
    document.querySelector("#ordersGrid").innerHTML = `
        <div class="empty-state empty-state--wide login-prompt">
            <strong>Inicia sesión para ver tus pedidos</strong>
            <p>El historial de compras se consulta con tu cuenta de cliente. Tus entradas y solicitudes de cancelación aparecerán aquí.</p>
            <div class="login-prompt__actions">
                <button class="btn btn--primary" type="button" data-open-login>Iniciar sesión</button>
                <a class="btn btn--ghost" href="/tienda.html">Ir a la tienda</a>
            </div>
        </div>
    `;
    document.querySelectorAll("[data-open-login]").forEach(button => {
        button.addEventListener("click", () => openModal("loginModal"));
    });
}

function updateStats() {
    const totalSpent = state.orders.reduce((sum, order) => sum + Number(order.importeTotal || 0), 0);
    const totalEntries = state.orders.reduce((sum, order) => sum + (order.entradas?.length || 0), 0);
    document.querySelector("#statOrders").textContent = state.orders.length;
    document.querySelector("#statSpent").textContent = `${formatPrice(totalSpent)} €`;
    document.querySelector("#statEntries").textContent = totalEntries;
}

async function loadOrders() {
    if (!getToken()) {
        state.orders = [];
        showLoginPrompt();
        return;
    }

    const grid = document.querySelector("#ordersGrid");
    grid.innerHTML = '<div class="loading-card loading-card--wide">Cargando pedidos...</div>';

    try {
        const orders = await api("/api/gateway/orders");
        state.orders = Array.isArray(orders) ? orders : [];
        updateStats();
        renderOrders();
    } catch (error) {
        if (error.status === 401) {
            setToken(null);
            showLoginPrompt();
            return;
        }
        grid.innerHTML = `
            <div class="empty-state empty-state--wide empty-state--error">
                <strong>No se pudo cargar el historial</strong>
                <p>Comprueba que el gestor de inventario esté disponible y vuelve a intentarlo.</p>
                <button class="btn btn--ghost" type="button" id="retryOrders">Reintentar</button>
            </div>
        `;
        document.querySelector("#retryOrders").addEventListener("click", loadOrders);
    }
}

function filteredOrders() {
    if (state.filter === "todos") return state.orders;
    return state.orders.filter(order => String(order.estado || "").toUpperCase() === state.filter);
}

function renderOrders() {
    const grid = document.querySelector("#ordersGrid");
    const orders = filteredOrders();

    if (!state.orders.length) {
        grid.innerHTML = `
            <div class="empty-state empty-state--wide">
                <strong>Todavía no hay pedidos</strong>
                <p>Cuando finalices una compra en la tienda, aparecerá aquí con sus entradas asociadas.</p>
                <a class="btn btn--primary" href="/tienda.html">Explorar la tienda</a>
            </div>
        `;
        return;
    }

    if (!orders.length) {
        grid.innerHTML = '<div class="empty-state empty-state--wide"><strong>No hay pedidos con ese estado</strong><p>Prueba con otro filtro.</p></div>';
        return;
    }

    grid.innerHTML = orders.map(order => {
        const lines = order.lineas || [];
        const entries = order.entradas || [];
        const canCancel = String(order.estado || "").toUpperCase() === "COMPLETADO";
        return `
            <article class="order-card" data-order-card="${escapeHtml(order.id)}">
                <header class="order-card__head">
                    <div>
                        <p class="order-card__id">${escapeHtml(order.id)}</p>
                        <p class="order-card__date">${formatDateTime(order.fechaCompra)} · ${lines.length} ${lines.length === 1 ? "línea" : "líneas"}${entries.length ? ` · ${entries.length} ${entries.length === 1 ? "entrada" : "entradas"}` : ""}</p>
                    </div>
                    <span class="status-badge ${statusClass(order.estado)}">${escapeHtml(statusLabel(order.estado))}</span>
                </header>

                <div class="order-card__lines">
                    ${lines.slice(0, 4).map(line => `
                        <div class="order-line">
                            <strong>${escapeHtml(line.nombreProducto || line.productoId)}</strong>
                            <span>${line.cantidad} × ${formatPrice(line.precioUnitario)} €</span>
                        </div>
                    `).join("")}
                    ${lines.length > 4 ? `<p class="order-card__more">+ ${lines.length - 4} líneas más</p>` : ""}
                </div>

                <footer class="order-card__foot">
                    <div class="order-card__total">
                        <span>Total</span>
                        <strong>${formatPrice(order.importeTotal)} €</strong>
                    </div>
                    <div class="order-card__actions">
                        <button class="btn btn--ghost btn--small" type="button" data-view-order="${escapeHtml(order.id)}">Ver detalle</button>
                        ${canCancel ? `<button class="btn btn--ghost btn--small btn--danger" type="button" data-cancel-order="${escapeHtml(order.id)}">Solicitar cancelación</button>` : ""}
                    </div>
                </footer>
            </article>
        `;
    }).join("");

    document.querySelectorAll("[data-view-order]").forEach(button => {
        button.addEventListener("click", () => openOrder(button.dataset.viewOrder));
    });
    document.querySelectorAll("[data-cancel-order]").forEach(button => {
        button.addEventListener("click", () => cancelOrder(button.dataset.cancelOrder));
    });
}

async function openOrder(orderId) {
    try {
        const order = await api(`/api/gateway/orders/${encodeURIComponent(orderId)}`);
        renderOrderDetail(order);
        openModal("orderModal");
    } catch (error) {
        toast(error.message || "No se pudo cargar el pedido");
    }
}

function renderOrderDetail(order) {
    const lines = order.lineas || [];
    const entries = order.entradas || [];
    const canCancel = String(order.estado || "").toUpperCase() === "COMPLETADO";
    document.querySelector("#orderModalTitle").textContent = order.id;

    document.querySelector("#orderDetail").innerHTML = `
        <div class="order-detail__head">
            <div>
                <span class="status-badge ${statusClass(order.estado)}">${escapeHtml(statusLabel(order.estado))}</span>
                <p class="order-detail__date">${formatDateTime(order.fechaCompra)}</p>
            </div>
            <div class="order-detail__total">
                <span>Importe total</span>
                <strong>${formatPrice(order.importeTotal)} €</strong>
            </div>
        </div>

        <section class="order-detail__section">
            <h3>Productos</h3>
            <div class="order-detail__lines">
                ${lines.map(line => `
                    <div class="order-detail__line">
                        <div>
                            <strong>${escapeHtml(line.nombreProducto || line.productoId)}</strong>
                            <span>${escapeHtml(line.productoId)}${line.eventoId ? ` · Evento ${escapeHtml(line.eventoId)}` : ""}</span>
                        </div>
                        <span>${line.cantidad} uds.</span>
                        <strong>${formatPrice(line.subtotal)} €</strong>
                    </div>
                `).join("")}
            </div>
        </section>

        ${entries.length ? `
            <section class="order-detail__section">
                <h3>Entradas y pases</h3>
                <p class="order-detail__hint">Cada unidad de entrada o pase genera un título independiente. Puedes solicitar la cancelación de una entrada concreta.</p>
                <div class="entry-list">
                    ${entries.map(entry => `
                        <article class="entry-item">
                            <div>
                                <strong>${escapeHtml(entry.id)}</strong>
                                <span>${escapeHtml(lines.find(line => line.productoId === entry.productoId)?.nombreProducto || entry.productoId)}</span>
                            </div>
                            <div class="entry-item__actions">
                                <span class="status-badge ${statusClass(entry.estado)}">${escapeHtml(statusLabel(entry.estado))}</span>
                                ${String(entry.estado || "").toUpperCase() === "VALIDA"
                                    ? `<button class="btn btn--ghost btn--small btn--danger" type="button" data-cancel-entry="${escapeHtml(entry.id)}" data-order="${escapeHtml(order.id)}">Cancelar</button>`
                                    : ""}
                            </div>
                        </article>
                    `).join("")}
                </div>
            </section>
        ` : ""}

        ${canCancel ? `
            <div class="order-detail__actions">
                <button class="btn btn--ghost btn--danger btn--full" type="button" data-cancel-order="${escapeHtml(order.id)}">Solicitar cancelación del pedido</button>
                <p>La cancelación queda registrada como solicitud. El gestor de inventario actualizará el estado del pedido y de sus entradas.</p>
            </div>
        ` : ""}
    `;

    const cancelButton = document.querySelector("#orderDetail [data-cancel-order]");
    if (cancelButton) {
        cancelButton.addEventListener("click", () => cancelOrder(order.id));
    }
    document.querySelectorAll("#orderDetail [data-cancel-entry]").forEach(button => {
        button.addEventListener("click", () => cancelEntry(button.dataset.cancelEntry, button.dataset.order));
    });
}

async function cancelOrder(orderId) {
    if (!window.confirm("¿Quieres solicitar la cancelación de este pedido?")) return;
    try {
        const order = await api(`/api/gateway/orders/${encodeURIComponent(orderId)}/cancel`, { method: "POST" });
        toast("Solicitud de cancelación registrada");
        renderOrderDetail(order);
        loadOrders();
    } catch (error) {
        toast(error.message || "No se pudo solicitar la cancelación");
    }
}

async function cancelEntry(entryId, orderId) {
    if (!window.confirm("¿Quieres solicitar la cancelación de esta entrada?")) return;
    try {
        await api(`/api/gateway/entries/${encodeURIComponent(entryId)}/cancel`, { method: "POST" });
        toast("Solicitud de cancelación de la entrada registrada");
        await openOrder(orderId);
        loadOrders();
    } catch (error) {
        toast(error.message || "No se pudo cancelar la entrada");
    }
}

function initEvents() {
    document.querySelector("#orderFilters").addEventListener("click", event => {
        const button = event.target.closest("[data-order-filter]");
        if (!button) return;
        state.filter = button.dataset.orderFilter;
        document.querySelectorAll("#orderFilters [data-order-filter]").forEach(candidate => {
            candidate.classList.toggle("active", candidate === button);
        });
        renderOrders();
    });

    document.querySelector("#refreshOrders").addEventListener("click", loadOrders);
    document.querySelector("#cartButton").addEventListener("click", () => {
        window.location.href = "/tienda.html";
    });

    document.querySelector("#logoutButton").addEventListener("click", () => {
        setToken(null);
        toast("Sesión cerrada");
        state.orders = [];
        showLoginPrompt();
    });

    initModalCloseButtons();
    initHeader();
    initLoginModal(loadOrders);
}

initEvents();
loadOrders();
