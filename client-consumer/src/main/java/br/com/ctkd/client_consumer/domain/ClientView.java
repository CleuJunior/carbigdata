package br.com.ctkd.client_consumer.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "client-view")
public class ClientView extends View {

    @Indexed(unique = true)
    @Field("client_id")
    private String clientId;

    @Field("client_name")
    private String name;

    @Field("birthdate")
    private LocalDate birthdate;

    @Field("cpf_number")
    private String cpf;

}
