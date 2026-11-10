# Subsonic Festival — Frontend

Página principal responsive de **Subsonic Festival 2027**, construida con HTML y CSS y servida con una aplicación mínima de **Spring Boot**.

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
        └── static/
            ├── index.html               # Página principal
            └── css/styles.css           # Estilos y diseño responsive
```

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
