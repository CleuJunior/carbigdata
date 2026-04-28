package br.com.ctkd.i18n;

import br.com.ctkd.exceptions.NotFoundException;
import br.com.ctkd.exceptions.OccurrenceStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LocalizedMessageTranslationService {
    private final MessageSource source;

    public String translateMessage(NotFoundException exception) {
        var locale = LocaleContextHolder.getLocale();
        return source.getMessage(exception.getMessage(), exception.getArgs(), locale);
    }

    public String translateMessage(OccurrenceStatusException exception) {
        var locale = LocaleContextHolder.getLocale();
        return source.getMessage(exception.getMessage(), new Object[]{}, locale);
    }

    public String translateMessage(String key) {
        var locale = LocaleContextHolder.getLocale();
        return source.getMessage(key, new Object[]{}, locale);
    }

    public String translateMessage(String key, String fallbackKey) {
        var locale = LocaleContextHolder.getLocale();
        var fallback = source.getMessage(fallbackKey, new Object[]{}, locale);
        return source.getMessage(key, new Object[]{}, fallback, locale);
    }

    public String translateMessageOrDefault(String key, String defaultMessage) {
        var locale = LocaleContextHolder.getLocale();
        return source.getMessage(key, new Object[]{}, defaultMessage, locale);
    }
}
