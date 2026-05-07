package br.com.ctkd.client_consumer.repository;

import br.com.ctkd.client_consumer.domain.ClientView;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClientDocumentRepository extends MongoRepository<ClientView, String> {

    Optional<ClientView> findByClientId(String clientId);
}
