package br.com.ctkd.client_consumer.listener;

import br.com.ctkd.client_consumer.dto.event.ClientEventDto;
import br.com.ctkd.client_consumer.service.ClientEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final ClientEventService service;

    @KafkaListener(
            topics = "${kafka.topics.client-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "clientEventListenerContainerFactory"
    )
    public void clientListener(
            @Payload ClientEventDto event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received message: topic={}, partition={}, offset={}", topic, partition, offset);
        service.process(event);
    }
}
