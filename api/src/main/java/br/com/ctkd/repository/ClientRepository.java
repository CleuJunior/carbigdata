package br.com.ctkd.repository;

import br.com.ctkd.domain.Client;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Override
    @Query("""
                SELECT c FROM clients c
                WHERE c.deleted IS FALSE AND c.id = :id
            """)
    Optional<Client> findById(@Param("id") UUID id);

    @Override
    @Query("""
                SELECT c FROM clients c
                WHERE c.deleted IS FALSE
            """)
    List<Client> findAll();

    @Override
    @Query("""
                SELECT c FROM clients c
                WHERE c.deleted IS FALSE
            """)
    Page<Client> findAll(@NonNull Pageable pageable);

    @Query("SELECT COUNT(c.id) FROM clients c WHERE c.deleted IS FALSE")
    long count();
}
