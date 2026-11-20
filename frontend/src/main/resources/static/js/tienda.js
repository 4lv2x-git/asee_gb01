const {
    escapeHtml,
    formatPrice,
    typeLabel,
    api,
    readCart,
    writeCart,
    toast,
    openModal,
    closeModal,
    getToken,
    initModalCloseButtons,
    initLoginModal,
    initHeader
} = window.Subsonic;

const state = {
    products: [],
    category: "todos",
    query: "",
    sort: "relevancia",
    cart: readCart(),
    quantities: {},
    pendingCheckout: false
};

const productImages = {
    entrada: ["images/riverland.png", "images/boombastic.jpg", "images/zevra.jpg"],
    pase: ["images/boombastic.jpg", "images/zevra.jpg", "images/riverland.png"],
    merchandising: ["images/logoPrincipal.png"]
};

function productImage(product, index) {
    if (product.imagenUrl) return product.imagenUrl;
    const images = productImages[String(product.tipo || "").toLowerCase()] || productImages.entrada;
    return images[index % images.length];
}

function isLogoProduct(product) {
    return String(product.tipo || "").toLowerCase() === "merchandising";
}

function stockInfo(stock) {
    const value = Number(stock || 0);
    if (value <= 0) return { text: "Agotado", className: "product-card__stock--out" };
    if (value <= 10) return { text: `Últimas ${value}`, className: "product-card__stock--low" };
    return { text: `${value} disponibles`, className: "" };
}

function getQuantity(productId) {
    return state.quantities[productId] || 1;
}

function setQuantity(productId, quantity, max) {
    state.quantities[productId] = Math.max(1, Math.min(Number(quantity), Math.max(1, Number(max || 1))));
    const element = document.querySelector(`[data-qty="${CSS.escape(productId)}"]`);
    if (element) element.textContent = state.quantities[productId];
}

function updateStats() {
    const totalStock = state.products.reduce((sum, product) => sum + Number(product.stockDisponible || 0), 0);
    const types = new Set(state.products.map(product => String(product.tipo || "").toLowerCase()));
    document.querySelector("#statProducts").textContent = state.products.length;
    document.querySelector("#statStock").textContent = totalStock.toLocaleString("es-ES");
    document.querySelector("#statTypes").textContent = types.size;
}

async function loadProducts() {
    const grid = document.querySelector("#productGrid");
    grid.innerHTML = '<div class="loading-card loading-card--wide">Cargando catálogo...</div>';

    try {
        const products = await api("/api/gateway/products");
        state.products = Array.isArray(products) ? products : [];
        state.products.forEach(product => {
            state.quantities[product.id] = 1;
        });
        updateStats();
        renderProducts();

        const selectedId = new URLSearchParams(location.search).get("producto");
        if (selectedId) openProductDetail(selectedId);
    } catch (error) {
        document.querySelector("#storeCount").textContent = "Catálogo no disponible";
        grid.innerHTML = `
            <div class="empty-state empty-state--wide empty-state--error">
                <strong>No se pudo conectar con el gestor de inventario</strong>
                <p>Comprueba que el microservicio de inventario esté iniciado en el puerto 8081 y que el gateway pueda acceder a él.</p>
                <button class="btn btn--ghost" type="button" id="retryProducts">Reintentar</button>
            </div>
        `;
        document.querySelector("#retryProducts").addEventListener("click", loadProducts);
    }
}

function filteredProducts() {
    const query = state.query.trim().toLowerCase();
    let products = state.products.filter(product => {
        const typeMatch = state.category === "todos" ||
            String(product.tipo || "").toLowerCase() === state.category;
        const text = `${product.nombre || ""} ${product.descripcion || ""}`.toLowerCase();
        return typeMatch && (!query || text.includes(query));
    });

    products = [...products].sort((a, b) => {
        if (state.sort === "precio-asc") return Number(a.precio) - Number(b.precio);
        if (state.sort === "precio-desc") return Number(b.precio) - Number(a.precio);
        if (state.sort === "nombre") return String(a.nombre || "").localeCompare(String(b.nombre || ""), "es");
        return Number(b.stockDisponible || 0) - Number(a.stockDisponible || 0);
    });
    return products;
}

