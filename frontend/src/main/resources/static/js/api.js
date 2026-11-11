/**
 * Cliente de la API de subsonic-users-auth.
 *
 * El frontend habla siempre con su propio origen (`/api/users-auth/**`).
 * Spring Boot reenvía esas peticiones al microservicio de usuarios, por lo
 * que no es necesario configurar CORS en subsonic-users-auth.
 */

const API_BASE = "/api/users-auth";
const SESSION_KEY = "subsonic.session";
const DEFAULT_EXPIRES_IN = 3600;

let memorySession = null;

export class ApiError extends Error {
    constructor(message, { status = 0, code = null, details = null } = {}) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.code = code;
        this.details = details;
    }
}

function readSession() {
    try {
        const raw = window.localStorage.getItem(SESSION_KEY);
        if (!raw) {
            return memorySession;
        }
        const value = JSON.parse(raw);
        if (!value || typeof value.accessToken !== "string" || !value.accessToken) {
            return null;
        }
        return value;
    } catch (error) {
        return memorySession;
    }
}

function writeSession(value) {
    memorySession = value;
    try {
        if (value) {
            window.localStorage.setItem(SESSION_KEY, JSON.stringify(value));
        } else {
            window.localStorage.removeItem(SESSION_KEY);
        }
    } catch (error) {
        // El modo privado puede bloquear localStorage: se conserva la sesión en memoria.
    }
}

export const session = {
    get: readSession,

    save({ accessToken, tokenType = "bearer", expiresIn = DEFAULT_EXPIRES_IN, user = null }) {
        const lifetime = Number.isFinite(expiresIn) && expiresIn > 0 ? expiresIn : DEFAULT_EXPIRES_IN;
        writeSession({
            accessToken,
            tokenType,
            expiresAt: Date.now() + lifetime * 1000 - 5000,
            user,
        });
    },

    clear() {
        writeSession(null);
    },

    token() {
        const value = readSession();
        return value ? value.accessToken : null;
    },

    user() {
        const value = readSession();
        return value ? value.user : null;
    },

    updateUser(user) {
        const current = readSession();
        if (current) {
            writeSession({ ...current, user });
        }
    },

    isExpired() {
        const value = readSession();
        if (!value) {
            return true;
        }
        return typeof value.expiresAt === "number" && Date.now() >= value.expiresAt;
    },
};

function fieldFromLocation(location) {
    if (!Array.isArray(location)) {
        return "";
    }
    const parts = location.filter((part) => typeof part === "string" && part !== "body");
    return parts.length ? parts[parts.length - 1] : "";
}

const FIELD_MESSAGES = {
    correo: "Introduce un correo electrónico válido.",
    password: "La contraseña debe tener al menos 8 caracteres.",
    nombre: "El nombre es obligatorio.",
    codigo: "El código no es válido o ha caducado.",
    nuevaPassword: "La nueva contraseña debe tener al menos 8 caracteres.",
    tipoServicio: "Indica el tipo de servicio que ofreces.",
};

function validationMessage(item) {
    const field = fieldFromLocation(item && item.loc);
    const raw = String((item && item.msg) || "Datos no válidos.");

    if (field && FIELD_MESSAGES[field]) {
        return FIELD_MESSAGES[field];
    }
    if (/valid email/i.test(raw)) {
        return "Introduce un correo electrónico válido.";
    }
    if (/at least 8 characters/i.test(raw)) {
        return "La contraseña debe tener al menos 8 caracteres.";
    }
    if (/at least 16 characters/i.test(raw)) {
        return "El código debe tener al menos 16 caracteres.";
    }
    if (/field required/i.test(raw)) {
        return "Hay campos obligatorios sin completar.";
    }
    return raw;
}

function readErrorMessage(payload, status) {
    if (payload && typeof payload === "object") {
        if (Array.isArray(payload.detail)) {
            const messages = payload.detail.map(validationMessage);
            return [...new Set(messages)].join(" ");
        }
        if (typeof payload.detail === "string" && payload.detail) {
            return payload.detail;
        }
        if (typeof payload.message === "string" && payload.message) {
            return payload.message;
        }
    }

    switch (status) {
        case 400:
            return "Los datos enviados no son válidos.";
        case 401:
            return "Credenciales inválidas o sesión caducada.";
        case 403:
            return "No tienes permisos para realizar esta acción.";
        case 404:
            return "No se ha encontrado el recurso solicitado.";
        case 409:
            return "Ya existe una cuenta con ese correo electrónico.";
        case 422:
            return "Revisa los datos del formulario.";
        case 502:
            return "El servicio de usuarios no está disponible.";
        default:
            return "Ha ocurrido un error inesperado. Inténtalo de nuevo.";
    }
}

