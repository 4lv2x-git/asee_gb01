const grid = document.querySelector("#festivalGrid");
const searchForm = document.querySelector("#festivalSearch");
const detail = document.querySelector("#festivalDetail");
const cartKey = "subsonic-cart";

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;");
}

function formatDate(value) {
    if (!value) return "Fecha por confirmar";
    return new Intl.DateTimeFormat("es-ES", {
        day: "numeric",
        month: "short",
        year: "numeric"
    }).format(new Date(value));
}

async function loadFestivals(query = "") {
    grid.innerHTML = '<div class="loading-card loading-card--wide">Cargando festivales...</div>';
    try {
        const params = new URLSearchParams({ page: "1", limit: "100", estado: "publicado" });
        if (query) params.set("nombre", query);
        const response = await fetch(`/api/gateway/events?${params}`);
        if (!response.ok) throw new Error("No se pudo cargar el catálogo");
        const result = await response.json();
        const festivals = result.data || [];

        const city = document.querySelector("#cityQuery")?.value.trim().toLowerCase() || "";
        const dateStart = document.querySelector("#dateStart")?.value || "";
        const dateEnd = document.querySelector("#dateEnd")?.value || "";
        const filtered = festivals.filter(festival => {
            const nameMatch = !query ||
                festival.nombre?.toLowerCase().includes(query.toLowerCase()) ||
                festival.descripcion?.toLowerCase().includes(query.toLowerCase());
            const cityMatch = !city ||
                (festival.ubicacion?.ciudad || "").toLowerCase().includes(city);
            const start = festival.fechaInicio ? festival.fechaInicio.slice(0, 10) : "";
            const dateMatch = (!dateStart || start >= dateStart) && (!dateEnd || start <= dateEnd);
            return nameMatch && cityMatch && dateMatch;
        });

        if (!filtered.length) {
            grid.innerHTML = '<div class="empty-state empty-state--wide"><strong>No hay resultados</strong><p>Prueba con otro nombre o ciudad.</p></div>';
            return;
        }

        grid.innerHTML = filtered.map(festival => `
            <article class="festival-card" id="festival-${festival.id}">
                <div class="festival-card__image">
                    <img src="${escapeHtml(festival.imagenUrl || "/images/riverland.png")}" alt="${escapeHtml(festival.nombre)}">
                    <span>Publicado</span>
                </div>
                <div class="festival-card__body">
                    <p>${formatDate(festival.fechaInicio)} · ${escapeHtml(festival.ubicacion?.ciudad || "Ubicación por confirmar")}</p>
                    <h3>${escapeHtml(festival.nombre)}</h3>
                    <p class="festival-card__description">${escapeHtml(festival.descripcion || "")}</p>
                    <button class="btn btn--ghost btn--full" type="button" data-festival="${festival.id}">Ver festival</button>
                </div>
            </article>
        `).join("");

        document.querySelectorAll("[data-festival]").forEach(button => {
            button.addEventListener("click", () => openFestival(button.dataset.festival));
        });

        const selectedId = new URLSearchParams(location.search).get("id");
        if (selectedId) openFestival(selectedId);
    } catch (error) {
        grid.innerHTML = `
            <div class="empty-state empty-state--wide empty-state--error">
                <strong>No se pudo conectar con el gestor de festivales</strong>
                <p>Comprueba que Docker esté iniciado y que la API de festivales responda en el puerto 8000.</p>
            </div>
        `;
    }
}

