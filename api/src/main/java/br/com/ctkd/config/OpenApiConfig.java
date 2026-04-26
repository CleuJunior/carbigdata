package br.com.ctkd.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String TITLE = "Carbigdata API";
    public static final String DESCRIPTION = "REST API for managing clients, addresses, occurrences and photo attachments. All endpoints except /api/v1/auth/login require a Bearer JWT token.";
    public static final String VERSION = "1.0.0";
    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(info())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(components());
    }

    private static Info info() {
        return new Info()
                .title(TITLE)
                .description(DESCRIPTION)
                .version(VERSION);
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .name(BEARER_AUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
