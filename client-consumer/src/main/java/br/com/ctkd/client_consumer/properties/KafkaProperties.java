package br.com.ctkd.client_consumer.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaProperties(
        String bootstrapServers,
        Consumer consumer
) {
    public record Consumer(
            String clientGroupId,
            String addressGroupId
    ) {}
}
