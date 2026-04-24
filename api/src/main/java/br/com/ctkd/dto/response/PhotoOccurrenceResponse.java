package br.com.ctkd.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PhotoOccurrenceResponse(
        String id,
        String occurrenceId,
        String pathBucket,
        String hash,
        OffsetDateTime creationDate,
        OffsetDateTime updateDate,
        boolean deleted
) {
}
