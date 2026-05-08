package br.com.ctkd.client_consumer.config;

import br.com.ctkd.client_consumer.dto.event.AddressEventDto;
import br.com.ctkd.client_consumer.dto.event.ClientEventDto;
import br.com.ctkd.client_consumer.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties properties;

    @Bean
    public ConsumerFactory<String, ClientEventDto> clientEventConsumerFactory() {
        var jsonDeserializer = new JsonDeserializer<>(ClientEventDto.class);
        jsonDeserializer.setRemoveTypeHeaders(false);
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers(),
                        ConsumerConfig.GROUP_ID_CONFIG, properties.consumer().clientGroupId(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    @Bean
    public ConsumerFactory<String, AddressEventDto> addressEventConsumerFactory() {
        var jsonDeserializer = new JsonDeserializer<>(AddressEventDto.class);
        jsonDeserializer.setRemoveTypeHeaders(false);
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers(),
                        ConsumerConfig.GROUP_ID_CONFIG, properties.consumer().addressGroupId(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClientEventDto> clientEventListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ClientEventDto>();
        factory.setConsumerFactory(clientEventConsumerFactory());
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AddressEventDto> addressEventListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, AddressEventDto>();
        factory.setConsumerFactory(addressEventConsumerFactory());
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }
}