/**
 * Realiza una petición a la API.
 *
 * @param {string} path Ruta relativa bajo /v1, por ejemplo "/v1/auth/login".
 * @param {{method?: string, body?: object, auth?: boolean}} options
 */
export async function api(path, { method = "GET", body, auth = true } = {}) {
    const headers = { Accept: "application/json" };
    if (body !== undefined && body !== null) {
        headers["Content-Type"] = "application/json";
    }

    const token = session.token();
    if (auth && token) {
        headers.Authorization = `Bearer ${token}`;
    }

    let response;
    try {
        response = await fetch(API_BASE + path, {
            method,
            headers,
            body: body === undefined || body === null ? undefined : JSON.stringify(body),
            credentials: "same-origin",
        });
    } catch (error) {
        throw new ApiError(
            "No se pudo conectar con el servicio de usuarios. Comprueba que subsonic-users-auth está en marcha.",
            { code: "NETWORK_ERROR" }
        );
    }

    const text = await response.text();
    let payload = null;
    if (text) {
        try {
            payload = JSON.parse(text);
        } catch (error) {
            payload = text;
        }
    }

    if (!response.ok) {
        if (response.status === 401 && auth) {
            session.clear();
        }
        throw new ApiError(readErrorMessage(payload, response.status), {
            status: response.status,
            code: payload && payload.code ? payload.code : null,
            details: payload && payload.details !== undefined ? payload.details : payload && payload.detail,
        });
    }

    return payload;
}

export function goToLogin(reason) {
    const url = new URL("login.html", window.location.href);
    if (!window.location.pathname.endsWith("/login.html")) {
        url.searchParams.set("next", window.location.pathname + window.location.search);
    }
    if (reason) {
        url.searchParams.set("reason", reason);
    }
    window.location.replace(url.pathname + url.search + url.hash);
}

export function goToNext(next, fallback) {
    if (typeof next === "string" && next.startsWith("/") && !next.startsWith("//")) {
        try {
            const target = new URL(next, window.location.origin);
            if (target.origin === window.location.origin && !target.pathname.endsWith("/login.html")) {
                window.location.replace(target.pathname + target.search + target.hash);
                return;
            }
        } catch (error) {
            // Se usa el destino por defecto.
        }
    }
    window.location.replace(fallback);
}

export function dashboardFor(user) {
    return user && user.rol === "admin" ? "admin.html" : "perfil.html";
}

/**
 * Comprueba la sesión contra la API y devuelve el perfil actualizado.
 * Si no hay sesión válida redirige al acceso.
 */
export async function requireSession({ adminOnly = false } = {}) {
    const current = session.get();
    if (!current || session.isExpired()) {
        session.clear();
        goToLogin("expired");
        throw new ApiError("La sesión ha caducado.", { status: 401 });
    }

    let user;
    try {
        user = await api("/v1/usuarios/perfil");
    } catch (error) {
        if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
            session.clear();
            goToLogin(error.status === 403 ? "suspended" : "expired");
        }
        throw error;
    }

    session.updateUser(user);

    if (adminOnly && user.rol !== "admin") {
        window.location.replace("perfil.html?denied=1");
        throw new ApiError("Necesitas una cuenta de administrador.", { status: 403 });
    }

    return user;
}

/**
 * Devuelve true cuando el error implica que la sesión ya no es válida y la
 * página está siendo redirigida al acceso.
 */
export function handleSessionError(error) {
    if (!(error instanceof ApiError)) {
        return false;
    }
    if (error.status === 401) {
        session.clear();
        goToLogin("expired");
        return true;
    }
    if (error.status === 403 && /suspendid/i.test(error.message)) {
        session.clear();
        goToLogin("suspended");
        return true;
    }
    return false;
}
