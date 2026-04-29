package br.com.ctkd.service;

import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.PhotoOccurrence;
import br.com.ctkd.exceptions.NotFoundException;
import br.com.ctkd.repository.PhotoOccurrenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PhotoOccurrenceServiceTest {

    @Mock
    private PhotoOccurrenceRepository repository;
    @Mock
    private OccurrenceService occurrenceService;
    @Mock
    private S3Service s3Service;
    @InjectMocks
    private PhotoOccurrenceService underTest;

    private Occurrence occurrence;
    private PhotoOccurrence photo;

    @BeforeEach
    void setup() {
        occurrence = new Occurrence();

        photo = new PhotoOccurrence();
        photo.setPathBucket("photos/abc.jpg");
        photo.setHash("deadbeef");
        photo.setOccurrence(occurrence);
    }

    @Test
    void shouldCreatePhotoEntities() throws IOException {
        var file = mock(MultipartFile.class);
        given(file.getBytes()).willReturn("image-content".getBytes());
        given(file.getOriginalFilename()).willReturn("photo.jpg");
        given(file.getContentType()).willReturn("image/jpeg");

        var result = underTest.createPhotoEntities(occurrence, List.of(file));

        then(result).hasSize(1);
        then(result.getFirst().getOccurrence()).isEqualTo(occurrence);
        then(result.getFirst().getPathBucket()).startsWith("photos/");
        then(result.getFirst().getHash()).isNotBlank();

        verify(s3Service).uploadFile(anyString(), any(byte[].class), anyString());
    }

    @Test
    void shouldUploadPhoto() throws IOException {
        var occurrenceId = UUID.randomUUID().toString();
        var file = mock(MultipartFile.class);
        given(file.getBytes()).willReturn("image-content".getBytes());
        given(file.getOriginalFilename()).willReturn("evidence.png");
        given(file.getContentType()).willReturn("image/png");
        given(occurrenceService.getOccurrenceById(occurrenceId)).willReturn(occurrence);
        given(repository.save(any(PhotoOccurrence.class))).willReturn(photo);

        var result = underTest.uploadPhoto(occurrenceId, file);

        then(result.getPathBucket()).isEqualTo("photos/abc.jpg");

        verify(occurrenceService).getOccurrenceById(occurrenceId);
        verify(s3Service).uploadFile(anyString(), any(byte[].class), anyString());
        verify(repository).save(any(PhotoOccurrence.class));
    }

    @Test
    void shouldGetPhotosByOccurrence() {
        var occurrenceId = UUID.randomUUID();

        given(repository.findByOccurrenceId(occurrenceId)).willReturn(List.of(photo));

        var result = underTest.getPhotosByOccurrence(occurrenceId.toString());

        then(result).hasSize(1);
        then(result.getFirst().getPathBucket()).isEqualTo("photos/abc.jpg");

        verify(repository).findByOccurrenceId(occurrenceId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldGetPhotoById() {
        var photoId = UUID.randomUUID();

        given(repository.findById(photoId)).willReturn(Optional.of(photo));

        var result = underTest.getPhotoById(photoId.toString());

        then(result.getPathBucket()).isEqualTo("photos/abc.jpg");

        verify(repository).findById(photoId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowWhenPhotoNotFound() {
        var photoId = UUID.randomUUID();

        given(repository.findById(photoId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getPhotoById(photoId.toString()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldSoftDeletePhoto() {
        var photoId = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(PhotoOccurrence.class);

        given(repository.findById(photoId)).willReturn(Optional.of(photo));
        given(repository.save(any(PhotoOccurrence.class))).willReturn(photo);

        underTest.deletePhoto(photoId.toString());

        verify(repository).save(captor.capture());

        then(captor.getValue().isDeleted()).isTrue();
    }
}
