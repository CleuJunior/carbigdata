package br.com.ctkd.repository;

import br.com.ctkd.domain.Occurrence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OccurrenceRepository extends JpaRepository<Occurrence, UUID>, JpaSpecificationExecutor<Occurrence> {

    @Override
    @Query("""
                SELECT c FROM occurrences c
                WHERE c.deleted IS FALSE AND c.id = :id
            """)
    Optional<Occurrence> findById(@Param("id") UUID id);

    @Override
    @Query("""
                SELECT c FROM occurrences c
                WHERE c.deleted IS FALSE
            """)
    List<Occurrence> findAll();

    @Query("""
                SELECT c FROM occurrences c
                WHERE c.deleted IS FALSE
            """)
    Page<Occurrence> findAll(Pageable pageable);
}
