package br.com.ctkd.controller;

import br.com.ctkd.config.TestcontainersConfiguration;
import br.com.ctkd.domain.Address;
import br.com.ctkd.domain.Client;
import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.StatusOccurrence;
import br.com.ctkd.repository.AddressRepository;
import br.com.ctkd.repository.ClientRepository;
import br.com.ctkd.repository.OccurrenceRepository;
import br.com.ctkd.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
        "DELETE FROM photos_occurrence",
        "DELETE FROM occurrences",
        "DELETE FROM clients",
        "DELETE FROM addresses"
})
class OccurrenceControllerIT {

    private static final String BASE_PATH = "/api/v1/occurrences";

    @LocalServerPort
    private int port;

    @MockitoBean
    private S3Service s3Service;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OccurrenceRepository occurrenceRepository;

    private RestClient restClient;
    private RestClient authClient;

    @BeforeEach
    void setup() {
        when(s3Service.generatePresignedUrl(anyString())).thenReturn("https://s3.example.com/test.jpg");

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
    void shouldGetOccurrenceById() {
        var client = savedClient("Ana Costa", LocalDate.of(1995, 3, 22), "700.062.580-50");
        var address = savedAddress("Rua das Flores", "Centro", "40010-000", "Salvador", "BA");
        var saved = savedOccurrence(client, address, LocalDate.of(2024, 5, 10), false);

        var response = authClient.get()
                .uri(BASE_PATH + "/" + saved.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody())
                .containsKey("id")
                .containsEntry("occurrenceDate", "2024-05-10")
                .containsEntry("status", "ACTIVE")
                .containsEntry("deleted", false);
    }

    @Test
    void shouldReturn404WhenOccurrenceNotFound() {
        var response = authClient.get()
                .uri(BASE_PATH + "/" + UUID.randomUUID())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldListOccurrences() {
        var client = savedClient("Bruno Melo", LocalDate.of(1988, 7, 14), "125.585.760-97");
        var address = savedAddress("Av. Atlântica", "Copacabana", "22010-000", "Rio de Janeiro", "RJ");
        savedOccurrence(client, address, LocalDate.of(2024, 1, 5), false);
        savedOccurrence(client, address, LocalDate.of(2024, 2, 20), false);

        var response = authClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnPagedOccurrences() {
        var client = savedClient("Carla Lima", LocalDate.of(1991, 11, 30), "470.486.950-48");
        var address = savedAddress("Rua Augusta", "Consolação", "01310-000", "Sao Paulo", "SP");
        savedOccurrence(client, address, LocalDate.of(2024, 3, 1), false);
        savedOccurrence(client, address, LocalDate.of(2024, 3, 15), false);

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
    void shouldCreateOccurrence() {
        var client = savedClient("Diego Rocha", LocalDate.of(1993, 6, 18), "208.276.380-35");
        var address = savedAddress("Rua Boa Vista", "Centro", "01014-000", "Sao Paulo", "SP");

        var dataJson = """
                {"clientId": "%s", "addressId": "%s", "occurrenceDate": "2024-06-15"}
                """.formatted(client.getId(), address.getId());

        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_JSON);

        var multipart = new LinkedMultiValueMap<String, Object>();
        multipart.add("data", new HttpEntity<>(dataJson, partHeaders));

        var response = authClient.post()
                .uri(BASE_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(multipart)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody())
                .containsKey("id")
                .containsEntry("occurrenceDate", "2024-06-15")
                .containsEntry("status", "ACTIVE")
                .containsEntry("deleted", false);
    }

    @Test
    void shouldReturn404WhenCreatingOccurrenceWithUnknownClient() {
        var address = savedAddress("Rua do Comercio", "Centro", "01010-000", "Sao Paulo", "SP");

        var dataJson = """
                {"clientId": "%s", "addressId": "%s", "occurrenceDate": "2024-06-15"}
                """.formatted(UUID.randomUUID(), address.getId());

        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_JSON);

        var multipart = new LinkedMultiValueMap<String, Object>();
        multipart.add("data", new HttpEntity<>(dataJson, partHeaders));

        var response = authClient.post()
                .uri(BASE_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(multipart)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldCloseOccurrence() {
        var client = savedClient("Elisa Nunes", LocalDate.of(1987, 9, 5), "918.432.740-51");
        var address = savedAddress("Av. Paulista", "Bela Vista", "01310-100", "Sao Paulo", "SP");
        var saved = savedOccurrence(client, address, LocalDate.of(2024, 4, 10), false);

        var response = authClient.patch()
                .uri(BASE_PATH + "/" + saved.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "FINISHED");
    }

    @Test
    void shouldReturn409WhenClosingAlreadyFinishedOccurrence() {
        var client = savedClient("Felipe Torres", LocalDate.of(1984, 2, 28), "798.543.950-14");
        var address = savedAddress("Rua da Consolação", "Consolação", "01301-000", "Sao Paulo", "SP");
        var saved = savedOccurrence(client, address, LocalDate.of(2024, 1, 20), false);

        authClient.patch().uri(BASE_PATH + "/" + saved.getId()).retrieve().toBodilessEntity();

        var conflictResponse = authClient.patch()
                .uri(BASE_PATH + "/" + saved.getId())
                .retrieve()
                .toBodilessEntity();

        assertThat(conflictResponse.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void shouldSearchOccurrencesByClientName() {
        var clientA = savedClient("Marco Aurelio", LocalDate.of(1990, 4, 12), "641.916.530-08");
        var clientB = savedClient("Paula Fernanda", LocalDate.of(1992, 8, 25), "262.224.900-42");
        var address = savedAddress("Rua X", "Bairro Y", "00000-001", "Curitiba", "PR");
        savedOccurrence(clientA, address, LocalDate.of(2024, 7, 1), false);
        savedOccurrence(clientB, address, LocalDate.of(2024, 7, 2), false);

        var response = authClient.post()
                .uri(BASE_PATH + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("clientName", "Marco", "page", 0, "size", 10))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat((List<?>) response.getBody().get("content")).hasSize(1);
    }

    @Test
    void shouldSearchOccurrencesByCity() {
        var client = savedClient("Gisele Brum", LocalDate.of(1996, 1, 8), "247.146.330-40");
        var addressSP = savedAddress("Rua A", "Centro", "01000-000", "Sao Paulo", "SP");
        var addressRJ = savedAddress("Rua B", "Centro", "20000-000", "Rio de Janeiro", "RJ");
        savedOccurrence(client, addressSP, LocalDate.of(2024, 8, 1), false);
        savedOccurrence(client, addressRJ, LocalDate.of(2024, 8, 2), false);

        var response = authClient.post()
                .uri(BASE_PATH + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("city", "paulo", "page", 0, "size", 10))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat((List<?>) response.getBody().get("content")).hasSize(1);
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() {
        var response = restClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(401);
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

    private Occurrence savedOccurrence(Client client, Address address, LocalDate date, boolean deleted) {
        var occurrence = new Occurrence();
        occurrence.setClient(client);
        occurrence.setAddress(address);
        occurrence.setOccurrenceDate(date);
        occurrence.setDeleted(deleted);
        return occurrenceRepository.save(occurrence);
    }
}
