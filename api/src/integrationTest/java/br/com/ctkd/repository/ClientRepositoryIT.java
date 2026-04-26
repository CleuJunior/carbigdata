package br.com.ctkd.repository;

import br.com.ctkd.config.JpaAuditingLogConfig;
import br.com.ctkd.config.TestcontainersConfiguration;
import br.com.ctkd.domain.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaAuditingLogConfig.class})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Sql(statements = {
        "DELETE FROM photos_occurrence",
        "DELETE FROM occurrences",
        "DELETE FROM clients",
        "DELETE FROM addresses"
})
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

    @Test
    void shouldGetClientsListNotDeleted() {
        var clientOne = new Client();
        clientOne.setName("Gabriela Nunes");
        clientOne.setBirthdate(LocalDate.of(2000, 10, 23));
        clientOne.setCpf("798.543.950-14");
        clientOne.setDeleted(false);

        var clientTwo = new Client();
        clientTwo.setName("Alvarez Paz");
        clientTwo.setBirthdate(LocalDate.of(1983, 11, 3));
        clientTwo.setCpf("918.432.740-51");
        clientTwo.setDeleted(false);

        var clientThree = new Client();
        clientThree.setName("Alvarez Paz");
        clientThree.setBirthdate(LocalDate.of(1978, 3, 28));
        clientThree.setCpf("208.276.380-35");
        clientThree.setDeleted(true);

        clientRepository.saveAll(List.of(clientOne, clientTwo, clientThree));

        var result = clientRepository.findAll();

        assertThat(result)
                .containsExactlyInAnyOrder(clientOne, clientTwo);
    }

    @Test
    void shouldCountClientsNotDeleted() {
        var clientOne = new Client();
        clientOne.setName("Taihau Fyuze");
        clientOne.setBirthdate(LocalDate.of(1961, 7, 21));
        clientOne.setCpf("700.062.580-50");
        clientOne.setDeleted(false);

        var clientTwo = new Client();
        clientTwo.setName("Cidaoze Loir");
        clientTwo.setBirthdate(LocalDate.of(1986, 12, 31));
        clientTwo.setCpf("125.585.760-97");
        clientTwo.setDeleted(false);

        var clientThree = new Client();
        clientThree.setName("Biubuas Dobiamo");
        clientThree.setBirthdate(LocalDate.of(1947, 2, 23));
        clientThree.setCpf("470.486.950-48");
        clientThree.setDeleted(true);

        clientRepository.saveAll(List.of(clientOne, clientTwo, clientThree));

        var result = clientRepository.count();

        assertThat(result)
                .isEqualTo(2);
    }
}
