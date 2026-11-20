# Subsonic Festival — Frontend

Interfaz web de **Subsonic Festival 2027**, construida con **Spring Boot**, **Spring MVC**
y **Thymeleaf**. La página principal se sirve como HTML estático con su hoja de estilos
y `site.js`; el área de acceso, el perfil, los catálogos de artistas y blog y el panel de
administración se renderizan en el servidor.

## Páginas

| Ruta | Descripción |
| --- | --- |
| `/` o `/index.html` | Página principal del festival (HTML estático). |
| `/login` | Inicio de sesión, registro de clientes y proveedores y recuperación de contraseña. |
| `/perfil` | Perfil del usuario autenticado: datos personales, datos de proveedor, verificación de correo y baja de cuenta. |
| `/admin` | Panel de administración: gestión de usuarios. |
| `/admin/eventos` | Consulta de festivales y eventos registrados en `service-festivals`. |
| `/admin/inventario` | Catálogo y stock de entradas, pases y merchandising del gestor de inventario. |
| `/admin/artistas` | Fichas del cartel publicadas por el servicio Artists & Blog. |
| `/artistas`, `/blog` | Catálogos renderizados con Thymeleaf a partir de Artists & Blog. |
| `/recintos.html` | Gestor de espacios y recintos (`subsonic-grounds`). |
| `/cuenta` | Redirección usada por el enlace «Acceso» de la portada: lleva al perfil, al panel de administración o al formulario de acceso según la sesión. |
| `/api/sesion` | Estado público de la sesión (nombre e iniciales) que usa la portada para mostrar el acceso al perfil cuando hay sesión iniciada. |

Las antiguas rutas `/login.html`, `/perfil.html` y `/admin.html` se mantienen como
alias de las rutas limpias. El registro se abre en `/login?vista=registro`.

## Estructura

```text
frontend/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/subsonicfestival/frontend/
    │   ├── FrontendApplication.java     # Punto de entrada de Spring Boot
    │   ├── GatewayController.java       # Proxy hacia los microservicios
    │   ├── artistsblog/                 # Cliente y vistas de Artists & Blog
    │   └── web/
    │       ├── AuthController.java               # /login, /cuenta, /registro, /recuperar, /logout
    │       ├── PerfilController.java             # /perfil y acciones del área personal
    │       ├── AdminController.java              # /admin y gestión de usuarios
    │       ├── AdminEventosController.java       # /admin/eventos (solo lectura)
    │       ├── AdminInventarioController.java    # /admin/inventario (solo lectura)
    │       ├── AdminArtistasController.java      # /admin/artistas (solo lectura)
    │       ├── EventosClient.java                # Cliente Java de service-festivals
    │       ├── InventarioClient.java             # Cliente Java del gestor de inventario
    │       ├── ServicioExternoException.java     # Fallos de microservicios externos
    │       ├── SesionApiController.java          # /api/sesion para la portada estática
    │       ├── UsuariosAuthClient.java           # Cliente Java de subsonic-users-auth
    │       ├── UsuariosAuthException.java        # Errores normalizados de la API
    │       ├── Sesion.java                       # Token y usuario en la HttpSession
    │       ├── SesionInterceptor.java            # Protección de /perfil y /admin
    │       ├── WebConfig.java                    # Registro del interceptor
    │       ├── ModeloGlobal.java                 # Usuario e iniciales en todas las plantillas
    │       ├── Textos.java                       # Etiquetas de rol, estado e iniciales
    │       ├── dto/                              # Usuario, TokenResponse
    │       └── form/                             # Formularios con validación Bean Validation
    └── resources/
        ├── application.properties
        ├── templates/
        │   ├── login.html
        │   ├── perfil.html
        │   ├── admin.html
        │   ├── admin-eventos.html
        │   ├── admin-inventario.html
        │   ├── admin-artistas.html
        │   ├── admin-editar.html
        │   ├── admin-password.html
        │   ├── confirmar-eliminar.html
        │   ├── artists-blog/              # Catálogo y detalle de artistas/blog
        │   └── fragments/
        │       ├── comun.html            # Cabeceras, pie, alertas y menú del panel
        │       └── navigation.html       # Navegación de las vistas de artistas/blog
        └── static/
            ├── index.html               # Página principal (con preview de recintos)
            ├── artistas.html            # Alias legado de /artistas (controlado por Thymeleaf)
            ├── blog.html                # Alias legado de /blog (controlado por Thymeleaf)
            ├── festivales.html          # Catálogo de festivales y pases
            ├── tienda.html              # Tienda conectada al inventario
            ├── pedidos.html             # Historial y detalle de pedidos
            ├── recintos.html            # Gestor de espacios / recintos (grounds)
            ├── css/
            │   ├── styles.css           # Estilos de la página principal
            │   ├── account.css          # Acceso, perfil y administración
            │   └── artists-blog.css     # Catálogos de artistas y blog
            └── js/
                ├── site.js              # Acceso al perfil en la portada según la sesión
                ├── cuenta.js            # Mostrar u ocultar contraseñas en las páginas de cuenta
                ├── app.js               # Página principal y preview de recintos
                ├── festivales.js        # Catálogo de festivales
                ├── commerce.js          # Utilidades de tienda, carrito y sesión
                ├── tienda.js            # Catálogo, carrito y checkout
                ├── recintos.js          # Gestor de espacios (grounds): listar, publicar, reservar
                └── pedidos.js           # Pedidos, entradas y cancelaciones
```

