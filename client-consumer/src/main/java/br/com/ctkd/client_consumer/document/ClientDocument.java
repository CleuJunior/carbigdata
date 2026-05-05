package br.com.ctkd.client_consumer.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "client-event")
public class ClientDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("client_id")
    private String clientId;

    @Field("client_name")
    private String name;

    @Field("birthdate")
    private LocalDate birthdate;

    @Field("cpf_number")
    private String cpf;

    @Field("event_type")
    private String eventType;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
