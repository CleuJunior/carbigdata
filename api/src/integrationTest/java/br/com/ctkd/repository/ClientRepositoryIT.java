package br.com.ctkd.repository;

import br.com.ctkd.config.JpaAuditingLogConfig;
import br.com.ctkd.domain.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import(JpaAuditingLogConfig.class)
@ActiveProfiles("test")
class ClientRepositoryIT {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void shouldFindByIdAndNotDeleted() {
        var client = new Client();
        client.setName("Fernando Anapolis");
        client.setBirthdate(LocalDate.of(1989, 12, 30));
        client.setCpf("262.224.900-42");

        clientRepository.save(client);

        var clientOptional = clientRepository.findById(UUID.fromString(client.getId()));

        assertThat(clientOptional)
                .isPresent()
                .get()
                .satisfies(c -> {
                    assertThat(c.getName()).isEqualTo("Fernando Anapolis");
                    assertThat(c.getCpf()).isEqualTo("262.224.900-42");
                    assertThat(c.getBirthdate()).isEqualTo(LocalDate.of(1989, 12, 30));
                });
    }

    @Test
    void shouldNotFindByIdWhenClientDeleted() {
        var client = new Client();
        client.setName("Rafael Menezes");
        client.setBirthdate(LocalDate.of(1999, 11, 8));
        client.setCpf("247.146.330-40");
        client.setDeleted(true);

        clientRepository.save(client);

        var clientOptional = clientRepository.findById(UUID.fromString(client.getId()));

        assertThat(clientOptional)
                .isNotPresent();
    }
}