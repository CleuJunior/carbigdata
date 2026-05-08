package br.com.ctkd.client_consumer.repository;

import br.com.ctkd.client_consumer.domain.AddressView;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AddressDocumentRepository extends MongoRepository<AddressView, String> {

    Optional<AddressView> findByAddressId(String addressId);
}