function renderProducts() {
    const grid = document.querySelector("#productGrid");
    const count = document.querySelector("#storeCount");
    const products = filteredProducts();

    count.textContent = products.length
        ? `${products.length} ${products.length === 1 ? "producto" : "productos"} encontrados`
        : "Sin resultados para los filtros seleccionados";

    if (!state.products.length) {
        grid.innerHTML = '<div class="empty-state empty-state--wide"><strong>Catálogo vacío</strong><p>El gestor de inventario todavía no ha publicado productos.</p></div>';
        return;
    }

    if (!products.length) {
        grid.innerHTML = '<div class="empty-state empty-state--wide"><strong>No hay resultados</strong><p>Prueba con otra búsqueda o cambia de categoría.</p></div>';
        return;
    }

    grid.innerHTML = products.map((product, index) => {
        const stock = Number(product.stockDisponible || 0);
        const info = stockInfo(stock);
        const quantity = getQuantity(product.id);
        const out = stock <= 0;
        return `
            <article class="product-card ${out ? "product-card--out" : ""}" data-product-card="${escapeHtml(product.id)}">
                <div class="product-card__media ${isLogoProduct(product) ? "product-card__media--logo" : ""}"
                     data-view-product="${escapeHtml(product.id)}" role="button" tabindex="0"
                     aria-label="Ver detalle de ${escapeHtml(product.nombre)}">
                    <img src="${escapeHtml(productImage(product, index))}" alt="${escapeHtml(product.nombre)}">
                    <span class="product-card__type">${escapeHtml(typeLabel(product.tipo))}</span>
                    <span class="product-card__stock ${info.className}">${escapeHtml(info.text)}</span>
                </div>
                <div class="product-card__body">
                    <p class="product-card__eyebrow">${escapeHtml(typeLabel(product.tipo))} · Subsonic Store</p>
                    <h3><button class="product-card__title-button" type="button" data-view-product="${escapeHtml(product.id)}">${escapeHtml(product.nombre)}</button></h3>
                    <p class="product-card__description">${escapeHtml(product.descripcion || "Producto oficial de Subsonic Festival.")}</p>
                    <div class="product-card__footer">
                        <p class="product-card__price"><strong>${formatPrice(product.precio)}</strong><span>€</span></p>
                        <div class="product-quantity" aria-label="Cantidad de ${escapeHtml(product.nombre)}">
                            <button type="button" data-qty-minus="${escapeHtml(product.id)}" aria-label="Reducir cantidad" ${out ? "disabled" : ""}>−</button>
                            <strong data-qty="${escapeHtml(product.id)}">${quantity}</strong>
                            <button type="button" data-qty-plus="${escapeHtml(product.id)}" aria-label="Aumentar cantidad" ${out ? "disabled" : ""}>+</button>
                        </div>
                    </div>
                    <div class="product-card__actions">
                        <button class="btn btn--ghost btn--small" type="button" data-view-product="${escapeHtml(product.id)}">Ver detalle</button>
                        <button class="btn btn--primary btn--small" type="button"
                                data-add-product="${escapeHtml(product.id)}" ${out ? "disabled" : ""}>
                            ${out ? "Agotado" : "Añadir"}
                        </button>
                    </div>
                </div>
            </article>
        `;
    }).join("");

    document.querySelectorAll("[data-view-product]").forEach(element => {
        element.addEventListener("click", () => openProductDetail(element.dataset.viewProduct));
        if (element.getAttribute("role") === "button") {
            element.addEventListener("keydown", event => {
                if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    openProductDetail(element.dataset.viewProduct);
                }
            });
        }
    });

    document.querySelectorAll("[data-qty-plus]").forEach(button => {
        button.addEventListener("click", () => {
            const product = state.products.find(item => item.id === button.dataset.qtyPlus);
            setQuantity(product.id, getQuantity(product.id) + 1, product.stockDisponible);
        });
    });

    document.querySelectorAll("[data-qty-minus]").forEach(button => {
        button.addEventListener("click", () => {
            const product = state.products.find(item => item.id === button.dataset.qtyMinus);
            setQuantity(product.id, getQuantity(product.id) - 1, product.stockDisponible);
        });
    });

    document.querySelectorAll("[data-add-product]").forEach(button => {
        button.addEventListener("click", () => {
            const product = state.products.find(item => item.id === button.dataset.addProduct);
            addProductToCart(product, getQuantity(product.id));
        });
    });
}

async function openProductDetail(productId) {
    try {
        const product = await api(`/api/gateway/products/${encodeURIComponent(productId)}`);
        renderProductDetail(product);
        openModal("productModal");
    } catch (error) {
        toast(error.message || "No se pudo cargar el producto");
    }
}

