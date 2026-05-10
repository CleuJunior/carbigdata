package br.com.ctkd.mail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.ses")
public record SesProperties(
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        String from,
        String verifiedEmail
) {
}
