# Subsonic Festival — Frontend

Interfaz web de **Subsonic Festival 2027**, construida con HTML, CSS y JavaScript
vanilla y servida por una aplicación de **Spring Boot**.

Incluye la página principal, el área de acceso y perfil de usuario y el panel de
administración para la gestión de usuarios.

## Páginas

| Página | Descripción |
| --- | --- |
| `/index.html` | Página principal del festival. |
| `/login.html` | Inicio de sesión, registro de clientes y proveedores, recuperación de contraseña y verificación de correo. |
| `/perfil.html` | Perfil del usuario autenticado: datos personales, datos de proveedor, verificación de correo y baja de cuenta. |
| `/admin.html` | Panel de administración. En esta entrega solo incluye la **gestión de usuarios**; eventos, inventario y artistas se incorporarán más adelante. |

## Estructura

```text
frontend/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/subsonicfestival/frontend/
    │   ├── FrontendApplication.java          # Punto de entrada de Spring Boot
    │   └── UsersAuthProxyController.java     # Proxy same-origin hacia subsonic-users-auth
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html
            ├── login.html
            ├── perfil.html
            ├── admin.html
            ├── css/
            │   ├── styles.css                # Estilos de la página principal
            │   └── account.css               # Acceso, perfil y administración
            └── js/
                ├── api.js                    # Cliente de la API y gestión de sesión
                ├── ui.js                     # Alertas, formularios y diálogos
                ├── auth.js                   # Lógica de login.html
                ├── perfil.js                 # Lógica de perfil.html
                ├── admin.js                  # Lógica de admin.html
                └── site.js                   # Enlace de acceso dinámico en la portada
```

## Requisitos

- JDK 17 o superior.
- Maven 3.8 o superior.
- El microservicio [`subsonic-users-auth`](../components/subsonic-users-auth/README.md)
  en marcha para usar el acceso, el perfil y el panel de administración.

## Arrancar el servicio de usuarios

Desde la raíz del repositorio:

```bash
cd components/subsonic-users-auth
cp .env.example .env
docker compose up --build
```

La API de usuarios queda disponible en `http://localhost:8000`. El arranque crea
de forma idempotente un administrador inicial con los valores `ADMIN_EMAIL` y
`ADMIN_PASSWORD` del archivo `.env` (por defecto
`admin@subsonicfestival.com` / `cambia-esto-en-produccion`).

> `ADMIN_EMAIL` debe usar un dominio de correo válido. El validador del
> microservicio rechaza dominios reservados como `.local`, así que no uses
> direcciones del tipo `admin@subsonic.local` para la cuenta de administración.

> El envío real de códigos de verificación y recuperación requiere configurar
> `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER` y `SMTP_PASSWORD`. Sin SMTP, los códigos
> se generan en la base de datos pero no se envían por correo.

## Arrancar el frontend

```bash
cd frontend
mvn spring-boot:run
```

Abre [http://localhost:8080](http://localhost:8080).

Para empaquetar como JAR:

```bash
mvn clean package
java -jar target/subsonic-frontend-1.0.0.jar
```

## Conexión con la API de usuarios

Las páginas llaman a `/api/users-auth/**`, un proxy same-origin implementado en
`UsersAuthProxyController`. Spring Boot reenvía esas peticiones a
`http://localhost:8000` (la raíz del microservicio, que expone los endpoints bajo
`/v1`). De este modo:

- No es necesario añadir `http://localhost:8080` a `CORS_ORIGINS` en
  `subsonic-users-auth`.
- El navegador nunca guarda ni envía la URL interna del servicio de usuarios.

La URL de destino se puede cambiar con la variable de entorno
`USERS_AUTH_API_URL` o con la propiedad
`subsonic.users-auth.url` en `application.properties`:

```bash
USERS_AUTH_API_URL=http://localhost:8000 mvn spring-boot:run
```

El valor debe ser la **raíz** del servicio, no la ruta `/v1`.

## Flujos implementados

### Autenticación

- `POST /v1/auth/login`: guarda el JWT en `localStorage` y redirige al panel
  correspondiente según el rol (`admin` o usuario).
- `POST /v1/auth/register`: permite registrarse como **cliente** o
  **proveedor** (con `tipoServicio` obligatorio para proveedores).
- `POST /v1/auth/forgot-password` y `POST /v1/auth/reset-password`: recuperación
  mediante código de un solo uso.
- `POST /v1/auth/verify-email`: verificación del correo desde el perfil.
- `POST /v1/auth/logout`: revoca el token actual y limpia la sesión local.

### Perfil

- `GET /v1/usuarios/perfil`: carga los datos actuales.
- `PATCH /v1/usuarios/perfil`: edita nombre, dirección y, en proveedores,
  `tipoServicio` y `descripcionServicio`.
- `DELETE /v1/usuarios/perfil`: elimina la cuenta con confirmación previa.

### Administración (solo usuarios)

- `GET /v1/usuarios/admin`: listado completo de cuentas.
- `PUT /v1/usuarios/admin/{id}`: edición de datos por parte del administrador.
- `PATCH /v1/usuarios/admin/{id}/estado`: suspender o reactivar una cuenta.
- `PUT /v1/usuarios/admin-password-reset/{id}`: restablecer la contraseña de un
  usuario.
- `DELETE /v1/usuarios/admin/{id}`: eliminar una cuenta.

El panel de administración solo es accesible para usuarios con rol `admin`.
Un administrador no puede suspender ni eliminar su propia cuenta desde la
interfaz.

## Notas

- La sesión se guarda en `localStorage`; si el token caduca o el usuario es
  suspendido, las páginas redirigen al acceso.
- Las páginas de cuenta necesitan que el frontend se sirva con Spring Boot: si
  se abren directamente como archivo (`file://`), los módulos JavaScript no
  pueden cargarse y no hay proxy hacia la API.
- La página principal (`index.html`) sigue funcionando sin backend.
