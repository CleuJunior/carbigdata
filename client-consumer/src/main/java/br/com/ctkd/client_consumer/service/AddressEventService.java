package br.com.ctkd.client_consumer.service;

import br.com.ctkd.client_consumer.domain.AddressView;
import br.com.ctkd.client_consumer.domain.ClientView;
import br.com.ctkd.client_consumer.dto.event.AddressEventDto;
import br.com.ctkd.client_consumer.dto.event.ClientEventDto;
import br.com.ctkd.client_consumer.mapper.ClientMapper;
import br.com.ctkd.client_consumer.repository.AddressDocumentRepository;
import br.com.ctkd.client_consumer.repository.ClientDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressEventService {

    private final AddressDocumentRepository repository;

    public void process(AddressEventDto event) {
        log.info("Processing address event: id={}", event.id());

        var addressView = repository.findByAddressId(event.id())
                .orElseGet(AddressView::new);


        addressView.setAddressId(event.id());
        addressView.setStreetName(event.streetName());
        addressView.setNeighborhood(event.neighborhood());
        addressView.setZipCode(event.zipCode());
        addressView.setCity(event.city());
        addressView.setState(event.state());
        addressView.setDeleted(event.deleted());

        repository.save(addressView);

        log.info("Address saved successfully: id={}", event.id());
    }
}
