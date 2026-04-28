package br.com.ctkd.exceptions;

import br.com.ctkd.i18n.LocalizedMessageTranslationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidHandler(HttpServletRequest request, MethodArgumentNotValidException cause) {
        var message = cause.getBindingResult().getFieldErrors().stream()
                .map(fe -> {
                    var codes = fe.getCodes();
                    var annotationName = codes != null ? codes[codes.length - 1] : "";
                    var translated = translation.translateMessageOrDefault("validation." + annotationName, fe.getDefaultMessage());
                    return fe.getField() + ": " + translated;
                })
                .collect(Collectors.joining(", "));

        var err = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .statusErrorMessage(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .trace(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("{} URI: {}", HttpStatus.BAD_REQUEST, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolationHandler(HttpServletRequest request, ConstraintViolationException cause) {
        var message = cause.getConstraintViolations().stream()
                .map(cv -> {
                    var path = cv.getPropertyPath().toString();
                    var field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    var annotationName = cv.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
                    var translated = translation.translateMessageOrDefault("validation." + annotationName, cv.getMessage());
                    return field + ": " + translated;
                })
                .collect(Collectors.joining(", "));

        var err = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .statusErrorMessage(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .trace(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("{} URI: {}", HttpStatus.BAD_REQUEST, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    private static final Pattern CONSTRAINT_NAME_PATTERN = Pattern.compile("constraint \"([^\"]+)\"");

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrityViolationHandler(HttpServletRequest request, DataIntegrityViolationException cause) {
        var rootMessage = cause.getMostSpecificCause().getMessage();
        var matcher = CONSTRAINT_NAME_PATTERN.matcher(rootMessage != null ? rootMessage : "");
        var messageKey = matcher.find() ? "constraint." + matcher.group(1) : "data.integrity.violation";
        var message = translation.translateMessage(messageKey, "data.integrity.violation");

        var err = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .statusErrorMessage(HttpStatus.CONFLICT.getReasonPhrase())
                .message(message)
                .trace(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("{} URI: {} - {}", HttpStatus.CONFLICT, request.getRequestURI(), rootMessage);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

}
