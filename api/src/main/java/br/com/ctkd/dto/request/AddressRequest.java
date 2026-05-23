package br.com.ctkd.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressRequest(
        @NotNull String streetName,
        @NotNull String neighborhood,
        @NotNull String zipCode,
        @NotNull String city,
        @NotNull String state
) {
}
