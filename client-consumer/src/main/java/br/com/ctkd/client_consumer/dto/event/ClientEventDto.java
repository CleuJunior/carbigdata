package br.com.ctkd.client_consumer.dto.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClientEventDto(
        String id,
        String name,
        LocalDate birthdate,
        String cpf,
        boolean deleted) {
}
