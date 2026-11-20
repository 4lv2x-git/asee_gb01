document.querySelectorAll("#togglePassword").forEach(button => {
    button.addEventListener("click", () => {
        const input = button.parentElement.querySelector("input");
        const image = button.querySelector("img");
        input.type = input.type === "password" ? "text" : "password";
        image.src = input.type === "password" ? "images/OjoCerrado.png" : "images/OjoAbierto.png";
    });
});

const loginForm = document.querySelector("#loginForm");
if (loginForm) {
    loginForm.addEventListener("submit", async event => {
        event.preventDefault();
        const message = document.querySelector("#loginMessage");
        message.textContent = "Conectando...";
        try {
            const response = await fetch("/api/gateway/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    correo: document.querySelector("#loginEmail").value,
                    password: document.querySelector("#loginPassword").value
                })
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.mensaje || result.message || result.detail || "No se pudo iniciar sesión");
            localStorage.setItem("subsonic-token", result.accessToken || result.access_token);
            message.textContent = "Sesión iniciada";
            setTimeout(() => location.href = "/", 500);
        } catch (error) {
            message.textContent = error.message;
        }
    });
}

const registerForm = document.querySelector("#registerForm");
if (registerForm) {
    registerForm.addEventListener("submit", async event => {
        event.preventDefault();
        const message = document.querySelector("#registerMessage");
        message.textContent = "Creando cuenta...";
        try {
            const response = await fetch("/api/gateway/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    nombre: document.querySelector("#registerName").value,
                    correo: document.querySelector("#registerEmail").value,
                    password: document.querySelector("#registerPassword").value,
                    rol: document.querySelector("#registerRole").value,
                    tipoServicio: document.querySelector("#registerRole").value === "proveedor"
                        ? document.querySelector("#providerService").value
                        : null
                })
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.detail || "No se pudo crear la cuenta");
            message.textContent = "Cuenta creada. Ya puedes iniciar sesión.";
            setTimeout(() => location.href = "/login.html", 900);
        } catch (error) {
            message.textContent = error.message;
        }
    });
}

document.querySelectorAll("#googleLogin, #googleRegister").forEach(button => {
    button.addEventListener("click", () => {
        alert("La conexión con Google debe configurarse con OAuth en el gestor de usuarios.");
    });
});

const orbs = document.querySelectorAll(".auth-orb");
document.addEventListener("pointermove", event => {
    const x = (event.clientX / window.innerWidth - 0.5) * 24;
    const y = (event.clientY / window.innerHeight - 0.5) * 24;
    orbs.forEach((orb, index) => {
        const factor = index === 0 ? 1 : -1;
        orb.style.transform = `translate(${x * factor}px, ${y * factor}px)`;
    });

    if (document.querySelector("#neural-glow-canvas")) {
        const neuralGlow = new NeuralGlow("#neural-glow-canvas", {
            primaryColor: [0.10, 0.35, 1.0],
            secondaryColor: [0.0, 0.08, 0.35]
        });
        neuralGlow.init();
    }
});

const role = document.querySelector("#registerRole");
if (role) {
    role.addEventListener("change", () => {
        const providerFields = document.querySelector("#providerFields");
        providerFields.hidden = role.value !== "proveedor";
        document.querySelector("#providerService").required = role.value === "proveedor";
    });
}
