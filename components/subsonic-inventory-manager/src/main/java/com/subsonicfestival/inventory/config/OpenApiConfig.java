package com.subsonicfestival.inventory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestor de Inventario y Pedidos (Arquitectura Ampliada)")
                        .version("3.0.0")
                        .description("Módulo encargado de controlar la disponibilidad de entradas, pases y productos de merchandising, así como de registrar las compras realizadas por los clientes."))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Servidor Local de Desarrollo"),
                        new Server().url("https://api.subsonicfestival.com/v1").description("API Gateway")
                ))
                .components(new Components()
                        .addSecuritySchemes("OAuth2", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Introduzca el token Bearer JWT")))
                .addSecurityItem(new SecurityRequirement().addList("OAuth2"));
    }
}
