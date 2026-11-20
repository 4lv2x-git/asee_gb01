const storedToken = localStorage.getItem("subsonic-token");

const state = {
    festivals: [],
    selectedFestival: null,
    cart: JSON.parse(localStorage.getItem("subsonic-cart") || "[]"),
    token: storedToken && storedToken !== "undefined" && storedToken !== "null" ? storedToken : null
};

const assets = [
    "images/riverland.png",
    "images/boombastic.jpg",
    "images/zevra.jpg"
];

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

async function api(path) {
    const response = await fetch(path);
    if (!response.ok) {
        throw new Error(`Error ${response.status}`);
    }
    return response.json();
}

async function loadFestivals() {
    const grid = document.querySelector("#festivalGrid");
    grid.innerHTML = '<div class="loading-card">Cargando festivales...</div>';

    try {
        const response = await api("/api/gateway/events?page=1&limit=20&estado=publicado");
        const festivals = response.data || [];
        state.festivals = festivals;
        if (!festivals.length) {
            grid.innerHTML = '<div class="empty-state"><strong>No hay festivales publicados</strong><p>Vuelve a intentarlo cuando el gestor publique el siguiente evento.</p></div>';
            renderGlobe([]);
            return;
        }

        grid.innerHTML = festivals.map((festival, index) => `
            <article class="festival-card" id="festival-${festival.id}">
                <div class="festival-card__image">
                    <img src="${escapeHtml(festival.imagenUrl || assets[index % assets.length])}" alt="${escapeHtml(festival.nombre)}">
                    <span>${escapeHtml(festival.estado || "Publicado")}</span>
                </div>
                <div class="festival-card__body">
                    <p>${formatDate(festival.fechaInicio)} · ${escapeHtml(festival.ubicacion?.ciudad || "Ubicación por confirmar")}</p>
                    <h3>${escapeHtml(festival.nombre)}</h3>
                    <p class="festival-card__description">${escapeHtml(festival.descripcion || "")}<br><strong>Artistas:</strong> ${escapeHtml(["Charlotte de Witte · Amelie Lens", "Carl Cox · Nina Kraviz", "Adam Beyer · Boris Brejcha"][index % 3])}</p>
                    <a class="btn btn--ghost btn--full" href="/festivales.html?id=${encodeURIComponent(festival.id)}">Ver festival</a>
                </div>
            </article>
        `).join("");
        renderGlobe(festivals);
    } catch (error) {
        grid.innerHTML = `
            <div class="empty-state empty-state--error">
                <strong>No se pudo conectar con el catálogo</strong>
                <p>Comprueba que el microservicio de festivales esté iniciado y que el puerto configurado coincida con el gateway.</p>
                <button class="btn btn--ghost" type="button" id="retryFestivals">Reintentar</button>
            </div>
        `;
        document.querySelector("#retryFestivals").addEventListener("click", loadFestivals);
    }
}

