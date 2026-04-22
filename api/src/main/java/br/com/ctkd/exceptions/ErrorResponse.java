package br.com.ctkd.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@JsonPropertyOrder({"timestamp", "status", "status_message", "error_message"})
@Builder
public record ErrorResponse(
        Integer status,
        @JsonProperty("status_message")
        String statusErrorMessage,
        @JsonProperty("error_message")
        String message,
        String trace,
        @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:SS")
        LocalDateTime timestamp
) {
}
