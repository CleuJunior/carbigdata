package br.com.ctkd.client_consumer.service;

import br.com.ctkd.client_consumer.domain.ClientView;
import br.com.ctkd.client_consumer.dto.event.ClientEventDto;
import br.com.ctkd.client_consumer.repository.ClientDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientEventService {

    private final ClientDocumentRepository repository;

    public void process(ClientEventDto event) {
        log.info("Processing client event: id={}", event.id());

        var document = repository.findByClientId(event.id())
                .orElseGet(ClientView::new);

        document.setClientId(event.id());
        document.setName(event.name());
        document.setBirthdate(event.birthdate());
        document.setCpf(event.cpf());
        document.setDeleted(event.deleted());

        repository.save(document);

        log.info("Client saved successfully: id={}", event.id());
    }
}
