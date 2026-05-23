package br.com.ctkd.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "doc.swagger")
public record SwaggerProperties(
        String tile,
        String description,
        String version,
        String auth
) {

}
