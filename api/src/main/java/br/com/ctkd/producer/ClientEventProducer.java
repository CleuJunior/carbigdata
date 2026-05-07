package br.com.ctkd.producer;

import br.com.ctkd.domain.Client;
import br.com.ctkd.dto.event.ClientEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientEventProducer {

    private final KafkaTemplate<String, ClientEvent> clientEventKafkaTemplate;

    @Value("${kafka.topics.client-events}")
    private String topic;

    public void publish(Client client, String eventType) {
        var event = new ClientEvent(
                client.getId(),
                client.getName(),
                client.getBirthdate(),
                client.getCpf(),
                eventType
        );

        clientEventKafkaTemplate.send(topic, client.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish client event: id={}, type={}", client.getId(), eventType, ex);
                    } else {
                        log.info("Published client event: id={}, type={}, offset={}",
                                client.getId(), eventType, result.getRecordMetadata().offset());
                    }
                });
    }

    public void publish(Client ...clients) {
        List.of(clients).forEach(c -> publish(c, "CREATED"));
    }
}
