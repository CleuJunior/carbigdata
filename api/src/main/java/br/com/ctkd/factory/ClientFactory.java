package br.com.ctkd.factory;

import br.com.ctkd.domain.Client;
import br.com.ctkd.dto.response.ClientResponse;
import br.com.ctkd.dto.request.ClientRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientFactory {

    public ClientResponse toClientResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .birthdate(client.getBirthdate())
                .cpf(client.getCpf())
                .creationDate(client.getCreationDate())
                .updateDate(client.getUpdateDate())
                .deleted(client.isDeleted())
                .build();
    }

    public List<ClientResponse> toClientResponse(List<Client> clients) {
        return clients.stream()
                .map(this::toClientResponse)
                .toList();
    }

    public Client toClient(ClientRequest request) {
        var client = new Client();

        client.setName(request.name());
        client.setBirthdate(request.birthdate());
        client.setCpf(request.cpf());

        return client;
    }
}
