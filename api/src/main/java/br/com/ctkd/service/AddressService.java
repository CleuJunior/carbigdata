package br.com.ctkd.service;

import br.com.ctkd.domain.Address;
import br.com.ctkd.domain.Client;
import br.com.ctkd.exceptions.NotFoundException;
import br.com.ctkd.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository repository;

    public Address getAddressById(String id) {
        var uuid = UUID.fromString(id);

        return repository.findById(uuid)
                .orElseThrow(() -> new NotFoundException("address.not.found", id));
    }

    public List<Address> getListAddresses() {
        return repository.findAll();
    }

    public Page<Address> pageAddresses(int page, int size) {
        var pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    public Address insertAddress(Address address) {
        return repository.save(address);
    }

    public Address updateAddress(Address request, String id) {
        var currentAddress = getAddressById(id);

        Optional.ofNullable(request.getStreetName()).ifPresent(currentAddress::setStreetName);
        Optional.ofNullable(request.getNeighborhood()).ifPresent(currentAddress::setNeighborhood);
        Optional.ofNullable(request.getZipCode()).ifPresent(currentAddress::setZipCode);
        Optional.ofNullable(request.getCity()).ifPresent(currentAddress::setCity);
        Optional.ofNullable(request.getState()).ifPresent(currentAddress::setState);

        return repository.save(currentAddress);
    }

    public void softDeleteAddress(String id) {
        var address = getAddressById(id);

        address.setDeleted(true);

        repository.save(address);
    }
}
