package br.com.ctkd.repository;

import br.com.ctkd.config.JpaAuditingLogConfig;
import br.com.ctkd.config.TestcontainersConfiguration;
import br.com.ctkd.domain.Address;
import br.com.ctkd.domain.Client;
import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.PhotoOccurrence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
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
class PhotoOccurrenceRepositoryIT {

    @Autowired
    private PhotoOccurrenceRepository photoOccurrenceRepository;

    @Autowired
    private OccurrenceRepository occurrenceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldFindPhotosByOccurrenceId() {
        var occurrence = savedOccurrenceWithDependencies(false);
        savedPhoto(occurrence, "photos/img1.jpg", "hash-1", false);
        savedPhoto(occurrence, "photos/img2.jpg", "hash-2", false);

        var result = photoOccurrenceRepository.findByOccurrenceId(UUID.fromString(occurrence.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(p -> {
            assertThat(p.isDeleted()).isFalse();
            assertThat(p.getOccurrence().getId()).isEqualTo(occurrence.getId());
        });
    }

    @Test
    void shouldNotFindPhotosByOccurrenceIdWhenPhotoIsDeleted() {
        var occurrence = savedOccurrenceWithDependencies(false);
        savedPhoto(occurrence, "photos/active.jpg", "hash-active", false);
        savedPhoto(occurrence, "photos/deleted.jpg", "hash-deleted", true);

        var result = photoOccurrenceRepository.findByOccurrenceId(UUID.fromString(occurrence.getId()));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPathBucket()).isEqualTo("photos/active.jpg");
    }

    @Test
    void shouldNotFindPhotosByOccurrenceIdWhenOccurrenceIsDeleted() {
        var deletedOccurrence = savedOccurrenceWithDependencies(true);
        savedPhoto(deletedOccurrence, "photos/orphan.jpg", "hash-orphan", false);

        var result = photoOccurrenceRepository.findByOccurrenceId(UUID.fromString(deletedOccurrence.getId()));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindPhotoByIdAndNotDeleted() {
        var occurrence = savedOccurrenceWithDependencies(false);
        var photo = savedPhoto(occurrence, "photos/find.jpg", "hash-find", false);

        var result = photoOccurrenceRepository.findById(UUID.fromString(photo.getId()));

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(p -> {
                    assertThat(p.getPathBucket()).isEqualTo("photos/find.jpg");
                    assertThat(p.getHash()).isEqualTo("hash-find");
                    assertThat(p.isDeleted()).isFalse();
                });
    }

    @Test
    void shouldNotFindPhotoByIdWhenDeleted() {
        var occurrence = savedOccurrenceWithDependencies(false);
        var photo = savedPhoto(occurrence, "photos/gone.jpg", "hash-gone", true);

        var result = photoOccurrenceRepository.findById(UUID.fromString(photo.getId()));

        assertThat(result).isNotPresent();
    }

    private Occurrence savedOccurrenceWithDependencies(boolean occurrenceDeleted) {
        var client = new Client();
        client.setName("Test Client");
        client.setBirthdate(LocalDate.of(1990, 1, 15));
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
        occurrence.setDeleted(occurrenceDeleted);
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
