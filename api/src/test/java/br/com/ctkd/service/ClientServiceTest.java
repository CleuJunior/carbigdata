package br.com.ctkd.service;


import br.com.ctkd.domain.Client;
import br.com.ctkd.exceptions.NotFoundException;
import br.com.ctkd.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private Client client;
    @Mock
    private ClientRepository repository;
    @InjectMocks
    private ClientService underTest;

    @BeforeEach
    void setup() {
        client = new Client();
        client.setName("Carlos Silva");
        client.setBirthdate(LocalDate.of(2008, 2, 23));
        client.setCpf("123.456.789-00");
    }

    @Test
    void shouldGetClientById() {
        var uuid = UUID.randomUUID();

        given(repository.findById(uuid)).willReturn(Optional.of(client));

        var result = underTest.getById(uuid.toString());

        then(result.getName()).isEqualTo("Carlos Silva");
        then(result.getBirthdate()).isEqualTo(LocalDate.of(2008, 2, 23));
        then(result.getCpf()).isEqualTo("123.456.789-00");

        verify(repository).findById(uuid);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowWhenClientNotFound() {
        var uuid = UUID.randomUUID();

        given(repository.findById(uuid)).willReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getById(uuid.toString()))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findById(uuid);
    }

    @Test
    void getListClients() {
        given(repository.findAll()).willReturn(Collections.singletonList(client));

        var result = underTest.getListClients();

        then(result.size()).isEqualTo(1);
        then(result.getFirst().getName()).isEqualTo("Carlos Silva");
        then(result.getFirst().getBirthdate()).isEqualTo(LocalDate.of(2008, 2, 23));
        then(result.getFirst().getCpf()).isEqualTo("123.456.789-00");

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldReturnPagedClients() {
        var pageable = PageRequest.of(0, 10);
        var clientsPage = new PageImpl<>(List.of(client), pageable, 1);

        given(repository.findAll(pageable)).willReturn(clientsPage);

        var result = underTest.pageClients(0, 10);

        then(result).isNotNull();
        then(result.getContent()).hasSize(1);
        then(result.getTotalElements()).isEqualTo(1);
        then(result.getTotalPages()).isEqualTo(1);
        then(result.getNumber()).isEqualTo(0);
        then(result.getSize()).isEqualTo(10);
        then(result.getContent()).containsExactlyInAnyOrder(client);

        verify(repository).findAll(pageable);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldInsertClient() {
        var clientToInsert = mock(Client.class);

        given(repository.save(clientToInsert)).willReturn(client);

        var result = underTest.insertClient(clientToInsert);

        then(result.getName()).isEqualTo("Carlos Silva");
        then(result.getBirthdate()).isEqualTo(LocalDate.of(2008, 2, 23));
        then(result.getCpf()).isEqualTo("123.456.789-00");

        verify(repository).save(clientToInsert);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldUpdateClient() {
        var uuid = UUID.randomUUID();
        var clientToUpdate = mock(Client.class);

        given(repository.findById(uuid)).willReturn(Optional.of(client));
        given(repository.save(client)).willReturn(client);

        var result = underTest.updateClient(clientToUpdate, uuid.toString());

        then(result.getName()).isEqualTo("Carlos Silva");
        then(result.getBirthdate()).isEqualTo(LocalDate.of(2008, 2, 23));
        then(result.getCpf()).isEqualTo("123.456.789-00");

        verify(repository).findById(uuid);
        verify(repository).save(client);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldSoftDelete() {
        var uuid = UUID.randomUUID();

        var captor = ArgumentCaptor.forClass(Client.class);

        given(repository.findById(uuid)).willReturn(Optional.of(client));
        given(repository.save(any(Client.class))).willReturn(client);

        underTest.softDelete(uuid.toString());

        verify(repository).save(captor.capture());

        var capturedClient = captor.getValue();

        then(capturedClient.isDeleted()).isTrue();

        verify(repository).findById(uuid);
        verifyNoMoreInteractions(repository);
    }
}