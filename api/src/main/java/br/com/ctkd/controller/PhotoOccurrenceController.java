package br.com.ctkd.controller;

import br.com.ctkd.dto.response.PhotoOccurrenceResponse;
import br.com.ctkd.factory.PhotoOccurrenceFactory;
import br.com.ctkd.service.PhotoOccurrenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
@Tag(name = "Photos", description = "Photo attachments for occurrences")
@SecurityRequirement(name = "bearerAuth")
public class PhotoOccurrenceController {

    private final PhotoOccurrenceService service;
    private final PhotoOccurrenceFactory factory;

    @GetMapping("/occurrences/{occurrenceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List photos for an occurrence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Occurrence not found")
    })
    public ResponseEntity<List<PhotoOccurrenceResponse>> getPhotosByOccurrence(
            @Parameter(description = "Occurrence UUID") @PathVariable String occurrenceId) {
        var photos = service.getPhotosByOccurrence(occurrenceId);
        var response = factory.toPhotoOccurrenceResponse(photos);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{photoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get a single photo by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Photo not found")
    })
    public ResponseEntity<PhotoOccurrenceResponse> getPhoto(
            @Parameter(description = "Photo UUID") @PathVariable String photoId) {
        var photo = service.getPhotoById(photoId);
        var response = factory.toPhotoOccurrenceResponse(photo);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a photo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Photo deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Photo not found")
    })
    public ResponseEntity<Void> deletePhoto(
            @Parameter(description = "Photo UUID") @PathVariable String photoId) {
        service.deletePhoto(photoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
