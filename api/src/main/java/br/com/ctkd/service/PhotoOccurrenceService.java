package br.com.ctkd.service;

import br.com.ctkd.domain.Occurrence;
import br.com.ctkd.domain.PhotoOccurrence;
import br.com.ctkd.exceptions.NotFoundException;
import br.com.ctkd.repository.PhotoOccurrenceRepository;
import br.com.ctkd.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoOccurrenceService {

    private final PhotoOccurrenceRepository repository;
    private final OccurrenceService occurrenceService;
    private final S3Service s3Service;

    public List<PhotoOccurrence> createPhotoEntities(Occurrence occurrence, List<MultipartFile> files) {
        return files.stream()
                .map(file -> buildPhotoEntity(occurrence, file))
                .toList();
    }

    public PhotoOccurrence uploadPhoto(String occurrenceId, MultipartFile file) {
        var occurrence = occurrenceService.getOccurrenceById(occurrenceId);
        var photo = buildPhotoEntity(occurrence, file);
        return repository.save(photo);
    }

    public List<PhotoOccurrence> getPhotosByOccurrence(String occurrenceId) {
        return repository.findByOccurrenceId(UUID.fromString(occurrenceId));
    }

    public PhotoOccurrence getPhotoById(String id) {
        return repository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("photo.not.found", id));
    }

    public void deletePhoto(String id) {
        var photo = getPhotoById(id);
        photo.setDeleted(true);
        repository.save(photo);
    }

    private PhotoOccurrence buildPhotoEntity(Occurrence occurrence, MultipartFile file) {
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        var extension = FileUtils.extractExtension(file.getOriginalFilename());
        var key = "photos/" + UUID.randomUUID() + extension;

        s3Service.uploadFile(key, content, file.getContentType());

        var photo = new PhotoOccurrence();
        photo.setOccurrence(occurrence);
        photo.setPathBucket(key);
        photo.setHash(FileUtils.computeSha256(content));
        return photo;
    }
}
