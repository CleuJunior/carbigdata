package br.com.ctkd.repository;

import br.com.ctkd.config.JpaAuditingLogConfig;
import br.com.ctkd.config.TestcontainersConfiguration;
import br.com.ctkd.domain.Address;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

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
class AddressRepositoryIT {

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldFindByIdAndNotDeleted() {
        var address = new Address();

        address.setStreetName("Mario Lei");
        address.setNeighborhood("Alamo");
        address.setZipCode("2993812");
        address.setCity("Santos");
        address.setState("Bahia");

        addressRepository.save(address);

        var addressOptional = addressRepository.findById(UUID.fromString(address.getId()));

        assertThat(addressOptional)
                .isPresent()
                .get()
                .satisfies(addr -> {
                    assertThat(addr.getStreetName()).isEqualTo("Mario Lei");
                    assertThat(addr.getNeighborhood()).isEqualTo("Alamo");
                    assertThat(addr.getZipCode()).isEqualTo("2993812");
                    assertThat(addr.getCity()).isEqualTo("Santos");
                    assertThat(addr.getState()).isEqualTo("Bahia");
                });
    }

    @Test
    void shouldNotFindByIdWhenAddressDeleted() {
        var address = new Address();

        address.setStreetName("Mario Lei");
        address.setNeighborhood("Alamo");
        address.setZipCode("2993812");
        address.setCity("Santos");
        address.setState("Bahia");
        address.setDeleted(true);

        addressRepository.save(address);

        var clientOptional = addressRepository.findById(UUID.fromString(address.getId()));

        assertThat(clientOptional)
                .isNotPresent();
    }

    @Test
    void shouldGetAddressesListNotDeleted() {
        var addressOne = new Address();

        addressOne.setStreetName("Flowers Street");
        addressOne.setNeighborhood("Downtown");
        addressOne.setZipCode("20040-020");
        addressOne.setCity("Rio de Janeiro");
        addressOne.setState("RJ");

        var addressTwo = new Address();

        addressTwo.setStreetName("Paulista Avenue");
        addressTwo.setNeighborhood("Bela Vista");
        addressTwo.setZipCode("01310-100");
        addressTwo.setCity("Sao Paulo");
        addressTwo.setState("SP");

        var addressThree = new Address();

        addressThree.setStreetName("Commerce Street");
        addressThree.setNeighborhood("Boa Viagem");
        addressThree.setZipCode("51011-000");
        addressThree.setCity("Recife");
        addressThree.setState("PE");
        addressThree.setDeleted(true);


        addressRepository.saveAll(List.of(addressOne, addressTwo, addressThree));

        var result = addressRepository.findAll();

        assertThat(result)
                .containsExactlyInAnyOrder(addressOne, addressTwo);
    }
}
