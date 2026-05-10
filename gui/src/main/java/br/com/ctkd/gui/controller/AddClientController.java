package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import br.com.ctkd.gui.service.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddClientController {

    @FXML private TextField nameField;
    @FXML private DatePicker birthdatePicker;
    @FXML private TextField cpfField;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    @Setter
    private String token;
    private final ApiService apiService = ApiService.getInstance();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        birthdatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_FMT.format(date) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) return null;
                try {
                    return LocalDate.parse(text, DATE_FMT);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        cpfField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 11) digits = digits.substring(0, 11);
            String formatted = formatCpf(digits);
            if (!newVal.equals(formatted)) {
                cpfField.setText(formatted);
                cpfField.positionCaret(formatted.length());
            }
        });
    }

    @FXML
    public void handleSave() {
        String name = nameField.getText().trim();
        LocalDate birthdate = birthdatePicker.getValue();
        String cpf = cpfField.getText().trim();

        if (name.isEmpty() || birthdate == null || cpf.isEmpty()) {
            showStatus("All fields are required.", false);
            return;
        }

        saveButton.setDisable(true);
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        new Thread(() -> {
            try {
                apiService.createClient(name, birthdate, cpf, token);
                Platform.runLater(() -> {
                    showStatus("Client \"" + name + "\" created successfully!", true);
                    saveButton.setDisable(false);
                    clearForm();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus(e.getMessage(), false);
                    saveButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    public void handleLogout() {
        try {
            MainApp.showLoginScreen();
        } catch (Exception e) {
            showStatus("Logout error: " + e.getMessage(), false);
        }
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add(success ? "success-label" : "error-label");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void clearForm() {
        nameField.clear();
        birthdatePicker.setValue(null);
        cpfField.clear();
    }

    private String formatCpf(String digits) {
        if (digits.length() <= 3) return digits;
        if (digits.length() <= 6) return digits.substring(0, 3) + "." + digits.substring(3);
        if (digits.length() <= 9) return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6);
        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9);
    }
}
