package com.subsonicfestival.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación Spring Boot que sirve la web de Subsonic Festival.
 *
 * <p>Incluye la página principal, el acceso y perfil de usuario, el panel de
 * administración y un proxy same-origin hacia el microservicio
 * {@code subsonic-users-auth}.</p>
 */
@SpringBootApplication
public class FrontendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}
