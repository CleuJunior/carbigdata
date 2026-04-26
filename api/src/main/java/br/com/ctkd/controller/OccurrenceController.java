package br.com.ctkd.controller;

import br.com.ctkd.dto.request.OccurrenceQueryRequest;
import br.com.ctkd.dto.request.OccurrenceRequest;
import br.com.ctkd.dto.response.OccurrenceResponse;
import br.com.ctkd.factory.OccurrenceFactory;
import br.com.ctkd.service.AddressService;
import br.com.ctkd.service.ClientService;
import br.com.ctkd.service.OccurrenceService;
import br.com.ctkd.service.PhotoOccurrenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/occurrences")
@RequiredArgsConstructor
@Tag(name = "Occurrences", description = "Occurrence (incident/complaint) management")
@SecurityRequirement(name = "bearerAuth")
public class OccurrenceController {

    private final OccurrenceService service;
    private final ClientService clientService;
    private final AddressService addressService;
    private final OccurrenceFactory factory;
    private final PhotoOccurrenceService photoOccurrenceService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get occurrence by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Occurrence found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Occurrence not found")
    })
    public ResponseEntity<OccurrenceResponse> getOccurrenceById(
            @Parameter(description = "Occurrence UUID") @PathVariable String id) {
        var occurrence = service.getOccurrenceById(id);
        var response = factory.toOccurrenceResponse(occurrence);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List all occurrences")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Occurrence list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<OccurrenceResponse>> getAllOccurrences() {
        var occurrences = service.getAllOccurrence();
        var response = factory.toOccurrenceResponse(occurrences);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Search occurrences",
            description = "Filter occurrences by client name, CPF, occurrence date, city, with optional sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered occurrence list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<OccurrenceResponse>> listOccurrences(@RequestBody OccurrenceQueryRequest request) {
        var occurrences = service.search(request);
        var response = factory.toOccurrenceResponse(occurrences);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Create an occurrence",
            description = "Creates an occurrence with optional photo attachments. " +
                    "Send as multipart/form-data: 'data' part (JSON) + optional 'photos' part (files).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Occurrence created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Client or address not found")
    })
    public ResponseEntity<OccurrenceResponse> saveOccurrence(
            @Parameter(description = "Occurrence data (JSON part)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("data") @Valid OccurrenceRequest request,
            @Parameter(description = "Photo attachments (optional)")
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {

        var client = clientService.getById(request.clientId());
        var address = addressService.getAddressById(request.addressId());
        var occurrence = factory.toOccurrence(request, client, address);

        if (photos != null && !photos.isEmpty()) {
            var photoEntities = photoOccurrenceService.createPhotoEntities(occurrence, photos);
            occurrence.addPhotos(photoEntities);
        }

        var saved = service.insertOccurrence(occurrence);
        return ResponseEntity.status(HttpStatus.CREATED).body(factory.toOccurrenceResponse(saved));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Close an occurrence", description = "Transitions the occurrence status to FINISHED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Occurrence closed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Occurrence not found"),
            @ApiResponse(responseCode = "409", description = "Occurrence is already FINISHED")
    })
    public ResponseEntity<OccurrenceResponse> closeOccurrence(
            @Parameter(description = "Occurrence UUID") @PathVariable String id) {
        var occurrence = service.closeOccurrence(id);
        var response = factory.toOccurrenceResponse(occurrence);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
