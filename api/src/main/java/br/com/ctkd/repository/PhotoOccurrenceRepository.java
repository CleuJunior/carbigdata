package br.com.ctkd.repository;

import br.com.ctkd.domain.PhotoOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhotoOccurrenceRepository extends JpaRepository<PhotoOccurrence, UUID> {

    @Query("SELECT p FROM photos_occurrence p WHERE p.occurrence.id = :occurrenceId AND p.deleted IS FALSE AND p.occurrence.deleted IS FALSE")
    List<PhotoOccurrence> findByOccurrenceId(@Param("occurrenceId") UUID occurrenceId);

    @Override
    @Query("SELECT p FROM photos_occurrence p WHERE p.id = :id AND p.deleted IS FALSE")
    Optional<PhotoOccurrence> findById(@Param("id") UUID id);
}
