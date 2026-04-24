package br.com.ctkd.factory;

import br.com.ctkd.domain.Address;
import br.com.ctkd.dto.request.AddressRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AddressFactoryTest {

    @Mock
    private Address address;
    @InjectMocks
    private AddressFactory underTest;

    @Test
    void shouldMapAddressToAddressResponse() {
        var id = "address-id";
        var streetName = "Rua das Flores";
        var neighborhood = "Centro";
        var zipCode = "01001-000";
        var city = "São Paulo";
        var state = "SP";
        var creationDate = OffsetDateTime.now();
        var updateDate = OffsetDateTime.now();

        given(address.getId()).willReturn(id);
        given(address.getStreetName()).willReturn(streetName);
        given(address.getNeighborhood()).willReturn(neighborhood);
        given(address.getZipCode()).willReturn(zipCode);
        given(address.getCity()).willReturn(city);
        given(address.getState()).willReturn(state);
        given(address.getCreationDate()).willReturn(creationDate);
        given(address.getUpdateDate()).willReturn(updateDate);
        given(address.isDeleted()).willReturn(false);

        var result = underTest.toAddressResponse(address);

        then(result.id()).isEqualTo(id);
        then(result.streetName()).isEqualTo(streetName);
        then(result.neighborhood()).isEqualTo(neighborhood);
        then(result.zipCode()).isEqualTo(zipCode);
        then(result.city()).isEqualTo(city);
        then(result.state()).isEqualTo(state);
        then(result.creationDate()).isEqualTo(creationDate);
        then(result.updateDate()).isEqualTo(updateDate);
        then(result.deleted()).isEqualTo(false);
    }

    @Test
    void shouldMapAddressListToAddressResponseList() {
        var id = "address-id";
        var streetName = "Rua das Flores";
        var neighborhood = "Centro";
        var zipCode = "01001-000";
        var city = "São Paulo";
        var state = "SP";
        var creationDate = OffsetDateTime.now();
        var updateDate = OffsetDateTime.now();

        given(address.getId()).willReturn(id);
        given(address.getStreetName()).willReturn(streetName);
        given(address.getNeighborhood()).willReturn(neighborhood);
        given(address.getZipCode()).willReturn(zipCode);
        given(address.getCity()).willReturn(city);
        given(address.getState()).willReturn(state);
        given(address.getCreationDate()).willReturn(creationDate);
        given(address.getUpdateDate()).willReturn(updateDate);
        given(address.isDeleted()).willReturn(false);

        var result = underTest.toAddressResponse(Collections.singletonList(address));

        then(result.size()).isEqualTo(1);
        then(result.getFirst().id()).isEqualTo(id);
        then(result.getFirst().streetName()).isEqualTo(streetName);
        then(result.getFirst().neighborhood()).isEqualTo(neighborhood);
        then(result.getFirst().zipCode()).isEqualTo(zipCode);
        then(result.getFirst().city()).isEqualTo(city);
        then(result.getFirst().state()).isEqualTo(state);
        then(result.getFirst().creationDate()).isEqualTo(creationDate);
        then(result.getFirst().updateDate()).isEqualTo(updateDate);
        then(result.getFirst().deleted()).isEqualTo(false);
    }

    @Test
    void shouldMapAddressRequestToAddress() {
        var request = mock(AddressRequest.class);

        given(request.streetName()).willReturn("Rua das Flores");
        given(request.neighborhood()).willReturn("Centro");
        given(request.zipCode()).willReturn("01001-000");
        given(request.state()).willReturn("SP");

        var result = underTest.toAddress(request);

        then(result.getStreetName()).isEqualTo("Rua das Flores");
        then(result.getNeighborhood()).isEqualTo("Centro");
        then(result.getZipCode()).isEqualTo("01001-000");
        then(result.getState()).isEqualTo("SP");
    }
}
