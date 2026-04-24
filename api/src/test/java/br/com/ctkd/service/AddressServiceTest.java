package br.com.ctkd.service;

import br.com.ctkd.domain.Address;
import br.com.ctkd.exceptions.NotFoundException;
import br.com.ctkd.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    private Address address;
    @Mock
    private AddressRepository repository;
    @InjectMocks
    private AddressService underTest;

    @BeforeEach
    void setup() {
        address = new Address();
        address.setStreetName("Rua das Flores");
        address.setNeighborhood("Centro");
        address.setZipCode("01001-000");
        address.setCity("São Paulo");
        address.setState("SP");
    }

    @Test
    void shouldGetAddressById() {
        var uuid = UUID.randomUUID();

        given(repository.findById(uuid)).willReturn(Optional.of(address));

        var result = underTest.getAddressById(uuid.toString());

        then(result.getStreetName()).isEqualTo("Rua das Flores");
        then(result.getNeighborhood()).isEqualTo("Centro");
        then(result.getZipCode()).isEqualTo("01001-000");
        then(result.getCity()).isEqualTo("São Paulo");
        then(result.getState()).isEqualTo("SP");

        verify(repository).findById(uuid);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowWhenAddressNotFound() {
        var uuid = UUID.randomUUID();

        given(repository.findById(uuid)).willReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getAddressById(uuid.toString()))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findById(uuid);
    }

    @Test
    void getListAddresses() {
        given(repository.findAll()).willReturn(Collections.singletonList(address));

        var result = underTest.getListAddresses();

        then(result.size()).isEqualTo(1);
        then(result.getFirst().getStreetName()).isEqualTo("Rua das Flores");
        then(result.getFirst().getNeighborhood()).isEqualTo("Centro");
        then(result.getFirst().getZipCode()).isEqualTo("01001-000");
        then(result.getFirst().getCity()).isEqualTo("São Paulo");
        then(result.getFirst().getState()).isEqualTo("SP");

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldReturnPagedAddresses() {
        var pageable = PageRequest.of(0, 10);
        var addressesPage = new PageImpl<>(List.of(address), pageable, 1);

        given(repository.findAll(pageable)).willReturn(addressesPage);

        var result = underTest.pageAddresses(0, 10);

        then(result).isNotNull();
        then(result.getContent()).hasSize(1);
        then(result.getTotalElements()).isEqualTo(1);
        then(result.getTotalPages()).isEqualTo(1);
        then(result.getNumber()).isEqualTo(0);
        then(result.getSize()).isEqualTo(10);
        then(result.getContent()).containsExactlyInAnyOrder(address);

        verify(repository).findAll(pageable);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldInsertAddress() {
        var addressToInsert = mock(Address.class);

        given(repository.save(addressToInsert)).willReturn(address);

        var result = underTest.insertAddress(addressToInsert);

        then(result.getStreetName()).isEqualTo("Rua das Flores");
        then(result.getNeighborhood()).isEqualTo("Centro");
        then(result.getZipCode()).isEqualTo("01001-000");
        then(result.getCity()).isEqualTo("São Paulo");
        then(result.getState()).isEqualTo("SP");

        verify(repository).save(addressToInsert);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldUpdateAddress() {
        var uuid = UUID.randomUUID();
        var addressToUpdate = mock(Address.class);

        given(repository.findById(uuid)).willReturn(Optional.of(address));
        given(repository.save(address)).willReturn(address);

        var result = underTest.updateAddress(addressToUpdate, uuid.toString());

        then(result.getStreetName()).isEqualTo("Rua das Flores");
        then(result.getNeighborhood()).isEqualTo("Centro");
        then(result.getZipCode()).isEqualTo("01001-000");
        then(result.getCity()).isEqualTo("São Paulo");
        then(result.getState()).isEqualTo("SP");

        verify(repository).findById(uuid);
        verify(repository).save(address);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldSoftDeleteAddress() {
        var uuid = UUID.randomUUID();

        var captor = ArgumentCaptor.forClass(Address.class);

        given(repository.findById(uuid)).willReturn(Optional.of(address));
        given(repository.save(any(Address.class))).willReturn(address);

        underTest.softDelete(uuid.toString());

        verify(repository).save(captor.capture());

        var capturedAddress = captor.getValue();

        then(capturedAddress.isDeleted()).isTrue();

        verify(repository).findById(uuid);
        verifyNoMoreInteractions(repository);
    }
}