## Requisitos

- JDK 17 o superior.
- Maven 3.8 o superior.
- El microservicio [`subsonic-users-auth`](../components/subsonic-users-auth/README.md)
  en marcha para usar el acceso, el perfil y el panel de administración.
- Opcionalmente, `service-festivals`, `subsonic-inventory-manager` y
  `service-artists-blog` para los módulos de eventos, inventario y artistas. Si no
  están disponibles, el panel sigue cargando y muestra un aviso en la propia página.
- Opcionalmente, `subsonic-grounds` para el gestor de recintos de `/recintos.html`.

## Arrancar el servicio de usuarios

Desde la raíz del repositorio:

```bash
cd components/subsonic-users-auth
docker compose up --build
```

La API de usuarios queda disponible en `http://localhost:8000`. El arranque crea
de forma idempotente un administrador inicial con los valores `ADMIN_EMAIL` y
`ADMIN_PASSWORD` de `.env.example`, que es el archivo que lee `docker-compose.yml`
(por defecto `admin@subsonicfestival.com` / `cambia-esto-en-produccion`). Edita
ese archivo antes de arrancar si quieres cambiar las credenciales.

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

Abre [http://localhost:8080](http://localhost:8080). Las páginas del componente
están disponibles en `/artistas` y `/blog`. Si el microservicio Artists & Blog
no está disponible, el frontend muestra una página de error en vez de presentar
una lista vacía como si fuera válida.

Configura las URL internas según qué microservicios arranques en local:

| Variable | Servicio | Valor local habitual |
| --- | --- | --- |
| `USERS_AUTH_API_URL` | Acceso, perfil y administración (servidor) | `http://localhost:8000` |
| `GATEWAY_EVENTS_URL` | Festivales y eventos | `http://localhost:8000` si arrancas `service-festivals` |
| `GATEWAY_ARTISTS_URL` | Artistas y blog | `http://localhost:8002` |
| `GATEWAY_INVENTORY_URL` | Inventario y pedidos | `http://localhost:8081` |
| `GATEWAY_GROUNDS_URL` | Recintos | `http://localhost:8004` |
| `GATEWAY_AUTH_URL` | Proxy heredado usado por los formularios JS de la tienda | `http://localhost:8000` |

Los servicios de usuarios y festivales usan ambos el puerto 8000 en local, así
que debes arrancar uno a la vez o asignarles puertos distintos. Las vistas
Thymeleaf (/perfil, /admin y sus módulos) siempre usan la URL del servidor
(`USERS_AUTH_API_URL`); `GATEWAY_AUTH_URL` solo afecta al formulario heredado de
la portada.

```powershell
$env:USERS_AUTH_API_URL = "http://localhost:8001"
$env:GATEWAY_ARTISTS_URL = "http://localhost:8002"
mvn spring-boot:run
```

La portada consulta `/api/gateway/artists`; las vistas Thymeleaf consultan el
mismo servicio usando `GATEWAY_ARTISTS_URL`.

Al iniciar el backend Artists & Blog, se cargan automáticamente las fichas de
demostración si la colección de artistas está vacía. Consulta el
[`README del servicio`](../components/service-artists-blog/README.md) para más
detalles.

## Pruebas

```bash
mvn test
```

Las pruebas MVC verifican el renderizado de catálogos, detalles, enlaces,
paginación y límites de consulta.

Para empaquetar como JAR:

```bash
mvn clean package
java -jar target/subsonic-frontend-1.0.0.jar
```

## Conexión con la API de usuarios

Las llamadas del área de cuenta y administración a `subsonic-users-auth` se hacen
desde Java con `UsuariosAuthClient` (`RestClient`). En ese flujo el navegador no
recibe el JWT: solo guarda la cookie `JSESSIONID`, por lo que no hace falta
configurar CORS en el microservicio.

Las páginas heredadas de la tienda y el carrito conservan un formulario que usa
`/api/gateway/auth/login` y guarda el token en `localStorage`. Ese token es
ajeno a `/perfil` y `/admin`: para esas rutas hay que iniciar sesión con el
formulario de `/login`, que sí crea la sesión del servidor.

La URL de destino se puede cambiar con la variable de entorno
`USERS_AUTH_API_URL` o con la propiedad
`subsonic.users-auth.url` en `application.properties`:

```bash
USERS_AUTH_API_URL=http://localhost:8000 mvn spring-boot:run
```

El valor debe ser la **raíz** del servicio, no la ruta `/v1`.

## Sesión y seguridad

- Tras un inicio de sesión correcto, el JWT se guarda en la `HttpSession` del
  servidor. El navegador solo recibe la cookie `JSESSIONID` (marcada como
  `HttpOnly` por defecto), nunca el token.
- `SesionInterceptor` protege `/perfil/**` y `/admin/**`. Si no hay sesión activa
  o el token ha caducado, redirige a `/login?motivo=caducada`; si la ruta es de
  administración y el rol no es `admin`, redirige a `/perfil?denegado=1`.
- La sesión del servidor caduca a los 60 minutos (`server.servlet.session.timeout`),
  igual que el JWT emitido por el microservicio.
- Al cerrar sesión se llama a `POST /v1/auth/logout` para revocar el token y se
  invalida la `HttpSession`.
- El formulario de acceso y los formularios de cuenta usan validación de servidor
  con Bean Validation; los errores se muestran junto a cada campo.
- Un administrador no puede suspender, cambiar la contraseña ni eliminar su propia
  cuenta desde el panel.

## Flujos implementados

### Autenticación

- `POST /v1/auth/login`: el servidor guarda el JWT en la sesión y redirige al
  panel correspondiente según el rol (`admin` o usuario).
- `POST /v1/auth/register`: permite registrarse como **cliente** o
  **proveedor** (con `tipoServicio` obligatorio para proveedores).
- `POST /v1/auth/forgot-password` y `POST /v1/auth/reset-password`: recuperación
  mediante código de un solo uso.
- `POST /v1/auth/verify-email`: verificación del correo desde el perfil.
- `POST /v1/auth/logout`: revoca el token actual e invalida la sesión.

### Perfil

- `GET /v1/usuarios/perfil`: carga los datos actuales.
- `PATCH /v1/usuarios/perfil`: edita nombre, dirección y, en proveedores,
  `tipoServicio` y `descripcionServicio`.
- `DELETE /v1/usuarios/perfil`: elimina la cuenta tras confirmar en una página
  dedicada.

### Administración

#### Usuarios

- `GET /v1/usuarios/admin`: listado completo de cuentas.
- `PUT /v1/usuarios/admin/{id}`: edición de datos por parte del administrador.
- `PATCH /v1/usuarios/admin/{id}/estado`: suspender o reactivar una cuenta.
- `PUT /v1/usuarios/admin-password-reset/{id}`: restablecer la contraseña de un
  usuario.
- `DELETE /v1/usuarios/admin/{id}`: eliminar una cuenta.

La búsqueda y los filtros por rol y estado del panel se aplican en el servidor
sobre el listado devuelto por la API. Las acciones de edición, cambio de
contraseña y eliminación tienen páginas propias renderizadas con Thymeleaf.

#### Eventos

`/admin/eventos` consulta `GET /eventos` de `service-festivals` desde
`EventosClient` y muestra el listado con búsqueda por nombre, ciudad o país y
filtros por estado (`publicado`, `borrador`, `cancelado`, `finalizado`). El
panel es de solo lectura: la creación y edición de festivales corresponde al
microservicio. Si el servicio no está disponible, la página sigue cargando y
muestra un aviso.

#### Inventario

`/admin/inventario` consulta `GET /inventario/productos` de
`subsonic-inventory-manager` y resume productos, unidades en stock, valor del
inventario, stock bajo y agotados. Permite buscar y filtrar por tipo (`entrada`,
`pase`, `merchandising`) y disponibilidad. También es de solo lectura: las altas y
los ajustes de stock se hacen en el gestor de inventario.

#### Artistas

`/admin/artistas` consulta `GET /v1/artistas` y `GET /v1/artistas/{id}` de
`service-artists-blog` a través de `ArtistsBlogApiClient`. Muestra una ficha por
artista con biografía, número de eventos vinculados, canciones destacadas y
enlaces oficiales, con búsqueda por nombre, ID o biografía. Es de solo lectura:
las altas y los cambios de ficha corresponden al microservicio. Si el servicio
no está disponible, la página se muestra con un aviso.

## Notas

- Todos los campos de contraseña de las páginas de cuenta incluyen un botón para
  mostrar u ocultar el texto. Es una mejora progresiva: si JavaScript está
  desactivado, el botón no aparece y el campo funciona con normalidad.
- La página principal (`index.html`) sigue funcionando sin backend. Si el
  frontend está arrancado y hay una sesión activa, `site.js` sustituye el enlace
  «Acceso» por el avatar con iniciales y el nombre del usuario, que enlaza
  directamente a `/perfil`. Sin sesión o sin conexión se mantiene `/cuenta`.
- En las páginas de cuenta, el avatar y el nombre de la cabecera también enlazan
  a `/perfil`.
- Los estilos de las páginas de cuenta reutilizan las variables y componentes de
  `css/styles.css` desde `css/account.css`.
- En desarrollo, `spring.thymeleaf.cache=false` permite ver los cambios de las
  plantillas sin reiniciar el servidor.
