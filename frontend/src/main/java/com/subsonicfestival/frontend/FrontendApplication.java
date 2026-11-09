package com.subsonicfestival.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación Spring Boot que sirve la página principal de Subsonic Festival.
 *
 * <p>Los recursos estáticos se encuentran en la raíz del proyecto
 * ({@code index.html} y {@code css/}) y se sirven tanto en desarrollo como
 * desde el JAR empaquetado.</p>
 */
@SpringBootApplication
public class FrontendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}
