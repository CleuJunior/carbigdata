package br.com.ctkd.controller;

import br.com.ctkd.dto.request.AddressRequest;
import br.com.ctkd.dto.response.AddressResponse;
import br.com.ctkd.factory.AddressFactory;
import br.com.ctkd.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/clients")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService service;
    private final AddressFactory factory;

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable String id) {
        var address = service.getAddressById(id);
        var response = factory.toAddressResponse(address);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<AddressResponse>> listAddress() {
        var addresses = service.getListAddresses();
        var response = factory.toAddressResponse(addresses);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/pageable")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<AddressResponse>> pageAddress(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "15") int size) {

        var response = service.pageAddresses(page, size)
                .map(factory::toAddressResponse);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> saveAddress(@RequestBody @Valid AddressRequest request) {
        var addressToSave = factory.toAddress(request);
        var addressSaved = service.insertAddress(addressToSave);
        var response = factory.toAddressResponse(addressSaved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable String id,
                                                         @RequestBody @Valid AddressRequest request) {

        var addressToUpdate = factory.toAddress(request);
        var addressUpdated = service.updateAddress(addressToUpdate, id);
        var response = factory.toAddressResponse(addressUpdated);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAddress(@PathVariable String id) {
        service.softDeleteAddress(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}