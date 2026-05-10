package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private Button clientsBtn;
    @FXML private Button addressesBtn;
    @FXML private Button occurrencesBtn;

    private Button activeBtn;

    @FXML
    public void initialize() {
        showClients();
    }

    @FXML
    public void showClients() {
        loadView("client-view.fxml", clientsBtn);
    }

    @FXML
    public void showAddresses() {
        loadView("address-view.fxml", addressesBtn);
    }

    @FXML
    public void showOccurrences() {
        loadView("occurrence-view.fxml", occurrencesBtn);
    }

    @FXML
    public void handleLogout() {
        try {
            MainApp.showLoginScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxml, Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxml));
            Node view = loader.load();
            contentPane.getChildren().setAll(view);

            if (activeBtn != null) activeBtn.getStyleClass().remove("nav-active");
            btn.getStyleClass().add("nav-active");
            activeBtn = btn;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
