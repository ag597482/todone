package com.indra.todone.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Relative URL: Swagger UI uses the same host as the current page (localhost, Railway, etc.)
        Server server = new Server()
                .url("/")
                .description("Current host");
        return new OpenAPI()
                .servers(List.of(server));
    }
}