async function loadArtists() {
    const grid = document.querySelector("#artistGrid");
    const localArtists = [
        { name: "Charlotte de Witte", genre: "Techno", image: "images/Charlotte_de_Witte.png" },
        { name: "Amelie Lens", genre: "Techno", image: "images/Amelie_Lens.jpg" },
        { name: "Boris Brejcha", genre: "High-Tech Minimal", image: "images/Boris_Brejcha.jpg" },
        { name: "Adam Beyer", genre: "Techno", image: "images/Adam_Beyer.webp" },
        { name: "Carl Cox", genre: "House & Techno", image: "images/Carl_Cox.jpg" },
        { name: "Nina Kraviz", genre: "Techno", image: "images/Nina_Kraviz.jpg" }
    ];
    try {
        const artists = await api("/api/gateway/artists");
        if (!artists.length) {
            grid.innerHTML = localArtists.map(artist => `
                <article class="artist-card">
                    <img src="${artist.image}" alt="${artist.name}">
                    <div><h3>${artist.name}</h3><p>${artist.genre}</p></div>
                </article>
            `).join("");
            return;
        }
        grid.innerHTML = artists.map((artist, index) => {
            const localImages = [
                "images/Adam_Beyer.webp", "images/Amelie_Lens.jpg", "images/Boris_Brejcha.jpg",
                "images/Carl_Cox.jpg", "images/Charlotte_de_Witte.png", "images/Nina_Kraviz.jpg"
            ];
            return `
                <article class="artist-card">
                    <img src="${escapeHtml(artist.imageUrl || localImages[index % localImages.length])}" alt="${escapeHtml(artist.name || artist.nombre)}">
                    <div>
                        <h3>${escapeHtml(artist.name || artist.nombre)}</h3>
                        <p>${escapeHtml(artist.genre || artist.genero || "Live act")}</p>
                    </div>
                </article>
            `;
        }).join("");
    } catch (error) {
        const localArtists = [
            ["Charlotte de Witte", "Techno", "images/Charlotte_de_Witte.png"],
            ["Amelie Lens", "Techno", "images/Amelie_Lens.jpg"],
            ["Boris Brejcha", "High-Tech Minimal", "images/Boris_Brejcha.jpg"],
            ["Adam Beyer", "Techno", "images/Adam_Beyer.webp"],
            ["Carl Cox", "House & Techno", "images/Carl_Cox.jpg"],
            ["Nina Kraviz", "Techno", "images/Nina_Kraviz.jpg"]
        ];
        grid.innerHTML = localArtists.map(([name, genre, image]) => `
            <article class="artist-card">
                <img src="${image}" alt="${name}">
                <div><h3>${name}</h3><p>${genre}</p></div>
            </article>
        `).join("");
    }
}

function formatMoney(value) {
    const amount = Number(value);
    if (!Number.isFinite(amount)) return "—";
    return new Intl.NumberFormat("es-ES", { minimumFractionDigits: 0, maximumFractionDigits: 2 }).format(amount);
}

async function loadGroundsPreview() {
    const grid = document.querySelector("#groundHomeGrid");
    if (!grid) return;
    grid.innerHTML = '<div class="loading-card loading-card--wide">Cargando espacios...</div>';

    try {
        const grounds = await api("/api/gateway/grounds");
        const list = Array.isArray(grounds) ? grounds : [];
        if (!list.length) {
            grid.innerHTML = `
                <div class="empty-state empty-state--wide">
                    <strong>Aún no hay espacios publicados</strong>
                    <p>Cuando el Gestor de Espacios publique recintos aparecerán aquí.</p>
                </div>
            `;
            return;
        }

        const order = { disponible: 0, reservado: 1, ocupado: 2 };
        const priority = ground => order[String(ground.estado || "").toLowerCase()] ?? 3;
        const preview = [...list]
            .sort((a, b) => priority(a) - priority(b) || Number(a.precio) - Number(b.precio))
            .slice(0, 3);

        grid.innerHTML = preview.map(ground => {
            const status = String(ground.estado || "").toLowerCase();
            const services = Array.isArray(ground.servicios_incluidos) ? ground.servicios_incluidos.slice(0, 2) : [];
            return `
                <article class="ground-home-card">
                    <div class="ground-home-card__top">
                        <span class="status-badge ${status === "disponible" ? "status-badge--ok" : status === "reservado" ? "status-badge--pending" : "status-badge--muted"}">
                            ${escapeHtml(ground.estado || "Sin estado")}
                        </span>
                        <span class="ground-home-card__event">Evento ${escapeHtml(ground.evento_id || "—")}</span>
                    </div>
                    <h3>${escapeHtml(ground.tipo || "Espacio")}</h3>
                    <p class="ground-home-card__meta">${escapeHtml(ground.ubicacion || "Ubicación por confirmar")} · ${escapeHtml(ground.dimensiones || "Dimensiones por confirmar")}</p>
                    ${services.length ? `<ul class="ground-services">${services.map(service => `<li class="ground-service">${escapeHtml(service)}</li>`).join("")}</ul>` : ""}
                    <div class="ground-home-card__footer">
                        <strong>${escapeHtml(formatMoney(ground.precio))} €</strong>
                        <a class="btn btn--ghost btn--small" href="/recintos.html?id=${encodeURIComponent(ground.id)}">Ver espacio</a>
                    </div>
                </article>
            `;
        }).join("");
    } catch (error) {
        grid.innerHTML = `
            <div class="empty-state empty-state--wide empty-state--error">
                <strong>No se pudo cargar el mapa de espacios</strong>
                <p>El Gestor de Espacios no responde. Puedes consultar el catálogo completo en la página de recintos.</p>
                <a class="btn btn--ghost" href="/recintos.html">Ir a recintos</a>
            </div>
        `;
    }
}

