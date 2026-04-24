package br.com.ctkd.dto.response;

import br.com.ctkd.domain.StatusOccurrence;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OccurrenceResponse(
        String id,
        LocalDate occurrenceDate,
        StatusOccurrence status,
        ClientResponse client,
        AddressResponse address,
        List<PhotoOccurrenceResponse> photos,
        OffsetDateTime creationDate,
        OffsetDateTime updateDate,
        boolean deleted
) {
}
