package com.indra.todone.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();

        // Add local server
        servers.add(new Server().url("http://localhost:8080").description("Local Server"));

        // Add ngrok server (update this with your actual ngrok URL when needed)
        servers.add(new Server().url("https://de9a45e8d584.ngrok-free.app").description("Ngrok Server"));

        return new OpenAPI()
                .servers(servers);
    }
}
