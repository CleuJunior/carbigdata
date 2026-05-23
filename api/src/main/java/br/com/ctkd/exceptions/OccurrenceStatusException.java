package br.com.ctkd.exceptions;

import lombok.Getter;

@Getter
public class OccurrenceStatusException extends RuntimeException {

    public OccurrenceStatusException(String message) {
        super(message);
    }
}
