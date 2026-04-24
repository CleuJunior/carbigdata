package br.com.ctkd.factory;

import br.com.ctkd.domain.Address;
import br.com.ctkd.dto.request.AddressRequest;
import br.com.ctkd.dto.response.AddressResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddressFactory {

    public AddressResponse toAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .streetName(address.getStreetName())
                .neighborhood(address.getNeighborhood())
                .zipCode(address.getZipCode())
                .city(address.getCity())
                .state(address.getState())
                .creationDate(address.getCreationDate())
                .updateDate(address.getUpdateDate())
                .deleted(address.isDeleted())
                .build();
    }

    public List<AddressResponse> toAddressResponse(List<Address> addresses) {
        return addresses.stream()
                .map(this::toAddressResponse)
                .toList();
    }

    public Address toAddress(AddressRequest request) {
        var address = new Address();

        address.setStreetName(request.streetName());
        address.setNeighborhood(request.neighborhood());
        address.setZipCode(request.zipCode());
        address.setCity(request.city());
        address.setState(request.state());

        return address;
    }
}
