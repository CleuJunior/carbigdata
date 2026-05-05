package br.com.ctkd.client_consumer.service;

import br.com.ctkd.client_consumer.document.ClientDocument;
import br.com.ctkd.client_consumer.event.ClientEvent;
import br.com.ctkd.client_consumer.repository.ClientDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientEventService {

    private final ClientDocumentRepository repository;

    public void process(ClientEvent event) {
        log.info("Processing client event: id={}, type={}", event.getId(), event.getEventType());

        var document = repository.findByClientId(event.getId())
                .orElseGet(ClientDocument::new);

        document.setClientId(event.getId());
        document.setName(event.getName());
        document.setBirthdate(event.getBirthdate());
        document.setCpf(event.getCpf());
        document.setEventType(event.getEventType());

        repository.save(document);

        log.info("Client saved successfully: id={}", event.getId());
    }
}
