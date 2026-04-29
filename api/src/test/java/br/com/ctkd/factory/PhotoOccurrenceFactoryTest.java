package br.com.ctkd.factory;

import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.PhotoOccurrence;
import br.com.ctkd.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PhotoOccurrenceFactoryTest {

    @Mock
    private S3Service s3Service;
    @InjectMocks
    private PhotoOccurrenceFactory underTest;

    private PhotoOccurrence photo;
    private Occurrence occurrence;

    @BeforeEach
    void setup() {
        occurrence = mock(Occurrence.class);
        given(occurrence.getId()).willReturn("occ-id-123");

        photo = mock(PhotoOccurrence.class);
        given(photo.getId()).willReturn("photo-id-456");
        given(photo.getOccurrence()).willReturn(occurrence);
        given(photo.getPathBucket()).willReturn("photos/uuid.jpg");
        given(photo.getHash()).willReturn("sha256hash");
        given(photo.getCreationDate()).willReturn(OffsetDateTime.now());
        given(photo.getUpdateDate()).willReturn(OffsetDateTime.now());
        given(photo.isDeleted()).willReturn(false);

        given(s3Service.generatePresignedUrl("photos/uuid.jpg"))
                .willReturn("http://minio:9000/occurrences/photos/uuid.jpg?X-Amz-Signature=abc");
    }

    @Test
    void shouldMapPhotoToResponse() {

        var result = underTest.toPhotoOccurrenceResponse(photo);

        then(result.id()).isEqualTo("photo-id-456");
        then(result.occurrenceId()).isEqualTo("occ-id-123");
        then(result.pathBucket()).isEqualTo("photos/uuid.jpg");
        then(result.url()).isEqualTo("http://minio:9000/occurrences/photos/uuid.jpg?X-Amz-Signature=abc");
        then(result.hash()).isEqualTo("sha256hash");
        then(result.deleted()).isFalse();

        verify(s3Service).generatePresignedUrl("photos/uuid.jpg");
    }

    @Test
    void shouldMapPhotoListToResponseList() {
        var result = underTest.toPhotoOccurrenceResponse(List.of(photo));

        then(result).hasSize(1);
        then(result.getFirst().pathBucket()).isEqualTo("photos/uuid.jpg");
        then(result.getFirst().url()).isNotBlank();

        verify(s3Service).generatePresignedUrl("photos/uuid.jpg");
    }

    @Test
    void shouldMapToPhotoOccurrenceEntity() {
        var result = underTest.toPhotoOccurrence(occurrence, "photos/new.jpg", "newhash");

        then(result.getOccurrence()).isEqualTo(occurrence);
        then(result.getPathBucket()).isEqualTo("photos/new.jpg");
        then(result.getHash()).isEqualTo("newhash");
    }
}