function renderGlobe(festivals) {
    const globe = document.querySelector("#festivalGlobe");
    if (!window.Globe) return;

    const points = festivals
        .filter(festival => Number.isFinite(Number(festival.ubicacion?.latitud)) && Number.isFinite(Number(festival.ubicacion?.longitud)))
        .map(festival => ({
            lat: Number(festival.ubicacion.latitud),
            lng: Number(festival.ubicacion.longitud),
            name: festival.nombre,
            id: festival.id,
            image: festival.imagenUrl || "images/riverland.png"
        }));

    const pinElement = point => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "festival-globe-pin";
        button.title = point.name;
        button.setAttribute("aria-label", `Ver ${point.name}`);
        button.innerHTML = `<img src="${escapeHtml(point.image)}" alt=""><span class="festival-globe-pin-line"></span><span class="festival-globe-pin-dot"></span>`;
        button.addEventListener("pointerdown", event => event.stopPropagation());
        button.addEventListener("mousedown", event => event.stopPropagation());
        button.addEventListener("click", () => {
            location.href = `/festivales.html?id=${encodeURIComponent(point.id)}`;
        });
        return button;
    };

    globe.innerHTML = "";
    const globeInstance = window.Globe()(globe)
        .width(Math.max(320, globe.clientWidth || 320))
        .height(Math.max(360, globe.clientHeight || 360))
        .backgroundColor("rgba(0,0,0,0)")
        .globeImageUrl("https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg")
        .bumpImageUrl("https://unpkg.com/three-globe/example/img/earth-topology.png")
        .showAtmosphere(true)
        .atmosphereColor("#55e6ff")
        .atmosphereAltitude(0.15)
        .htmlElementsData(points)
        .htmlLat(point => point.lat)
        .htmlLng(point => point.lng)
        .htmlAltitude(() => 0.08)
        .htmlElement(pinElement);

    const controls = globeInstance.controls();
    controls.autoRotate = true;
    controls.autoRotateSpeed = 0.45;
    controls.enablePan = false;
    controls.minDistance = 180;
    controls.maxDistance = 420;
    globeInstance.pointOfView({ lat: 30, lng: -35, altitude: 2.1 }, 0);
}

function addToCart(item) {
    const itemKey = item.productId || item.passId;
    const existing = state.cart.find(product => (product.productId || product.passId) === itemKey);
    if (existing) existing.quantity += 1;
    else state.cart.push(item);
    persistCart();
    openModal("cartModal");
    showToast(`${item.name} añadido al carrito`);
}

function currentUserId() {
    if (!state.token) return null;
    try {
        const payload = state.token.split(".")[1];
        if (!payload) return state.token;
        const normalized = payload.replaceAll("-", "+").replaceAll("_", "/");
        const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
        return JSON.parse(atob(padded)).sub || state.token;
    } catch (error) {
        return state.token;
    }
}

function updateAccountLink() {
    const link = document.querySelector("#accountLink");
    if (link) link.href = state.token ? "/pedidos.html" : "/login.html";
}

function persistCart() {
    localStorage.setItem("subsonic-cart", JSON.stringify(state.cart));
    renderCart();
}

