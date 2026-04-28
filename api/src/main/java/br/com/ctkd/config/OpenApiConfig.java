package br.com.ctkd.config;

import br.com.ctkd.properties.SwaggerProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final SwaggerProperties properties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(info())
                .addSecurityItem(new SecurityRequirement().addList(properties.auth()))
                .components(components());
    }

    private Info info() {
        return new Info()
                .title(properties.tile())
                .description(properties.description())
                .version(properties.version());
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes(properties.auth(), new SecurityScheme()
                        .name(properties.auth())
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
