package br.com.ctkd.client_consumer.dto.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressEventDto(
        String id,
        String streetName,
        String neighborhood,
        String zipCode,
        String city,
        String state,
        boolean deleted) {
}