function renderCart() {
    const count = state.cart.reduce((sum, item) => sum + item.quantity, 0);
    document.querySelector("#cartCount").textContent = count;
    const items = document.querySelector("#cartItems");
    if (!state.cart.length) {
        items.innerHTML = '<div class="empty-state empty-state--wide">Tu carrito está vacío.</div>';
    } else {
        items.innerHTML = state.cart.map((item, index) => `
            <article class="cart-item">
                <div>
                    <strong>${escapeHtml(item.name)}</strong>
                    <span>${escapeHtml(item.eventName || item.tipo || "Subsonic Store")}</span>
                </div>
                <div class="cart-item__quantity">
                    <button type="button" data-cart-action="minus" data-index="${index}" aria-label="Reducir cantidad">−</button>
                    <span>${item.quantity}</span>
                    <button type="button" data-cart-action="plus" data-index="${index}" aria-label="Aumentar cantidad">+</button>
                </div>
                <strong>${(item.price * item.quantity).toLocaleString("es-ES")} €</strong>
            </article>
        `).join("");
    }

    document.querySelector("#cartTotal").textContent =
        `${state.cart.reduce((sum, item) => sum + item.price * item.quantity, 0).toLocaleString("es-ES")} €`;

    document.querySelectorAll("[data-cart-action]").forEach(button => {
        button.addEventListener("click", () => {
            const index = Number(button.dataset.index);
            if (button.dataset.cartAction === "plus") state.cart[index].quantity += 1;
            else state.cart[index].quantity -= 1;
            if (state.cart[index].quantity <= 0) state.cart.splice(index, 1);
            persistCart();
        });
    });
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

function showToast(message) {
    const toast = document.querySelector("#toast");
    toast.textContent = message;
    toast.classList.add("toast--visible");
    window.setTimeout(() => toast.classList.remove("toast--visible"), 2600);
}

async function checkout() {
    if (!state.cart.length) return showToast("El carrito está vacío");
    if (!state.token) {
        closeModal("cartModal");
        openModal("accountModal");
        showToast("Inicia sesión para finalizar la compra");
        return;
    }

    const unsupported = state.cart.filter(item => !item.productId && item.passId);
    if (unsupported.length) {
        closeModal("cartModal");
        showToast("Los pases del cartel se compran desde la tienda oficial");
        window.setTimeout(() => location.href = "/tienda.html", 900);
        return;
    }

    const headers = {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${state.token}`
    };
    const userId = currentUserId();
    if (userId) headers["X-User-Id"] = userId;

    try {
        const response = await fetch("/api/gateway/orders", {
            method: "POST",
            headers,
            body: JSON.stringify({
                lineas: state.cart.map(item => {
                    const line = {
                        productoId: item.productId || item.passId,
                        cantidad: item.quantity
                    };
                    if (item.eventId) line.eventoId = item.eventId;
                    if (item.paseId) line.paseId = item.paseId;
                    return line;
                })
            })
        });
        const result = await response.json().catch(() => null);
        if (!response.ok) {
            if (response.status === 401) {
                state.token = null;
                localStorage.removeItem("subsonic-token");
                closeModal("cartModal");
                openModal("accountModal");
            }
            throw new Error(result?.mensaje || result?.message || result?.detail || `Error ${response.status}`);
        }
        state.cart = [];
        persistCart();
        closeModal("cartModal");
        showToast(`Pedido ${result?.id || ""} registrado correctamente`);
    } catch (error) {
        showToast(error.message || "No se pudo registrar el pedido");
    }
}

async function login(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const message = document.querySelector("#loginMessage");
    message.textContent = "Conectando...";

    try {
        const response = await fetch("/api/gateway/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                correo: form.email.value,
                password: form.password.value
            })
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.detail || result.message || "Credenciales incorrectas");
        state.token = result.accessToken || result.access_token || result.token;
        localStorage.setItem("subsonic-token", state.token);
        updateAccountLink();
        message.textContent = "Sesión iniciada";
        window.setTimeout(() => closeModal("accountModal"), 500);
    } catch (error) {
        message.textContent = error.message;
    }
}

document.querySelector("#cartButton").addEventListener("click", () => openModal("cartModal"));
document.querySelector("#checkoutButton").addEventListener("click", checkout);
document.querySelector("#loginForm").addEventListener("submit", login);
document.querySelectorAll("[data-close-modal]").forEach(button => {
    button.addEventListener("click", () => closeModal(button.dataset.closeModal));
});
document.querySelector("#togglePassword").addEventListener("click", event => {
    const password = document.querySelector("#loginPassword");
    const image = event.currentTarget.querySelector("img");
    password.type = password.type === "password" ? "text" : "password";
    image.src = password.type === "password" ? "images/OjoCerrado.png" : "images/OjoAbierto.png";
});

renderCart();
updateAccountLink();
loadFestivals();
loadArtists();
loadGroundsPreview();

if (location.hash === "#cart") openModal("cartModal");
if (location.hash === "#account") openModal("accountModal");
