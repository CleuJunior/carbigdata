package br.com.ctkd.gui.ui;

import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateConverters {

    private DateConverters() {}

    public static StringConverter<LocalDate> forPattern(String pattern) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate d) {
                return d != null ? fmt.format(d) : "";
            }

            @Override
            public LocalDate fromString(String s) {
                try {
                    return s != null && !s.isBlank() ? LocalDate.parse(s, fmt) : null;
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }
}
