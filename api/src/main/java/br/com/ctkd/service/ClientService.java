package br.com.ctkd.service;

import br.com.ctkd.domain.Client;
import br.com.ctkd.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;

    public Client getById(String id) {
        var uuid = UUID.fromString(id);

        return repository.findById(uuid)
                .orElseThrow();
    }

    public List<Client> getListClients() {
        return repository.findAll();
    }

    public Page<Client> pageClients(int page, int size) {
        var pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    public Client insertClient(Client client) {
        return repository.save(client);
    }

    public Client updateClient(Client request, String id) {
        var currentClient = getById(id);

        Optional.ofNullable(request.getName()).ifPresent(currentClient::setName);
        Optional.ofNullable(request.getBirthdate()).ifPresent(currentClient::setBirthdate);
        Optional.ofNullable(request.getCpf()).ifPresent(currentClient::setCpf);

        return repository.save(currentClient);
    }

    public void softDelete(String id) {
        var client = getById(id);

        client.setDeleted(true);

        repository.save(client);
    }
}