function renderProductDetail(product) {
    const stock = Number(product.stockDisponible || 0);
    const info = stockInfo(stock);
    const quantity = getQuantity(product.id);
    document.querySelector("#productModalTitle").textContent = product.nombre;

    document.querySelector("#productDetail").innerHTML = `
        <div class="product-detail__media ${isLogoProduct(product) ? "product-detail__media--logo" : ""}">
            <img src="${escapeHtml(productImage(product, 0))}" alt="${escapeHtml(product.nombre)}">
            <span class="product-card__type">${escapeHtml(typeLabel(product.tipo))}</span>
            <span class="product-card__stock ${info.className}">${escapeHtml(info.text)}</span>
        </div>
        <p class="product-detail__eyebrow">${escapeHtml(typeLabel(product.tipo))} · ${escapeHtml(product.id)}</p>
        <p class="product-detail__description">${escapeHtml(product.descripcion || "Producto oficial de Subsonic Festival.")}</p>
        <div class="product-detail__row">
            <div class="product-detail__price">
                <span>Precio unitario</span>
                <strong>${formatPrice(product.precio)} €</strong>
            </div>
            <div class="product-detail__stock">
                <span>Stock disponible</span>
                <strong>${stock.toLocaleString("es-ES")} uds.</strong>
            </div>
        </div>
        <div class="product-detail__actions">
            <div class="product-quantity" aria-label="Cantidad">
                <button type="button" data-detail-minus aria-label="Reducir cantidad" ${stock <= 0 ? "disabled" : ""}>−</button>
                <strong data-detail-qty>${quantity}</strong>
                <button type="button" data-detail-plus aria-label="Aumentar cantidad" ${stock <= 0 ? "disabled" : ""}>+</button>
            </div>
            <button class="btn btn--primary" type="button" data-detail-add ${stock <= 0 ? "disabled" : ""}>
                ${stock <= 0 ? "Agotado" : "Añadir al carrito"}
            </button>
        </div>
    `;

    const quantityElement = document.querySelector("[data-detail-qty]");
    document.querySelector("[data-detail-minus]").addEventListener("click", () => {
        setQuantity(product.id, getQuantity(product.id) - 1, stock);
        quantityElement.textContent = getQuantity(product.id);
    });
    document.querySelector("[data-detail-plus]").addEventListener("click", () => {
        setQuantity(product.id, getQuantity(product.id) + 1, stock);
        quantityElement.textContent = getQuantity(product.id);
    });
    document.querySelector("[data-detail-add]").addEventListener("click", () => {
        addProductToCart(product, getQuantity(product.id));
        closeModal("productModal");
    });
}

function addProductToCart(product, quantity) {
    if (!product) return;
    const stock = Number(product.stockDisponible || 0);
    if (stock <= 0) {
        toast("Este producto está agotado");
        return;
    }

    const existing = state.cart.find(item => item.productId === product.id);
    if (existing) {
        const newQuantity = Math.min(stock, existing.quantity + Number(quantity));
        if (newQuantity === existing.quantity) {
            toast(`No hay más stock disponible de ${product.nombre}`);
            return;
        }
        existing.quantity = newQuantity;
        existing.stock = stock;
    } else {
        state.cart.push({
            productId: product.id,
            name: product.nombre,
            price: Number(product.precio),
            quantity: Math.min(stock, Number(quantity)),
            stock,
            tipo: product.tipo,
            description: product.descripcion
        });
    }

    writeCart(state.cart);
    state.cart = readCart();
    renderCart();
    toast(`${product.nombre} añadido al carrito`);
    openModal("cartModal");
}

function renderCart() {
    const items = document.querySelector("#cartItems");
    const summary = document.querySelector("#cartSummary");
    const success = document.querySelector("#orderSuccess");
    const total = state.cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

    if (!state.cart.length) {
        items.innerHTML = '<div class="empty-state empty-state--wide">Tu carrito está vacío.</div>';
        document.querySelector("#cartTotal").textContent = "0 €";
        summary.hidden = true;
        success.hidden = true;
        return;
    }

    success.hidden = true;
    summary.hidden = false;
    document.querySelector("#cartTotal").textContent = `${formatPrice(total)} €`;
    document.querySelector("#cartMessage").textContent = "";

    items.innerHTML = state.cart.map((item, index) => {
        const fromFestival = !item.productId && item.passId;
        const subtitle = fromFestival
            ? `${item.eventName || "Pase del cartel"} · solo disponible en Festivales`
            : typeLabel(item.tipo || "producto");
        return `
            <article class="cart-item">
                <div>
                    <strong>${escapeHtml(item.name)}</strong>
                    <span>${escapeHtml(subtitle)}</span>
                </div>
                <div class="cart-item__quantity">
                    <button type="button" data-cart-action="minus" data-index="${index}" aria-label="Reducir cantidad">−</button>
                    <span>${item.quantity}</span>
                    <button type="button" data-cart-action="plus" data-index="${index}" aria-label="Aumentar cantidad">+</button>
                    <button type="button" data-cart-action="remove" data-index="${index}" aria-label="Quitar del carrito">×</button>
                </div>
                <strong>${formatPrice(item.price * item.quantity)} €</strong>
            </article>
        `;
    }).join("");

    document.querySelectorAll("[data-cart-action]").forEach(button => {
        button.addEventListener("click", () => {
            const index = Number(button.dataset.index);
            const item = state.cart[index];
            if (!item) return;
            const max = item.stock && item.stock > 0 ? item.stock : Infinity;

            if (button.dataset.cartAction === "remove") {
                state.cart.splice(index, 1);
                writeCart(state.cart);
                state.cart = readCart();
                renderCart();
                return;
            }

            if (button.dataset.cartAction === "plus") {
                if (item.quantity >= max) {
                    toast("No hay más stock disponible de este producto");
                    return;
                }
                item.quantity += 1;
            } else {
                item.quantity -= 1;
            }

            if (item.quantity <= 0) state.cart.splice(index, 1);
            writeCart(state.cart);
            state.cart = readCart();
            renderCart();
        });
    });
}

