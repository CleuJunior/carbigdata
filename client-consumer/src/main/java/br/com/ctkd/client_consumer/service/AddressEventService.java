package br.com.ctkd.client_consumer.service;

import br.com.ctkd.client_consumer.domain.AddressView;
import br.com.ctkd.client_consumer.dto.event.AddressEventDto;
import br.com.ctkd.client_consumer.enums.EventType;
import br.com.ctkd.client_consumer.mapper.AddressMapper;
import br.com.ctkd.client_consumer.repository.AddressDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressEventService {

    private final AddressDocumentRepository repository;
    private final AddressMapper mapper;

    public void process(AddressEventDto event) {
        log.info("Processing address event: id={}, for type: {}", event.id(), event.eventType());

        if (event.eventType() == EventType.CREATED) {
            var addressView = mapper.fromEvent(event);
            repository.save(addressView);
            log.info("Address saved successfully: id={}", event.id());
            return;
        }

        if (event.eventType() == EventType.UPDATED) {
            repository.findByAddressId(event.id())
                    .ifPresent(address -> {
                        var addressUpdated = mapper.apply(address, event);
                        repository.save(addressUpdated);
                        log.info("Address updated");
                    });
            return;
        }

        if (event.eventType() == EventType.DELETED) {
            repository.findByAddressId(event.id())
                    .ifPresent(address -> {
                        address.setDeleted(true);
                        repository.save(address);
                        log.info("Address deleted");
                    });
            return;
        }

        log.warn("Event type incorrectly");
    }
}
