package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import br.com.ctkd.gui.model.ClientModel;
import br.com.ctkd.gui.service.ApiService;
import br.com.ctkd.gui.ui.CpfFormatter;
import br.com.ctkd.gui.ui.FxUtils;
import br.com.ctkd.gui.ui.WindowBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ClientController extends BaseController {

    @FXML private TableView<ClientModel> table;
    @FXML private TableColumn<ClientModel, String> nameCol;
    @FXML private TableColumn<ClientModel, String> cpfCol;
    @FXML private TableColumn<ClientModel, String> birthdateCol;
    @FXML private TableColumn<ClientModel, String> creationDateCol;
    @FXML private TableColumn<ClientModel, String> updateDateCol;
    @FXML private TableColumn<ClientModel, String> deletedCol;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ApiService api = ApiService.getInstance();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        cpfCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().cpf()));
        birthdateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().birthdate() != null ? DATE_FMT.format(d.getValue().birthdate()) : ""));
        creationDateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().creationDate() != null ? DT_FMT.format(d.getValue().creationDate()) : ""));
        updateDateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().updateDate() != null ? DT_FMT.format(d.getValue().updateDate()) : ""));
        deletedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().deleted() ? "Yes" : "No"));
        loadData();
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                ClientModel sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) openDetailWindow(sel);
            }
        });
    }

    @FXML
    public void handleAddNew() {
        TextField nameField = FxUtils.styledTextField("Full name");
        TextField cpfField  = FxUtils.styledTextField("000.000.000-00");
        CpfFormatter.install(cpfField);
        DatePicker birthPicker = FxUtils.styledDatePicker();

        Button saveBtn   = FxUtils.saveButton("Create Client");
        Button cancelBtn = FxUtils.cancelButton();

        Stage stage = WindowBuilder.create(table.getScene().getWindow(), "New Client")
                .header("New Client", "Fill in all fields below")
                .size(560, 360)
                .addCard(FxUtils.card("CLIENT INFORMATION", FxUtils.fieldsRow(
                        FxUtils.fieldGroup("Full Name", nameField),
                        FxUtils.fieldGroup("CPF", cpfField),
                        FxUtils.fieldGroup("Birthdate", birthPicker))))
                .addCard(FxUtils.card("ACTIONS", new VBox(10, saveBtn, cancelBtn)))
                .build();

        cancelBtn.setOnAction(e -> stage.close());
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            LocalDate bd = birthPicker.getValue();
            String cpf  = cpfField.getText().trim();
            if (name.isEmpty() || bd == null || cpf.isEmpty()) {
                FxUtils.errorDialog(stage, "All fields are required.");
                return;
            }
            saveBtn.setDisable(true);
            FxUtils.async(() -> {
                try {
                    api.createClient(name, bd, cpf, MainApp.getAuthToken());
                    Platform.runLater(() -> { stage.close(); showStatus("Client created.", true); loadData(); });
                } catch (Exception ex) {
                    Platform.runLater(() -> { saveBtn.setDisable(false); FxUtils.errorDialog(stage, ex.getMessage()); });
                }
            });
        });
        stage.showAndWait();
    }

    @Override
    protected void loadData() {
        FxUtils.async(() -> {
            try {
                var list = api.listClients(MainApp.getAuthToken());
                Platform.runLater(() -> table.getItems().setAll(list));
            } catch (Exception e) {
                Platform.runLater(() -> showStatus(e.getMessage(), false));
            }
        });
    }

    private void openDetailWindow(ClientModel m) {
        TextField nameField = FxUtils.styledTextField("Full name", m.name());
        TextField cpfField  = FxUtils.styledTextField("000.000.000-00", m.cpf());
        CpfFormatter.install(cpfField);
        DatePicker birthPicker = FxUtils.styledDatePicker(m.birthdate());

        Button saveBtn   = FxUtils.saveButton("Save Changes");
        Button deleteBtn = FxUtils.dangerButton("Delete Client");

        Stage stage = WindowBuilder.create(table.getScene().getWindow(), "Client — " + m.name())
                .header(m.name(), "ID: " + m.id())
                .size(560, 500)
                .addCard(FxUtils.card("EDIT INFORMATION", FxUtils.fieldsRow(
                        FxUtils.fieldGroup("Full Name", nameField),
                        FxUtils.fieldGroup("CPF", cpfField),
                        FxUtils.fieldGroup("Birthdate", birthPicker))))
                .addCard(FxUtils.recordInfoCard(m.creationDate(), m.updateDate(), m.deleted()))
                .addCard(FxUtils.card("ACTIONS", new VBox(10, saveBtn, deleteBtn)))
                .build();

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            LocalDate bd = birthPicker.getValue();
            String cpf  = cpfField.getText().trim();
            if (name.isEmpty() || bd == null || cpf.isEmpty()) {
                FxUtils.errorDialog(stage, "All fields are required.");
                return;
            }
            FxUtils.async(() -> {
                try {
                    api.updateClient(m.id(), name, bd, cpf, MainApp.getAuthToken());
                    Platform.runLater(() -> { stage.close(); showStatus("Client updated.", true); loadData(); });
                } catch (Exception ex) {
                    Platform.runLater(() -> FxUtils.errorDialog(stage, ex.getMessage()));
                }
            });
        });

        deleteBtn.setOnAction(e -> {
            if (FxUtils.confirm(stage, "Delete \"" + m.name() + "\"? This cannot be undone.")) {
                FxUtils.async(() -> {
                    try {
                        api.deleteClient(m.id(), MainApp.getAuthToken());
                        Platform.runLater(() -> { stage.close(); showStatus("Client deleted.", true); loadData(); });
                    } catch (Exception ex) {
                        Platform.runLater(() -> FxUtils.errorDialog(stage, ex.getMessage()));
                    }
                });
            }
        });

        stage.showAndWait();
    }
}
