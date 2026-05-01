package br.com.ctkd.controller;

import br.com.ctkd.config.TestcontainersConfiguration;
import br.com.ctkd.domain.Address;
import br.com.ctkd.domain.Client;
import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.PhotoOccurrence;
import br.com.ctkd.repository.AddressRepository;
import br.com.ctkd.repository.ClientRepository;
import br.com.ctkd.repository.OccurrenceRepository;
import br.com.ctkd.repository.PhotoOccurrenceRepository;
import br.com.ctkd.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
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
class PhotoOccurrenceControllerIT {

    private static final String BASE_PATH = "/api/v1/photos";

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

    @Autowired
    private PhotoOccurrenceRepository photoOccurrenceRepository;

    private RestClient restClient;
    private RestClient authClient;

    @BeforeEach
    void setup() {
        when(s3Service.generatePresignedUrl(anyString())).thenReturn("https://s3.example.com/photo.jpg");

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
    void shouldGetPhotosByOccurrence() {
        var occurrence = savedOccurrenceWithDependencies();
        savedPhoto(occurrence, "photos/abc.jpg", "hash-abc", false);
        savedPhoto(occurrence, "photos/def.jpg", "hash-def", false);

        var response = authClient.get()
                .uri(BASE_PATH + "/occurrences/" + occurrence.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0))
                .containsKey("id")
                .containsKey("url")
                .containsEntry("deleted", false);
    }

    @Test
    void shouldReturnEmptyListWhenOccurrenceHasNoPhotos() {
        var occurrence = savedOccurrenceWithDependencies();

        var response = authClient.get()
                .uri(BASE_PATH + "/occurrences/" + occurrence.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldExcludeDeletedPhotosWhenListingByOccurrence() {
        var occurrence = savedOccurrenceWithDependencies();
        savedPhoto(occurrence, "photos/active.jpg", "hash-active", false);
        savedPhoto(occurrence, "photos/deleted.jpg", "hash-deleted", true);

        var response = authClient.get()
                .uri(BASE_PATH + "/occurrences/" + occurrence.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0)).containsEntry("pathBucket", "photos/active.jpg");
    }

    @Test
    void shouldGetPhotoById() {
        var occurrence = savedOccurrenceWithDependencies();
        var photo = savedPhoto(occurrence, "photos/img.jpg", "hash-img", false);

        var response = authClient.get()
                .uri(BASE_PATH + "/" + photo.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody())
                .containsEntry("pathBucket", "photos/img.jpg")
                .containsEntry("url", "https://s3.example.com/photo.jpg")
                .containsEntry("hash", "hash-img")
                .containsEntry("deleted", false);
    }

    @Test
    void shouldReturn404WhenPhotoNotFound() {
        var response = authClient.get()
                .uri(BASE_PATH + "/" + UUID.randomUUID())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldSoftDeletePhoto() {
        var occurrence = savedOccurrenceWithDependencies();
        var photo = savedPhoto(occurrence, "photos/remove.jpg", "hash-remove", false);

        var deleteResponse = authClient.delete()
                .uri(BASE_PATH + "/" + photo.getId())
                .retrieve()
                .toBodilessEntity();
        assertThat(deleteResponse.getStatusCode().value()).isEqualTo(204);

        var getResponse = authClient.get()
                .uri(BASE_PATH + "/" + photo.getId())
                .retrieve()
                .toBodilessEntity();
        assertThat(getResponse.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() {
        var response = restClient.get()
                .uri(BASE_PATH + "/" + UUID.randomUUID())
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    private Occurrence savedOccurrenceWithDependencies() {
        var client = new Client();
        client.setName("Test Client");
        client.setBirthdate(LocalDate.of(1990, 1, 1));
        client.setCpf("641.916.530-08");
        clientRepository.save(client);

        var address = new Address();
        address.setStreetName("Test Street");
        address.setNeighborhood("Test Hood");
        address.setZipCode("00000-000");
        address.setCity("Test City");
        address.setState("TS");
        addressRepository.save(address);

        var occurrence = new Occurrence();
        occurrence.setClient(client);
        occurrence.setAddress(address);
        occurrence.setOccurrenceDate(LocalDate.of(2024, 6, 1));
        return occurrenceRepository.save(occurrence);
    }

    private PhotoOccurrence savedPhoto(Occurrence occurrence, String pathBucket, String hash, boolean deleted) {
        var photo = new PhotoOccurrence();
        photo.setOccurrence(occurrence);
        photo.setPathBucket(pathBucket);
        photo.setHash(hash);
        photo.setDeleted(deleted);
        return photoOccurrenceRepository.save(photo);
    }
}
