# Subsonic Festival — Frontend

Frontend server-side de **Subsonic Festival 2027**, construido con Java 17,
Spring Boot, Spring MVC, Thymeleaf, HTML y CSS.

## Estructura

```text
frontend/
├── pom.xml                              # Proyecto Maven / Spring Boot
├── README.md
└── src/main/
    ├── java/com/subsonicfestival/frontend/
    │   ├── FrontendApplication.java     # Punto de entrada de Spring Boot
    │   └── GatewayController.java       # Proxy hacia los microservicios
    └── resources/
        ├── application.properties
       ├── templates/
       │   ├── fragments/navigation.html
       │   └── artists-blog/              # Catálogo y detalle de artistas/blog
       └── static/
           ├── index.html               # Página principal (con preview de recintos)
           ├── artistas.html            # Alias legado; la vista completa usa Thymeleaf
           ├── blog.html                # Alias legado; la vista completa usa Thymeleaf
           ├── festivales.html          # Catálogo de festivales y pases
           ├── login.html               # Inicio de sesión
           ├── register.html            # Registro de usuarios / proveedores
           ├── tienda.html              # Tienda conectada al inventario
           ├── pedidos.html             # Historial y detalle de pedidos
           ├── recintos.html            # Gestor de espacios / recintos (grounds)
           ├── css/                     # Estilos del sitio y de Artists & Blog
           ├── images/
           └── js/
               ├── app.js               # Página principal y preview de recintos
               ├── auth-pages.js        # Utilidades de login y registro
               ├── commerce.js          # Utilidades compartidas (sesión, JWT, modales, avisos)
               ├── festivales.js        # Catálogo de festivales
               ├── neural-glow.js       # Efectos visuales
               ├── pedidos.js           # Pedidos, entradas y cancelaciones
               ├── recintos.js          # Gestor de espacios (grounds): listar, publicar, reservar
               └── tienda.js            # Catálogo, carrito y checkout
```

## Gestor de Espacios — Recintos (grounds)

La página `recintos.html` (controlada por `js/recintos.js`) es el front del microservicio **subsonic-grounds**. A través del gateway (`/api/gateway/grounds`) permite:

- Listar y filtrar espacios por tipo, ubicación, estado y precio máximo.
- Publicar, editar y eliminar espacios (roles `admin` / `proveedor`).
- Reservar un espacio como proveedor (`POST .../{id}/rent`).

El gateway expone esos endpoints en `GatewayController.java` y la URL del microservicio se configura en `application.properties`:

```
gateway.grounds-url=${GATEWAY_GROUNDS_URL:http://localhost:8004}
```

Para probarlo en conjunto, el microservicio de recintos (`components/subsonic-grounds`) debe estar levantado en el puerto 8004 (o en la URL indicada por `GATEWAY_GROUNDS_URL`).

## Artists & Blog

Las páginas públicas de artistas y blog usan Thymeleaf y consultan el
microservicio Artists & Blog mediante `GATEWAY_ARTISTS_URL` (por defecto
`http://localhost:8002`). El gateway del frontend también expone la lista de
artistas para la portada. Las rutas completas son `/artistas`,
`/artistas/{id}`, `/blog` y `/blog/{id}`. El listado permite filtrar por nombre;
las fichas muestran biografía, redes, eventos y canciones. El blog ofrece
listado paginado y artículos completos. Sin registros, ambas vistas muestran
un estado vacío; el blog seguirá así hasta que se carguen artículos.

## Ejecutar con Spring Boot

Requisitos: JDK 17+ y Maven 3.8+.

```bash
cd frontend
mvn spring-boot:run
```

Abre [http://localhost:8080](http://localhost:8080). Las páginas del componente
están disponibles en `/artistas` y `/blog`. Si el microservicio Artists & Blog
no está disponible, el frontend muestra una página de error en vez de presentar
una lista vacía como si fuera válida.

Configura las URL internas de los microservicios mediante `GATEWAY_EVENTS_URL`,
`GATEWAY_ARTISTS_URL`, `GATEWAY_AUTH_URL` y `GATEWAY_INVENTORY_URL`. Artists &
Blog publica el puerto 8002 en el host cuando se inicia con Docker Compose.

```powershell
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

## Empaquetar como JAR

```bash
mvn clean package
java -jar target/subsonic-frontend-1.0.0.jar
```

La página y sus estilos se sirven directamente desde `src/main/resources/static`, por lo que funcionan igual en el IDE y dentro del JAR.
