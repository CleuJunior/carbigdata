package br.com.ctkd.controller;

import br.com.ctkd.domain.Client;
import br.com.ctkd.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ClientControllerIT {

    private static final String BASE_PATH = "/api/v1/clients";

    @LocalServerPort
    private int port;

    @Autowired
    private ClientRepository clientRepository;

    private RestClient restClient;
    private RestClient authClient;

    @BeforeEach
    void setup() {
        clientRepository.deleteAll();

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
    void shouldGetClientById() {
        var saved = savedClient("João Silva", LocalDate.of(1990, 5, 20), "641.916.530-08");

        var response = authClient.get()
                .uri(BASE_PATH + "/" + saved.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody())
                .containsEntry("name", "João Silva")
                .containsEntry("cpf", "641.916.530-08")
                .containsEntry("deleted", false);
    }

    @Test
    void shouldReturn404WhenClientNotFound() {
        var response = authClient.get()
                .uri(BASE_PATH + "/" + UUID.randomUUID())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldListClients() {
        savedClient("João Silva", LocalDate.of(1990, 5, 20), "641.916.530-08");
        savedClient("Maria Santos", LocalDate.of(1985, 3, 15), "262.224.900-42");

        var response = authClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnPagedClients() {
        savedClient("João Silva", LocalDate.of(1990, 5, 20), "641.916.530-08");
        savedClient("Maria Santos", LocalDate.of(1985, 3, 15), "262.224.900-42");

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
    void shouldCreateClient() {
        var response = authClient.post()
                .uri(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "name", "Carlos Oliveira",
                        "birthdate", "1992-07-10",
                        "cpf", "798.543.950-14"
                ))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody())
                .containsKey("id")
                .containsEntry("name", "Carlos Oliveira")
                .containsEntry("cpf", "798.543.950-14")
                .containsEntry("deleted", false);
    }

    @Test
    void shouldUpdateClient() {
        var saved = savedClient("João Silva", LocalDate.of(1990, 5, 20), "641.916.530-08");

        var response = authClient.put()
                .uri(BASE_PATH + "/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "name", "João Santos",
                        "birthdate", "1990-05-20",
                        "cpf", "641.916.530-08"
                ))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(202);
        assertThat(response.getBody())
                .containsEntry("name", "João Santos")
                .containsEntry("cpf", "641.916.530-08");
    }

    @Test
    void shouldSoftDeleteClient() {
        var saved = savedClient("João Silva", LocalDate.of(1990, 5, 20), "641.916.530-08");

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

    private Client savedClient(String name, LocalDate birthdate, String cpf) {
        var client = new Client();
        client.setName(name);
        client.setBirthdate(birthdate);
        client.setCpf(cpf);
        return clientRepository.save(client);
    }
}
