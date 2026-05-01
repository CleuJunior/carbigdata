package br.com.ctkd.controller;

import br.com.ctkd.config.TestcontainersConfiguration;
import br.com.ctkd.domain.Address;
import br.com.ctkd.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
        "DELETE FROM photos_occurrence",
        "DELETE FROM occurrences",
        "DELETE FROM clients",
        "DELETE FROM addresses"
})
class AddressControllerIT {

    private static final String BASE_PATH = "/api/v1/address";

    @LocalServerPort
    private int port;

    @Autowired
    private AddressRepository addressRepository;

    private RestClient restClient;
    private RestClient authClient;

    @BeforeEach
    void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
                .build();

        var loginResponse = restClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", "admin", "password", "admin"))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);
        var token = (String) loginResponse.getBody().get("token");

        authClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
                .build();
    }

    @Test
    void shouldGetAddressById() {
        var saved = savedAddress("Main Street", "Downtown", "01310-100", "Sao Paulo", "SP");

        var response = authClient.get()
                .uri(BASE_PATH + "/" + saved.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody())
                .containsEntry("streetName", "Main Street")
                .containsEntry("city", "Sao Paulo")
                .containsEntry("deleted", false);
    }

    @Test
    void shouldReturn404WhenAddressNotFound() {
        var response = authClient.get()
                .uri(BASE_PATH + "/" + UUID.randomUUID())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldListAddresses() {
        savedAddress("Main Street", "Downtown", "01310-100", "Sao Paulo", "SP");
        savedAddress("Paulista Avenue", "Bela Vista", "01310-200", "Sao Paulo", "SP");

        var response = authClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnPagedAddresses() {
        savedAddress("Main Street", "Downtown", "01310-100", "Sao Paulo", "SP");
        savedAddress("Paulista Avenue", "Bela Vista", "01310-200", "Sao Paulo", "SP");

        var response = authClient.get()
                .uri(BASE_PATH + "/pageable?page=0&size=10")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat((List<?>) response.getBody().get("content")).hasSize(2);
        assertThat(response.getBody())
                .containsEntry("totalElements", 2)
                .containsEntry("totalPages", 1)
                .containsEntry("number", 0)
                .containsEntry("size", 10);
    }

    @Test
    void shouldCreateAddress() {
        var response = authClient.post()
                .uri(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "streetName", "Ocean Drive",
                        "neighborhood", "Beachfront",
                        "zipCode", "33139-001",
                        "city", "Miami",
                        "state", "FL"
                ))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody())
                .containsKey("id")
                .containsEntry("streetName", "Ocean Drive")
                .containsEntry("city", "Miami")
                .containsEntry("deleted", false);
    }

    @Test
    void shouldUpdateAddress() {
        var saved = savedAddress("Old Street", "Old Hood", "00000-000", "Old City", "OC");

        var response = authClient.put()
                .uri(BASE_PATH + "/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "streetName", "New Street",
                        "neighborhood", "New Hood",
                        "zipCode", "11111-111",
                        "city", "New City",
                        "state", "NC"
                ))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(202);
        assertThat(response.getBody())
                .containsEntry("streetName", "New Street")
                .containsEntry("city", "New City");
    }

    @Test
    void shouldSoftDeleteAddress() {
        var saved = savedAddress("To Delete Street", "Old Hood", "00000-000", "Old City", "OC");

        var deleteResponse = authClient.delete()
                .uri(BASE_PATH + "/" + saved.getId())
                .retrieve()
                .toBodilessEntity();
        assertThat(deleteResponse.getStatusCode().value()).isEqualTo(204);

        var getResponse = authClient.get()
                .uri(BASE_PATH + "/" + saved.getId())
                .retrieve()
                .toBodilessEntity();
        assertThat(getResponse.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() {
        var response = restClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(401);
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
