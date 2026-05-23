package br.com.ctkd.repository;

import br.com.ctkd.domain.Address;
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
public interface AddressRepository extends JpaRepository<Address, UUID> {

    @Override
    @Query("""
                SELECT a FROM addresses a
                WHERE a.deleted IS FALSE AND a.id = :id
            """)
    Optional<Address> findById(@Param("id") UUID id);

    @Override
    @Query("""
                SELECT a FROM addresses a
                WHERE a.deleted IS FALSE
            """)
    List<Address> findAll();

    @Override
    @Query("""
                SELECT a FROM addresses a
                WHERE a.deleted IS FALSE
            """)
    Page<Address> findAll(@NonNull Pageable pageable);
}