function showOrderSuccess(order) {
    const success = document.querySelector("#orderSuccess");
    document.querySelector("#cartItems").innerHTML = "";
    document.querySelector("#cartSummary").hidden = true;
    success.hidden = false;
    success.innerHTML = `
        <div class="order-success__icon" aria-hidden="true">✓</div>
        <h3>Pedido confirmado</h3>
        <p>Tu compra se ha registrado correctamente en el gestor de inventario.</p>
        <dl class="order-success__data">
            <div><dt>Pedido</dt><dd>${escapeHtml(order.id)}</dd></div>
            <div><dt>Total</dt><dd>${formatPrice(order.importeTotal)} €</dd></div>
            <div><dt>Estado</dt><dd>${escapeHtml(window.Subsonic.statusLabel(order.estado))}</dd></div>
        </dl>
        <div class="order-success__actions">
            <a class="btn btn--primary" href="/pedidos.html">Ver mis pedidos</a>
            <button class="btn btn--ghost" type="button" data-close-modal="cartModal">Seguir comprando</button>
        </div>
    `;
    success.querySelector("[data-close-modal]").addEventListener("click", () => closeModal("cartModal"));
}

async function checkout() {
    if (!state.cart.length) {
        toast("El carrito está vacío");
        return;
    }

    if (!getToken()) {
        state.pendingCheckout = true;
        closeModal("cartModal");
        openModal("loginModal");
        toast("Inicia sesión para finalizar la compra");
        return;
    }

    const festivalItems = state.cart.filter(item => !item.productId && item.passId);
    if (festivalItems.length) {
        document.querySelector("#cartMessage").textContent =
            "Los pases añadidos desde Festivales no se venden en esta tienda. Quítalos del carrito para continuar.";
        return;
    }

    const lineas = state.cart.map(item => {
        const line = {
            productoId: item.productId,
            cantidad: item.quantity
        };
        if (item.eventId) line.eventoId = item.eventId;
        if (item.paseId) line.paseId = item.paseId;
        return line;
    });

    const button = document.querySelector("#checkoutButton");
    const message = document.querySelector("#cartMessage");
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = "Procesando...";
    message.textContent = "";

    try {
        const order = await api("/api/gateway/orders", {
            method: "POST",
            body: JSON.stringify({ lineas })
        });
        state.cart = [];
        writeCart(state.cart);
        renderCart();
        showOrderSuccess(order);
        loadProducts();
    } catch (error) {
        if (error.status === 401) {
            state.pendingCheckout = true;
            closeModal("cartModal");
            openModal("loginModal");
            toast("Tu sesión ha caducado. Vuelve a iniciar sesión.");
        } else {
            message.textContent = error.message;
        }
    } finally {
        button.disabled = false;
        button.textContent = originalText;
    }
}

function initEvents() {
    document.querySelector("#storeQuery").addEventListener("input", event => {
        state.query = event.target.value;
        renderProducts();
    });

    document.querySelector("#storeCategories").addEventListener("click", event => {
        const button = event.target.closest("[data-category]");
        if (!button) return;
        state.category = button.dataset.category;
        document.querySelectorAll("#storeCategories [data-category]").forEach(candidate => {
            candidate.classList.toggle("active", candidate === button);
        });
        renderProducts();
    });

    document.querySelector("#storeSort").addEventListener("change", event => {
        state.sort = event.target.value;
        renderProducts();
    });

    document.querySelector("#cartButton").addEventListener("click", () => {
        renderCart();
        openModal("cartModal");
    });
    document.querySelector("#checkoutButton").addEventListener("click", checkout);

    initModalCloseButtons();
    initHeader();
    initLoginModal(() => {
        if (state.pendingCheckout) {
            state.pendingCheckout = false;
            window.setTimeout(() => {
                openModal("cartModal");
                checkout();
            }, 500);
        }
    });
}

renderCart();
initEvents();
loadProducts();
