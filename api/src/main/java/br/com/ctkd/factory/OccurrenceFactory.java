package br.com.ctkd.factory;

import br.com.ctkd.domain.Address;
import br.com.ctkd.domain.Client;
import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.dto.request.OccurrenceRequest;
import br.com.ctkd.dto.response.OccurrenceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OccurrenceFactory {

    private final ClientFactory clientFactory;
    private final AddressFactory addressFactory;
    private final PhotoOccurrenceFactory photoOccurrenceFactory;

    public OccurrenceResponse toOccurrenceResponse(Occurrence occurrence) {
        return OccurrenceResponse.builder()
                .id(occurrence.getId())
                .occurrenceDate(occurrence.getOccurrenceDate())
                .status(occurrence.getStatus())
                .client(clientFactory.toClientResponse(occurrence.getClient()))
                .address(addressFactory.toAddressResponse(occurrence.getAddress()))
                .photos(photoOccurrenceFactory.toPhotoOccurrenceResponse(occurrence.getPhotos()))
                .creationDate(occurrence.getCreationDate())
                .updateDate(occurrence.getUpdateDate())
                .deleted(occurrence.isDeleted())
                .build();
    }

    public List<OccurrenceResponse> toOccurrenceResponse(List<Occurrence> occurrences) {
        return occurrences.stream()
                .map(this::toOccurrenceResponse)
                .toList();
    }

    public Occurrence toOccurrence(OccurrenceRequest request, Client client, Address address) {
        var occurrence = new Occurrence();
        occurrence.setClient(client);
        occurrence.setAddress(address);
        occurrence.setOccurrenceDate(request.occurrenceDate());
        return occurrence;
    }
}
