package br.com.ctkd.producer;

import br.com.ctkd.domain.Address;
import br.com.ctkd.domain.Client;
import br.com.ctkd.dto.event.AddressEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressEventProducer {

    private final KafkaTemplate<String, AddressEvent> addressEventKafkaTemplate;

    @Value("${kafka.topics.address-events}")
    private String topic;

    public void publish(Address address) {
        var event = AddressEvent.builder()
                .id(address.getId())
                .streetName(address.getStreetName())
                .neighborhood(address.getNeighborhood())
                .zipCode(address.getZipCode())
                .city(address.getCity())
                .state(address.getState())
                .build();

        log.info("[Sending event]: [{}], for topic: {}", event, topic);
        addressEventKafkaTemplate.send(topic, address.getId(), event);
        log.info("Address event sent successfully");
    }
}
