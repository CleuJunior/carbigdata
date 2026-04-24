package br.com.ctkd.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record PhotoOccurrenceRequest(
        @NotNull String occurrenceId,
        @NotNull String pathBucket,
        @NotNull String hash
) {
}
