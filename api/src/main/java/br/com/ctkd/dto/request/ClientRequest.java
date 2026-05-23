package br.com.ctkd.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientRequest(
        @NotNull
        String name,
        @NotNull
        LocalDate birthdate,
        @CPF
        String cpf
) {
}
