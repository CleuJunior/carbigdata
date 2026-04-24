package br.com.ctkd.exceptions;

import br.com.ctkd.i18n.LocalizedMessageTranslationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ErrorExceptionHandler {

    private final LocalizedMessageTranslationService translation;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFoundHandler(HttpServletRequest request, NotFoundException cause) {
        var message = translation.translateMessage(cause);

        var err = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .statusErrorMessage(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(message)
                .trace(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("{} URI: {}", HttpStatus.NOT_FOUND, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(OccurrenceStatusException.class)
    public ResponseEntity<ErrorResponse> occurrenceStatusConflict(HttpServletRequest request, OccurrenceStatusException cause) {
        var message = translation.translateMessage(cause);

        var err = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .statusErrorMessage(HttpStatus.CONFLICT.getReasonPhrase())
                .message(message)
                .trace(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("{} URI: {}", HttpStatus.CONFLICT, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

}
