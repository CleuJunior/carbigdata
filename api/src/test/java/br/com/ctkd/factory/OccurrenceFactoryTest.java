package br.com.ctkd.factory;

import br.com.ctkd.domain.Address;
import br.com.ctkd.domain.Client;
import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.PhotoOccurrence;
import br.com.ctkd.domain.StatusOccurrence;
import br.com.ctkd.dto.request.OccurrenceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OccurrenceFactoryTest {

    @Spy
    private ClientFactory clientFactory;
    @Spy
    private AddressFactory addressFactory;
    @Spy
    private PhotoOccurrenceFactory photoOccurrenceFactory;
    @InjectMocks
    private OccurrenceFactory underTest;

    private Client client;
    private Address address;
    private PhotoOccurrence photoOccurrence;
    private Occurrence occurrence;
    private OccurrenceRequest occurrenceRequest;

    @BeforeEach
    void setup() {
        client = new Client();
        client.setName("Fernando Gaspar");
        client.setBirthdate(LocalDate.of(1998, 5, 28));
        client.setCpf("816.951.710-98");

        address = new Address();
        address.setStreetName("Vigario Norte");
        address.setNeighborhood("Porto Meio Alegre");
        address.setZipCode("64057-290");
        address.setCity("Rio Ouro Verde");
        address.setState("RJ");

        photoOccurrence = new PhotoOccurrence();
        photoOccurrence.setPathBucket("s3://bucket/photo.jpg");
        photoOccurrence.setHash("91823af61238490");

        occurrence = new Occurrence();
        occurrence.setClient(client);
        occurrence.setAddress(address);
        occurrence.setOccurrenceDate(LocalDate.of(2024, 2, 15));
        occurrence.setStatus(StatusOccurrence.ACTIVE);
        occurrence.addPhotos(List.of(photoOccurrence));

        photoOccurrence.setOccurrence(occurrence);

        occurrenceRequest = OccurrenceRequest.builder()
                .clientId("3f6b2c1d-4e5a-7890-abcd-ef1234567890")
                .addressId("1a2b3c4d-5e6f-7890-abcd-ef0987654321")
                .occurrenceDate(LocalDate.of(2024, 2, 15))
                .build();
    }

    @Test
    void shouldMapOccurrenceToOccurrenceResponse() {
        var result = underTest.toOccurrenceResponse(occurrence);

        then(result.occurrenceDate()).isEqualTo(occurrence.getOccurrenceDate());
        then(result.status()).isEqualTo(occurrence.getStatus());
        then(result.client().name()).isEqualTo(occurrence.getClient().getName());
        then(result.client().birthdate()).isEqualTo(occurrence.getClient().getBirthdate());
        then(result.client().cpf()).isEqualTo(occurrence.getClient().getCpf());
        then(result.address().streetName()).isEqualTo(occurrence.getAddress().getStreetName());
        then(result.address().neighborhood()).isEqualTo(occurrence.getAddress().getNeighborhood());
        then(result.address().zipCode()).isEqualTo(occurrence.getAddress().getZipCode());
        then(result.address().city()).isEqualTo(occurrence.getAddress().getCity());
        then(result.address().state()).isEqualTo(occurrence.getAddress().getState());
        then(result.photos().getFirst().pathBucket()).isEqualTo(occurrence.getPhotos().getFirst().getPathBucket());
        then(result.photos().getFirst().hash()).isEqualTo(occurrence.getPhotos().getFirst().getHash());

        verify(clientFactory).toClientResponse(occurrence.getClient());
        verify(addressFactory).toAddressResponse(occurrence.getAddress());
        verify(photoOccurrenceFactory).toPhotoOccurrenceResponse(occurrence.getPhotos());
    }

    @Test
    void shouldMapOccurrenceListToOccurrenceResponseList() {
        var result = underTest.toOccurrenceResponse(Collections.singletonList(occurrence));

        then(result.getFirst().occurrenceDate()).isEqualTo(occurrence.getOccurrenceDate());
        then(result.getFirst().status()).isEqualTo(occurrence.getStatus());
        then(result.getFirst().client().name()).isEqualTo(occurrence.getClient().getName());
        then(result.getFirst().client().birthdate()).isEqualTo(occurrence.getClient().getBirthdate());
        then(result.getFirst().client().cpf()).isEqualTo(occurrence.getClient().getCpf());
        then(result.getFirst().address().streetName()).isEqualTo(occurrence.getAddress().getStreetName());
        then(result.getFirst().address().neighborhood()).isEqualTo(occurrence.getAddress().getNeighborhood());
        then(result.getFirst().address().zipCode()).isEqualTo(occurrence.getAddress().getZipCode());
        then(result.getFirst().address().city()).isEqualTo(occurrence.getAddress().getCity());
        then(result.getFirst().address().state()).isEqualTo(occurrence.getAddress().getState());
        then(result.getFirst().photos().getFirst().pathBucket()).isEqualTo(occurrence.getPhotos().getFirst().getPathBucket());
        then(result.getFirst().photos().getFirst().hash()).isEqualTo(occurrence.getPhotos().getFirst().getHash());

        verify(clientFactory).toClientResponse(occurrence.getClient());
        verify(addressFactory).toAddressResponse(occurrence.getAddress());
        verify(photoOccurrenceFactory).toPhotoOccurrenceResponse(occurrence.getPhotos());
    }

    @Test
    void shouldMapOccurrenceRequestToOccurrence() {
        var result = underTest.toOccurrence(occurrenceRequest, client, address);

        then(result.getOccurrenceDate()).isEqualTo(LocalDate.of(2024, 2, 15));
        then(result.getStatus()).isEqualTo(StatusOccurrence.ACTIVE);
        then(result.getClient()).isEqualTo(client);
        then(result.getAddress()).isEqualTo(address);
    }
}
