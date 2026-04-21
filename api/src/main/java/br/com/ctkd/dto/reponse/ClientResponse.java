package br.com.ctkd.dto.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClientResponse(
        String id,
        String name,
        LocalDate birthdate,
        String cpf,
        OffsetDateTime creationDate,
        OffsetDateTime updateDate,
        boolean deleted
) {
}
