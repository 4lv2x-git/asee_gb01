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
    │   └── FrontendApplication.java     # Punto de entrada de Spring Boot
    └── resources/
        ├── application.properties
        ├── templates/
        │   ├── fragments/navigation.html
        │   └── artists-blog/              # Catálogo y detalle de artistas/blog
        └── static/
            ├── index.html               # Página principal
            └── css/                      # Estilos del sitio y del componente
```

## Artists & Blog

Las páginas públicas de artistas y blog usan Thymeleaf y consultan el
microservicio FastAPI mediante `ARTISTS_BLOG_API_URL` (por defecto
`http://localhost:8000`). El listado de artistas permite filtrar por nombre; las
fichas muestran biografía, redes, eventos y canciones. El blog ofrece listado
paginado y artículos completos.

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

Para usar una URL distinta del servicio:

```powershell
$env:ARTISTS_BLOG_API_URL = "http://localhost:8000"
mvn spring-boot:run
```

La aplicación conecta directamente con la URL base configurada. Si el API
Gateway del equipo está desplegado, configura aquí su dirección y confirma con
el equipo el prefijo de rutas que expone.

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
