package br.com.ctkd.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Shared base for the three CRUD controllers (Client, Address, Occurrence).
 * Provides a unified status label, a no-op refresh hook, and async data loading contract.
 */
public abstract class BaseController {

    @FXML protected Label statusLabel;

    @FXML
    public void handleRefresh() {
        loadData();
    }

    protected abstract void loadData();

    protected void showStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add(ok ? "success-label" : "error-label");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
