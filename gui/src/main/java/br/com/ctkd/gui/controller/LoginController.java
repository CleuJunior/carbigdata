package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import br.com.ctkd.gui.service.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final ApiService apiService = ApiService.getInstance();

    @FXML
    public void initialize() {
        usernameField.setText("admin");
        passwordField.setText("admin");
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Logging in...");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Thread t = new Thread(() -> {
            try {
                String token = apiService.login(username, password);
                Platform.runLater(() -> {
                    try {
                        MainApp.showMainScreen(token);
                    } catch (Exception e) {
                        showError("Screen load error: " + e.getMessage());
                        resetButton();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(e.getMessage() != null ? e.getMessage() : "Connection failed — is the server running?");
                    resetButton();
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void resetButton() {
        loginButton.setDisable(false);
        loginButton.setText("Login");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
