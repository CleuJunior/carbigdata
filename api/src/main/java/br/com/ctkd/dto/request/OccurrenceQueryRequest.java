package br.com.ctkd.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record OccurrenceQueryRequest(
        String clientName,
        String cpf,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate occurrenceDate,
        String city,
        String sortBy,
        String sortDirection
) {
}
