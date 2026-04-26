package br.com.ctkd.controller;

import br.com.ctkd.dto.request.AddressRequest;
import br.com.ctkd.dto.response.AddressResponse;
import br.com.ctkd.factory.AddressFactory;
import br.com.ctkd.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping(value = "/api/v1/address")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Address management")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService service;
    private final AddressFactory factory;

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get address by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<AddressResponse> getAddressById(
            @Parameter(description = "Address UUID") @PathVariable String id) {
        var address = service.getAddressById(id);
        var response = factory.toAddressResponse(address);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List all addresses")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<AddressResponse>> listAddress() {
        var addresses = service.getListAddresses();
        var response = factory.toAddressResponse(addresses);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/pageable")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List addresses with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated address list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<AddressResponse>> pageAddress(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "15") @RequestParam(defaultValue = "15") int size) {

        var response = service.pageAddresses(page, size)
                .map(factory::toAddressResponse);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create an address")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required")
    })
    public ResponseEntity<AddressResponse> saveAddress(@RequestBody @Valid AddressRequest request) {
        var addressToSave = factory.toAddress(request);
        var addressSaved = service.insertAddress(addressToSave);
        var response = factory.toAddressResponse(addressSaved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an address")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Address updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<AddressResponse> updateAddress(
            @Parameter(description = "Address UUID") @PathVariable String id,
            @RequestBody @Valid AddressRequest request) {

        var addressToUpdate = factory.toAddress(request);
        var addressUpdated = service.updateAddress(addressToUpdate, id);
        var response = factory.toAddressResponse(addressUpdated);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete an address")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "Address UUID") @PathVariable String id) {
        service.softDelete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
