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
}
