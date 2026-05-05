package br.com.ctkd.client_consumer.repository;

import br.com.ctkd.client_consumer.document.ClientDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClientDocumentRepository extends MongoRepository<ClientDocument, String> {

    Optional<ClientDocument> findByClientId(String clientId);
}
