/*
 * Utilidades compartidas por la tienda y la página de pedidos.
 * Centraliza el acceso al carrito, la autenticación y el formato de datos
 * del Gestor de Inventario y Pedidos.
 */
(function () {
    const CART_KEY = "subsonic-cart";
    const TOKEN_KEY = "subsonic-token";

    const TYPE_LABELS = {
        entrada: "Entrada",
        pase: "Pase",
        merchandising: "Merchandising"
    };

    const STATUS_LABELS = {
        COMPLETADO: "Completado",
        CANCELACION_SOLICITADA: "Cancelación solicitada",
        CANCELADO: "Cancelado",
        VALIDA: "Válida",
        CANCELADA: "Cancelada"
    };

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }

    function formatPrice(value) {
        return Number(value || 0).toLocaleString("es-ES", {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2
        });
    }

    function formatDate(value) {
        if (!value) return "Fecha por confirmar";
        return new Intl.DateTimeFormat("es-ES", {
            day: "numeric",
            month: "short",
            year: "numeric"
        }).format(new Date(value));
    }

    function formatDateTime(value) {
        if (!value) return "Fecha por confirmar";
        return new Intl.DateTimeFormat("es-ES", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }).format(new Date(value));
    }

    function typeLabel(type) {
        return TYPE_LABELS[String(type || "").toLowerCase()] || "Producto";
    }

    function statusLabel(status) {
        return STATUS_LABELS[String(status || "").toUpperCase()] || String(status || "Sin estado");
    }

    function statusClass(status) {
        const normalized = String(status || "").toUpperCase();
        if (normalized === "COMPLETADO" || normalized === "VALIDA") return "status-badge--ok";
        if (normalized === "CANCELACION_SOLICITADA") return "status-badge--pending";
        return "status-badge--muted";
    }

    function getToken() {
        const token = localStorage.getItem(TOKEN_KEY);
        return token && token !== "undefined" && token !== "null" ? token : null;
    }

    function setToken(token) {
        if (token) localStorage.setItem(TOKEN_KEY, token);
        else localStorage.removeItem(TOKEN_KEY);
        updateAccountLink();
    }

    function decodeJwt(token) {
        try {
            const payload = token.split(".")[1];
            if (!payload) return null;
            const normalized = payload.replaceAll("-", "+").replaceAll("_", "/");
            const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
            return JSON.parse(atob(padded));
        } catch (error) {
            return null;
        }
    }

    function currentUserId() {
        const token = getToken();
        if (!token) return null;
        const payload = decodeJwt(token);
        return payload?.sub || token;
    }

    function authHeaders(extra = {}) {
        const headers = new Headers(extra);
        const token = getToken();
        if (token) {
            headers.set("Authorization", `Bearer ${token}`);
            const userId = currentUserId();
            if (userId) headers.set("X-User-Id", userId);
        }
        return headers;
    }

    async function api(path, options = {}) {
        const headers = options.auth === false ? new Headers(options.headers || {}) : authHeaders(options.headers || {});
        if (options.body && !headers.has("Content-Type")) {
            headers.set("Content-Type", "application/json");
        }

        const response = await fetch(path, { ...options, headers });
        const text = await response.text();
        let data = null;
        if (text) {
            try {
                data = JSON.parse(text);
            } catch (error) {
                data = text;
            }
        }

        if (!response.ok) {
            const message = data?.mensaje || data?.message || data?.detail || data?.error || `Error ${response.status}`;
            const error = new Error(message);
            error.status = response.status;
            error.data = data;
            throw error;
        }
        return data;
    }

    function readCart() {
        try {
            const raw = JSON.parse(localStorage.getItem(CART_KEY) || "[]");
            if (!Array.isArray(raw)) return [];
            return raw.map(normalizeCartItem).filter(item => item.productId || item.passId);
        } catch (error) {
            return [];
        }
    }

    function normalizeCartItem(item) {
        return {
            ...item,
            productId: item.productId || null,
            passId: item.passId || null,
            name: item.name || item.nombre || "Producto",
            price: Number(item.price ?? item.precio ?? 0),
            quantity: Math.max(1, Number(item.quantity ?? item.cantidad ?? 1)),
            stock: Number.isFinite(Number(item.stock)) ? Number(item.stock) : null
        };
    }

    function writeCart(cart) {
        localStorage.setItem(CART_KEY, JSON.stringify(cart.map(normalizeCartItem)));
        updateCartBadge();
    }

    function cartCount() {
        return readCart().reduce((sum, item) => sum + item.quantity, 0);
    }

    function updateCartBadge() {
        document.querySelectorAll("#cartCount").forEach(element => {
            element.textContent = cartCount();
        });
    }

    function updateAccountLink() {
        document.querySelectorAll("[data-account-link]").forEach(element => {
            element.href = getToken() ? "/pedidos.html" : "/cuenta";
            element.title = getToken() ? "Mis pedidos" : "Iniciar sesión";
        });
    }

    function toast(message) {
        const element = document.querySelector("#toast");
        if (!element) return;
        element.textContent = message;
        element.classList.add("toast--visible");
        window.setTimeout(() => element.classList.remove("toast--visible"), 2800);
    }

    function openModal(id) {
        const modal = document.querySelector(`#${id}`);
        if (!modal) return;
        modal.classList.add("modal--open");
        modal.setAttribute("aria-hidden", "false");
    }

    function closeModal(id) {
        const modal = document.querySelector(`#${id}`);
        if (!modal) return;
        modal.classList.remove("modal--open");
        modal.setAttribute("aria-hidden", "true");
    }

    function initModalCloseButtons() {
        document.querySelectorAll("[data-close-modal]").forEach(button => {
            button.addEventListener("click", () => closeModal(button.dataset.closeModal));
        });
    }

    function initLoginModal(onSuccess) {
        const form = document.querySelector("#loginForm");
        if (!form) return;

        const toggle = document.querySelector("#togglePassword");
        if (toggle) {
            toggle.addEventListener("click", () => {
                const input = document.querySelector("#loginPassword");
                const image = toggle.querySelector("img");
                input.type = input.type === "password" ? "text" : "password";
                if (image) {
                    image.src = input.type === "password" ? "images/OjoCerrado.png" : "images/OjoAbierto.png";
                }
            });
        }

        form.addEventListener("submit", async event => {
            event.preventDefault();
            const message = document.querySelector("#loginMessage");
            message.textContent = "Conectando...";
            try {
                const result = await api("/api/gateway/auth/login", {
                    method: "POST",
                    auth: false,
                    body: JSON.stringify({
                        correo: document.querySelector("#loginEmail").value,
                        password: document.querySelector("#loginPassword").value
                    })
                });
                setToken(result.accessToken || result.access_token || result.token);
                message.textContent = "Sesión iniciada";
                toast("Sesión iniciada correctamente");
                window.setTimeout(() => closeModal("loginModal"), 450);
                if (typeof onSuccess === "function") onSuccess();
            } catch (error) {
                message.textContent = error.message;
            }
        });
    }

    function initHeader() {
        updateCartBadge();
        updateAccountLink();
        document.querySelectorAll("[data-open-login]").forEach(button => {
            button.addEventListener("click", () => openModal("loginModal"));
        });
    }

    window.Subsonic = {
        CART_KEY,
        TOKEN_KEY,
        escapeHtml,
        formatPrice,
        formatDate,
        formatDateTime,
        typeLabel,
        statusLabel,
        statusClass,
        getToken,
        setToken,
        currentUserId,
        api,
        readCart,
        writeCart,
        cartCount,
        updateCartBadge,
        updateAccountLink,
        toast,
        openModal,
        closeModal,
        initModalCloseButtons,
        initLoginModal,
        initHeader
    };
})();
