package br.com.ctkd.gui.ui;

import javafx.scene.control.TextField;

public final class CpfFormatter {

    private CpfFormatter() {}

    public static void install(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 11) digits = digits.substring(0, 11);
            String formatted = format(digits);
            if (!newVal.equals(formatted)) {
                field.setText(formatted);
                field.positionCaret(formatted.length());
            }
        });
    }

    private static String format(String d) {
        if (d.length() <= 3) return d;
        if (d.length() <= 6) return d.substring(0, 3) + "." + d.substring(3);
        if (d.length() <= 9) return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6);
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }
}
