package com.abnamro.recipes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the SpringDoc OpenAPI metadata (title, version, contact, licence)
 * surfaced at {@code /api-docs} and rendered by Swagger UI at {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.contact.name}")
    private String contactName;

    @Value("${app.contact.email}")
    private String contactEmail;

    @Bean
    public OpenAPI recipeManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Recipe Manager API")
                        .description("REST API for managing favourite recipes. Supports CRUD operations and advanced filtering.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail))
                        .license(new License()
                                .name("Apache 2.0")));
    }
}
