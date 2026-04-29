package br.com.ctkd.factory;

import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.PhotoOccurrence;
import br.com.ctkd.dto.response.PhotoOccurrenceResponse;
import br.com.ctkd.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PhotoOccurrenceFactory {

    private final S3Service s3Service;

    public PhotoOccurrenceResponse toPhotoOccurrenceResponse(PhotoOccurrence photo) {
        return PhotoOccurrenceResponse.builder()
                .id(photo.getId())
                .occurrenceId(photo.getOccurrence().getId())
                .pathBucket(photo.getPathBucket())
                .url(s3Service.generatePresignedUrl(photo.getPathBucket()))
                .hash(photo.getHash())
                .creationDate(photo.getCreationDate())
                .updateDate(photo.getUpdateDate())
                .deleted(photo.isDeleted())
                .build();
    }

    public List<PhotoOccurrenceResponse> toPhotoOccurrenceResponse(List<PhotoOccurrence> photos) {
        return photos.stream()
                .map(this::toPhotoOccurrenceResponse)
                .toList();
    }

    public PhotoOccurrence toPhotoOccurrence(Occurrence occurrence, String pathBucket, String hash) {
        var photo = new PhotoOccurrence();
        photo.setOccurrence(occurrence);
        photo.setPathBucket(pathBucket);
        photo.setHash(hash);
        return photo;
    }
}
