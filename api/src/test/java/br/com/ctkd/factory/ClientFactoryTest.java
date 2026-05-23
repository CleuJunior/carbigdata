package br.com.ctkd.factory;

import br.com.ctkd.domain.Client;
import br.com.ctkd.dto.request.ClientRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ClientFactoryTest {

    @Mock
    private Client client;
    @InjectMocks
    private ClientFactory underTest;

    @Test
    void shouldMapClientToClientResponse() {
        var id = "client-id";
        var name = "client-name";
        var birthdate = LocalDate.now();
        var cpf = "641.916.530-08";
        var creationDate = OffsetDateTime.now();
        var updateDate = OffsetDateTime.now();

        given(client.getId()).willReturn(id);
        given(client.getName()).willReturn(name);
        given(client.getBirthdate()).willReturn(birthdate);
        given(client.getCpf()).willReturn(cpf);
        given(client.getCreationDate()).willReturn(creationDate);
        given(client.getUpdateDate()).willReturn(updateDate);
        given(client.isDeleted()).willReturn(false);

        var result = underTest.toClientResponse(client);

        then(result.id()).isEqualTo(id);
        then(result.name()).isEqualTo(name);
        then(result.birthdate()).isEqualTo(birthdate);
        then(result.cpf()).isEqualTo(cpf);
        then(result.creationDate()).isEqualTo(creationDate);
        then(result.updateDate()).isEqualTo(updateDate);
        then(result.deleted()).isEqualTo(false);
    }

    @Test
    void shouldMapClientListToClientResponseList() {
        var id = "client-id";
        var name = "client-name";
        var birthdate = LocalDate.now();
        var cpf = "641.916.530-08";
        var creationDate = OffsetDateTime.now();
        var updateDate = OffsetDateTime.now();

        given(client.getId()).willReturn(id);
        given(client.getName()).willReturn(name);
        given(client.getBirthdate()).willReturn(birthdate);
        given(client.getCpf()).willReturn(cpf);
        given(client.getCreationDate()).willReturn(creationDate);
        given(client.getUpdateDate()).willReturn(updateDate);
        given(client.isDeleted()).willReturn(false);

        var result = underTest.toClientResponse(Collections.singletonList(client));

        then(result.size()).isEqualTo(1);
        then(result.getFirst().id()).isEqualTo(id);
        then(result.getFirst().name()).isEqualTo(name);
        then(result.getFirst().birthdate()).isEqualTo(birthdate);
        then(result.getFirst().cpf()).isEqualTo(cpf);
        then(result.getFirst().creationDate()).isEqualTo(creationDate);
        then(result.getFirst().updateDate()).isEqualTo(updateDate);
        then(result.getFirst().deleted()).isEqualTo(false);
    }

    @Test
    void shouldMapClientRequestToClient() {
        var request = mock(ClientRequest.class);

        given(request.name()).willReturn("client-name");
        given(request.birthdate()).willReturn(LocalDate.now());
        given(request.cpf()).willReturn("641.916.530-08");

        var result = underTest.toClient(request);

        then(result.getName()).isEqualTo("client-name");
        then(result.getBirthdate()).isEqualTo(LocalDate.now());
        then(result.getCpf()).isEqualTo("641.916.530-08");
    }
}