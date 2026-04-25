package br.com.ctkd.controller;

import br.com.ctkd.dto.response.PhotoOccurrenceResponse;
import br.com.ctkd.factory.PhotoOccurrenceFactory;
import br.com.ctkd.service.PhotoOccurrenceService;
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
public class PhotoOccurrenceController {

    private final PhotoOccurrenceService service;
    private final PhotoOccurrenceFactory factory;

    @GetMapping("/occurrences/{occurrenceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<PhotoOccurrenceResponse>> getPhotosByOccurrence(@PathVariable String occurrenceId) {
        var photos = service.getPhotosByOccurrence(occurrenceId);
        var response = factory.toPhotoOccurrenceResponse(photos);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{photoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PhotoOccurrenceResponse> getPhoto(@PathVariable String photoId) {
        var photo = service.getPhotoById(photoId);
        var response = factory.toPhotoOccurrenceResponse(photo);

        return ResponseEntity.status(HttpStatus.OK).body(response);    }

    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePhoto(@PathVariable String photoId) {
        service.deletePhoto(photoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
