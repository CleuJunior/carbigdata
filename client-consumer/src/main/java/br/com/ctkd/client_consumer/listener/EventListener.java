package br.com.ctkd.client_consumer.listener;

import br.com.ctkd.client_consumer.dto.event.AddressEventDto;
import br.com.ctkd.client_consumer.dto.event.ClientEventDto;
import br.com.ctkd.client_consumer.service.AddressEventService;
import br.com.ctkd.client_consumer.service.ClientEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    private final ClientEventService clientEventService;
    private final AddressEventService addressEventService;

    @Qualifier("clientEventListenerContainerFactory")
    @KafkaListener(topics = "${kafka.topics.client-events}", groupId = "${spring.kafka.consumer.client-group-id}", containerFactory = "clientEventListenerContainerFactory")
    public void clientListener(ClientEventDto event) {
        log.info("[Client Event received]: payload: {}", event.toString());
        clientEventService.process(event);
        log.info("Client Event saved in the database");
    }

    @Qualifier("clientEventListenerContainerFactory")
    @KafkaListener(topics = "${kafka.topics.address-events}", groupId = "${spring.kafka.consumer.address-group-id}", containerFactory = "addressEventListenerContainerFactory")
    public void addressListener(AddressEventDto event) {
        log.info("[Address Event received]: payload: {}", event.toString());
        addressEventService.process(event);
        log.info("Address Event saved in the database");
    }
}