async function openFestival(id) {
    detail.hidden = true;
    try {
        const [festivalResponse, passesResponse, scheduleResponse] = await Promise.all([
            fetch(`/api/gateway/events/${id}`),
            fetch(`/api/gateway/events/${id}/passes`),
            fetch(`/api/gateway/events/${id}/schedule`)
        ]);
        if (!festivalResponse.ok) throw new Error("Festival no encontrado");
        const [festival, passes, schedule] = await Promise.all([
            festivalResponse.json(),
            passesResponse.json(),
            scheduleResponse.ok ? scheduleResponse.json() : { data: [] }
        ]);

        detail.hidden = false;
        document.querySelector("#festivalDetailHero").innerHTML =
            `<img src="${escapeHtml(festival.imagenUrl || "/images/riverland.png")}" alt="${escapeHtml(festival.nombre)}">`;
        document.querySelector("#festivalTitle").textContent = festival.nombre;
        document.querySelector("#festivalLocation").textContent =
            `${formatDate(festival.fechaInicio)} · ${festival.ubicacion?.ciudad || "Ubicación por confirmar"}`;
        document.querySelector("#festivalDescription").textContent = festival.descripcion || "";
        document.querySelector("#festivalMeta").innerHTML = `
            <article><span>Fechas</span><strong>${formatDate(festival.fechaInicio)} — ${formatDate(festival.fechaFin)}</strong></article>
            <article><span>Ubicación</span><strong>${escapeHtml(festival.ubicacion?.nombre || festival.ubicacion?.ciudad || "Por confirmar")}</strong></article>
            <article><span>Aforo</span><strong>${festival.aforo ? `${Number(festival.aforo).toLocaleString("es-ES")} personas` : "Por confirmar"}</strong></article>
            <article><span>Estado</span><strong>${escapeHtml(festival.estado || "Publicado")}</strong></article>
        `;

        document.querySelector("#festivalPasses").innerHTML =
            (passes.data || []).map((pass, index) => `
                <article class="ticket pass-card ${index === 0 ? "pass-card--selected" : ""}">
                    <div class="ticket__top">
                        <span>${escapeHtml(pass.tipo || "Pase")}</span>
                        <h3>${escapeHtml(pass.nombre)}</h3>
                        <p class="ticket__price"><strong>${Number(pass.precio).toLocaleString("es-ES")}</strong><span>€</span></p>
                    </div>
                    <ul class="ticket__features">
                        ${(pass.serviciosIncluidos || []).map(service => `<li>${escapeHtml(service)}</li>`).join("")}
                    </ul>
                    <div class="pass-quantity">
                        <button type="button" data-pass-minus="${escapeHtml(pass.id)}" aria-label="Reducir entradas">−</button>
                        <strong data-pass-quantity="${escapeHtml(pass.id)}">1</strong>
                        <button type="button" data-pass-plus="${escapeHtml(pass.id)}" aria-label="Aumentar entradas">+</button>
                    </div>
                    <button class="btn ${index === 0 ? "btn--primary" : "btn--ghost"} btn--full" type="button"
                        data-pass="${escapeHtml(pass.id)}" data-name="${escapeHtml(pass.nombre)}"
                        data-price="${pass.precio}" ${pass.stock <= 0 ? "disabled" : ""}>
                        Añadir al carrito
                    </button>
                </article>
            `).join("") || '<div class="empty-state empty-state--wide">No hay pases disponibles.</div>';

        document.querySelectorAll("[data-pass-plus]").forEach(button => {
            button.addEventListener("click", () => {
                const quantity = document.querySelector(`[data-pass-quantity="${button.dataset.passPlus}"]`);
                quantity.textContent = String(Math.min(10, Number(quantity.textContent) + 1));
            });
        });
        document.querySelectorAll("[data-pass-minus]").forEach(button => {
            button.addEventListener("click", () => {
                const quantity = document.querySelector(`[data-pass-minus="${button.dataset.passMinus}"]`)
                    .closest(".pass-card")
                    .querySelector("[data-pass-quantity]");
                quantity.textContent = String(Math.max(1, Number(quantity.textContent) - 1));
            });
        });
        document.querySelectorAll("[data-pass]").forEach(button => {
            button.addEventListener("click", () => addToCart({
                passId: button.dataset.pass,
                eventId: id,
                eventName: festival.nombre,
                name: button.dataset.name,
                price: Number(button.dataset.price),
                quantity: Number(button.closest(".pass-card").querySelector("[data-pass-quantity]").textContent)
            }));
        });

        const lineup = (schedule.data || []).filter((slot, index, items) =>
            items.findIndex(candidate => candidate.artistaId === slot.artistaId) === index
        );
        const artistImages = {
            "Charlotte de Witte": "images/Charlotte_de_Witte.png",
            "Amelie Lens": "images/Amelie_Lens.jpg",
            "Boris Brejcha": "images/Boris_Brejcha.jpg",
            "Adam Beyer": "images/Adam_Beyer.webp",
            "Carl Cox": "images/Carl_Cox.jpg",
            "Nina Kraviz": "images/Nina_Kraviz.jpg"
        };
        document.querySelector("#festivalLineup").innerHTML = lineup.length ? `
            <p class="eyebrow">Lineup</p>
            <div class="lineup-artist-grid">
                ${lineup.map(slot => `
                    <article class="lineup-artist">
                        <img src="${escapeHtml(slot.imagenUrl || artistImages[slot.nombreArtista] || "images/User.png")}" alt="${escapeHtml(slot.nombreArtista)}">
                        <div>
                            <h3>${escapeHtml(slot.nombreArtista || "Artista por confirmar")}</h3>
                            <p>${escapeHtml(slot.escenario || "Escenario principal")}</p>
                        </div>
                    </article>
                `).join("")}
            </div>
        ` : "";

        history.replaceState(null, "", `/festivales.html?id=${encodeURIComponent(id)}`);
        detail.scrollIntoView({ behavior: "smooth" });
    } catch (error) {
        detail.hidden = false;
        detail.innerHTML = '<div class="empty-state empty-state--wide empty-state--error"><strong>Festival no disponible</strong></div>';
    }
}

