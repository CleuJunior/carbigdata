package br.com.ctkd.controller;

import br.com.ctkd.dto.request.OccurrenceQueryRequest;
import br.com.ctkd.dto.request.OccurrenceRequest;
import br.com.ctkd.dto.response.OccurrenceResponse;
import br.com.ctkd.factory.OccurrenceFactory;
import br.com.ctkd.service.AddressService;
import br.com.ctkd.service.ClientService;
import br.com.ctkd.service.OccurrenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/occurrences")
@RequiredArgsConstructor
public class OccurrenceController {

    private final OccurrenceService service;
    private final ClientService clientService;
    private final AddressService addressService;
    private final OccurrenceFactory factory;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<OccurrenceResponse>> getAllOccurrences() {
        var occurrences = service.getAllOccurrence();
        var response = factory.toOccurrenceResponse(occurrences);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<OccurrenceResponse>> listOccurrences(@RequestBody OccurrenceQueryRequest request) {
        var occurrences = service.search(request);
        var response = factory.toOccurrenceResponse(occurrences);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<OccurrenceResponse> saveOccurrence(@RequestBody @Valid OccurrenceRequest request) {
        var client = clientService.getById(request.clientId());
        var address = addressService.getAddressById(request.addressId());
        var occurrence = factory.toOccurrence(request, client, address);

        service.insertOccurrence(occurrence);

        var response = factory.toOccurrenceResponse(occurrence);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<OccurrenceResponse> closeOccurrence(@PathVariable String id) {
        var occurrence = service.closeOccurrence(id);

        var response = factory.toOccurrenceResponse(occurrence);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
