package br.com.ctkd.client_consumer.service;

import br.com.ctkd.client_consumer.domain.ClientView;
import br.com.ctkd.client_consumer.dto.event.ClientEventDto;
import br.com.ctkd.client_consumer.mapper.ClientMapper;
import br.com.ctkd.client_consumer.repository.ClientDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientEventService {

    private final ClientDocumentRepository repository;
    private final ClientMapper mapper;

    public void process(ClientEventDto event) {
        log.info("Processing client event: id={}", event.id());

        var clientView = repository.findByClientId(event.id())
                .orElseGet(ClientView::new);

        clientView.setClientId(event.id());
        clientView.setName(event.name());
        clientView.setBirthdate(event.birthdate());
        clientView.setCpf(event.cpf());
        clientView.setDeleted(event.deleted());

        repository.save(clientView);

        log.info("Client saved successfully: id={}", event.id());
    }
}
