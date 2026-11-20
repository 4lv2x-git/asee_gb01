# Subsonic Festival — Frontend

Página principal responsive de **Subsonic Festival 2027**, construida con HTML y CSS y servida con una aplicación mínima de **Spring Boot**.

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
        └── static/
            ├── index.html               # Página principal (con preview de recintos)
            ├── artistas.html            # Catálogo de artistas
            ├── blog.html                # Blog del festival
            ├── festivales.html          # Catálogo de festivales y pases
            ├── login.html               # Inicio de sesión
            ├── register.html            # Registro de usuarios / proveedores
            ├── tienda.html              # Tienda conectada al inventario
            ├── pedidos.html             # Historial y detalle de pedidos
            ├── recintos.html            # Gestor de espacios / recintos (grounds)
            ├── css/styles.css           # Estilos y diseño responsive
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

## Ver la página sin backend

Abre `src/main/resources/static/index.html` directamente en el navegador. La página no necesita JavaScript ni servidor para visualizarse.

## Ejecutar con Spring Boot

Requisitos: JDK 17+ y Maven 3.8+.

```bash
cd frontend
mvn spring-boot:run
```

Abre [http://localhost:8080](http://localhost:8080).

## Empaquetar como JAR

```bash
mvn clean package
java -jar target/subsonic-frontend-1.0.0.jar
```

La página y sus estilos se sirven directamente desde `src/main/resources/static`, por lo que funcionan igual en el IDE y dentro del JAR.
