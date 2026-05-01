package br.com.ctkd.repository;

import br.com.ctkd.config.JpaAuditingLogConfig;
import br.com.ctkd.config.TestcontainersConfiguration;
import br.com.ctkd.domain.Address;
import br.com.ctkd.domain.Client;
import br.com.ctkd.domain.Occurrence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
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
class OccurrenceRepositoryIT {

    @Autowired
    private OccurrenceRepository occurrenceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldFindByIdAndNotDeleted() {
        var client = savedClient("Fernanda Dias", LocalDate.of(1990, 6, 15), "700.062.580-50");
        var address = savedAddress("Rua das Palmeiras", "Jardim", "30100-000", "Belo Horizonte", "MG");
        var occurrence = new Occurrence();
        occurrence.setClient(client);
        occurrence.setAddress(address);
        occurrence.setOccurrenceDate(LocalDate.of(2024, 3, 10));
        occurrenceRepository.save(occurrence);

        var result = occurrenceRepository.findById(UUID.fromString(occurrence.getId()));

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(o -> {
                    assertThat(o.getOccurrenceDate()).isEqualTo(LocalDate.of(2024, 3, 10));
                    assertThat(o.isDeleted()).isFalse();
                });
    }

    @Test
    void shouldNotFindByIdWhenOccurrenceDeleted() {
        var client = savedClient("Rodrigo Pires", LocalDate.of(1985, 11, 20), "125.585.760-97");
        var address = savedAddress("Av. Brasil", "Centro", "20000-000", "Rio de Janeiro", "RJ");
        var occurrence = new Occurrence();
        occurrence.setClient(client);
        occurrence.setAddress(address);
        occurrence.setOccurrenceDate(LocalDate.of(2024, 2, 5));
        occurrence.setDeleted(true);
        occurrenceRepository.save(occurrence);

        var result = occurrenceRepository.findById(UUID.fromString(occurrence.getId()));

        assertThat(result).isNotPresent();
    }

    @Test
    void shouldGetOccurrencesListNotDeleted() {
        var client = savedClient("Simone Castro", LocalDate.of(1993, 4, 8), "470.486.950-48");
        var address = savedAddress("Rua Vergueiro", "Vila Mariana", "04101-000", "Sao Paulo", "SP");

        var activeOne = new Occurrence();
        activeOne.setClient(client);
        activeOne.setAddress(address);
        activeOne.setOccurrenceDate(LocalDate.of(2024, 4, 1));
        activeOne.setDeleted(false);

        var activeTwo = new Occurrence();
        activeTwo.setClient(client);
        activeTwo.setAddress(address);
        activeTwo.setOccurrenceDate(LocalDate.of(2024, 4, 15));
        activeTwo.setDeleted(false);

        var deleted = new Occurrence();
        deleted.setClient(client);
        deleted.setAddress(address);
        deleted.setOccurrenceDate(LocalDate.of(2024, 4, 20));
        deleted.setDeleted(true);

        occurrenceRepository.saveAll(List.of(activeOne, activeTwo, deleted));

        var result = occurrenceRepository.findAll();

        assertThat(result).containsExactlyInAnyOrder(activeOne, activeTwo);
    }

    @Test
    void shouldGetPagedOccurrencesNotDeleted() {
        var client = savedClient("Tiago Souza", LocalDate.of(1988, 9, 17), "208.276.380-35");
        var address = savedAddress("Rua Líbero Badaró", "Centro", "01008-000", "Sao Paulo", "SP");

        var activeOne = new Occurrence();
        activeOne.setClient(client);
        activeOne.setAddress(address);
        activeOne.setOccurrenceDate(LocalDate.of(2024, 5, 5));
        activeOne.setDeleted(false);

        var activeTwo = new Occurrence();
        activeTwo.setClient(client);
        activeTwo.setAddress(address);
        activeTwo.setOccurrenceDate(LocalDate.of(2024, 5, 18));
        activeTwo.setDeleted(false);

        var deletedOccurrence = new Occurrence();
        deletedOccurrence.setClient(client);
        deletedOccurrence.setAddress(address);
        deletedOccurrence.setOccurrenceDate(LocalDate.of(2024, 5, 25));
        deletedOccurrence.setDeleted(true);

        occurrenceRepository.saveAll(List.of(activeOne, activeTwo, deletedOccurrence));

        var page = occurrenceRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).containsExactlyInAnyOrder(activeOne, activeTwo);
    }

    private Client savedClient(String name, LocalDate birthdate, String cpf) {
        var client = new Client();
        client.setName(name);
        client.setBirthdate(birthdate);
        client.setCpf(cpf);
        return clientRepository.save(client);
    }

    private Address savedAddress(String streetName, String neighborhood, String zipCode, String city, String state) {
        var address = new Address();
        address.setStreetName(streetName);
        address.setNeighborhood(neighborhood);
        address.setZipCode(zipCode);
        address.setCity(city);
        address.setState(state);
        return addressRepository.save(address);
    }
}