function addToCart(item) {
    const cart = JSON.parse(localStorage.getItem(cartKey) || "[]");
    const existing = cart.find(entry => entry.passId === item.passId);
    if (existing) existing.quantity += 1;
    else cart.push(item);
    localStorage.setItem(cartKey, JSON.stringify(cart));
    document.querySelector("#cartCount").textContent =
        cart.reduce((sum, entry) => sum + entry.quantity, 0);
    const toast = document.querySelector("#toast");
    toast.textContent = `${item.name} añadido al carrito`;
    toast.classList.add("toast--visible");
    setTimeout(() => toast.classList.remove("toast--visible"), 2500);
}

searchForm.addEventListener("submit", event => {
    event.preventDefault();
    loadFestivals(document.querySelector("#festivalQuery").value.trim());
});

function renderCart() {
    const cart = JSON.parse(localStorage.getItem(cartKey) || "[]");
    document.querySelector("#cartCount").textContent =
        cart.reduce((sum, entry) => sum + entry.quantity, 0);
    document.querySelector("#cartItems").innerHTML = cart.length
        ? cart.map((entry, index) => `
            <article class="cart-item">
                <div><strong>${escapeHtml(entry.name)}</strong><span>${escapeHtml(entry.eventName)}</span></div>
                <div class="cart-item__quantity">
                    <button type="button" data-cart-minus="${index}">−</button>
                    <span>${entry.quantity}</span>
                    <button type="button" data-cart-plus="${index}">+</button>
                </div>
                <strong>${(entry.price * entry.quantity).toLocaleString("es-ES")} €</strong>
            </article>
        `).join("")
        : '<div class="empty-state empty-state--wide">Tu carrito está vacío.</div>';
    document.querySelector("#cartTotal").textContent =
        `${cart.reduce((sum, entry) => sum + entry.price * entry.quantity, 0).toLocaleString("es-ES")} €`;

    document.querySelectorAll("[data-cart-plus]").forEach(button => button.addEventListener("click", () => updateCartItem(Number(button.dataset.cartPlus), 1)));
    document.querySelectorAll("[data-cart-minus]").forEach(button => button.addEventListener("click", () => updateCartItem(Number(button.dataset.cartMinus), -1)));
}

function updateCartItem(index, change) {
    const cart = JSON.parse(localStorage.getItem(cartKey) || "[]");
    cart[index].quantity += change;
    if (cart[index].quantity <= 0) cart.splice(index, 1);
    localStorage.setItem(cartKey, JSON.stringify(cart));
    renderCart();
}

function openModal(id) {
    const modal = document.querySelector(`#${id}`);
    modal.classList.add("modal--open");
    modal.setAttribute("aria-hidden", "false");
}

function closeModal(id) {
    const modal = document.querySelector(`#${id}`);
    modal.classList.remove("modal--open");
    modal.setAttribute("aria-hidden", "true");
}

document.querySelector("#cartButton").addEventListener("click", () => openModal("cartModal"));
document.querySelectorAll("[data-close-cart]").forEach(button => button.addEventListener("click", () => closeModal("cartModal")));
document.querySelectorAll("[data-close-account]").forEach(button => button.addEventListener("click", () => closeModal("accountModal")));
document.querySelector("#checkoutButton").addEventListener("click", () => {
    const toast = document.querySelector("#toast");
    toast.textContent = localStorage.getItem("subsonic-token")
        ? "Preparando el pedido..."
        : "Inicia sesión para finalizar la compra";
    toast.classList.add("toast--visible");
    setTimeout(() => toast.classList.remove("toast--visible"), 2500);
});
document.querySelector("#loginForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const message = document.querySelector("#loginMessage");
    message.textContent = "Conectando...";
    try {
        const response = await fetch("/api/gateway/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ correo: form.email.value, password: form.password.value })
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.detail || "Credenciales incorrectas");
        localStorage.setItem("subsonic-token", result.access_token || result.token);
        message.textContent = "Sesión iniciada";
        setTimeout(() => closeModal("accountModal"), 500);
    } catch (error) {
        message.textContent = error.message;
    }
});

renderCart();
loadFestivals();
