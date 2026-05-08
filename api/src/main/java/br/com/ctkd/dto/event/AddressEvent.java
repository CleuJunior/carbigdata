package br.com.ctkd.dto.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressEvent(
        String id,
        String streetName,
        String neighborhood,
        String zipCode,
        String city,
        String state,
        boolean deleted
) {

}
