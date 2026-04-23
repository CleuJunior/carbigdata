package br.com.ctkd.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthRequest(
        @NotNull String username,
        @NotNull String password
) {
}
