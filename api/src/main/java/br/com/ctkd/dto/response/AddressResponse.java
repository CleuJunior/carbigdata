package br.com.ctkd.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressResponse(
        String id,
        String streetName,
        String neighborhood,
        String zipCode,
        String city,
        String state,
        OffsetDateTime creationDate,
        OffsetDateTime updateDate,
        boolean deleted
) {
}
