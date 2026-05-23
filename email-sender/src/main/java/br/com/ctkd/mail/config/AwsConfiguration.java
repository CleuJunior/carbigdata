package br.com.ctkd.mail.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import java.net.URI;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SesProperties.class)
public class AwsConfiguration {

    private final SesProperties properties;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(properties.region()))
                .endpointOverride(URI.create(properties.endpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(getCredentials()))
                .build();
    }

    @Bean
    public ApplicationRunner verifyEmailIdentity(SesClient sesClient) {
        return args -> {
            sesClient.verifyEmailIdentity(r -> r.emailAddress(properties.from()));
            log.info("SES email verificado: {}", properties.from());
        };
    }

    private AwsBasicCredentials getCredentials() {
        return AwsBasicCredentials.create(
                properties.accessKey(),
                properties.secretKey()
        );
    }
}
