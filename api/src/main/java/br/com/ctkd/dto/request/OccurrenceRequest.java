package br.com.ctkd.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record OccurrenceRequest(
        @NotNull String clientId,
        @NotNull String addressId,
        @NotNull LocalDate occurrenceDate
) {
}
