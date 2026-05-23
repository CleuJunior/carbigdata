package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import br.com.ctkd.gui.model.AddressModel;
import br.com.ctkd.gui.service.ApiService;
import br.com.ctkd.gui.ui.FxUtils;
import br.com.ctkd.gui.ui.WindowBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class AddressController extends BaseController {

    @FXML private TableView<AddressModel> table;
    @FXML private TableColumn<AddressModel, String> streetCol;
    @FXML private TableColumn<AddressModel, String> neighborhoodCol;
    @FXML private TableColumn<AddressModel, String> zipCol;
    @FXML private TableColumn<AddressModel, String> cityCol;
    @FXML private TableColumn<AddressModel, String> stateCol;
    @FXML private TableColumn<AddressModel, String> creationDateCol;
    @FXML private TableColumn<AddressModel, String> updateDateCol;
    @FXML private TableColumn<AddressModel, String> deletedCol;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ApiService api = ApiService.getInstance();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        streetCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().streetName()));
        neighborhoodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().neighborhood()));
        zipCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().zipCode()));
        cityCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().city()));
        stateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().state()));
        creationDateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().creationDate() != null ? DT_FMT.format(d.getValue().creationDate()) : ""));
        updateDateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().updateDate() != null ? DT_FMT.format(d.getValue().updateDate()) : ""));
        deletedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().deleted() ? "Yes" : "No"));
        loadData();
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                AddressModel sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) openDetailWindow(sel);
            }
        });
    }

    @FXML
    public void handleAddNew() {
        TextField streetField       = FxUtils.styledTextField("Street name");
        TextField neighborhoodField = FxUtils.styledTextField("Neighborhood");
        TextField zipField          = FxUtils.styledTextField("00000-000");
        TextField cityField         = FxUtils.styledTextField("City");
        TextField stateField        = FxUtils.styledTextField("SP");
        zipField.setMaxWidth(110);
        stateField.setMaxWidth(80);

        Button saveBtn   = FxUtils.saveButton("Create Address");
        Button cancelBtn = FxUtils.cancelButton();

        Stage stage = WindowBuilder.create(table.getScene().getWindow(), "New Address")
                .header("New Address", "Fill in all fields below")
                .size(580, 400)
                .addCard(FxUtils.card("ADDRESS INFORMATION", new VBox(10,
                        FxUtils.fieldsRow(
                                FxUtils.fieldGroup("Street", streetField),
                                FxUtils.fieldGroup("Neighborhood", neighborhoodField),
                                FxUtils.fieldGroup("ZIP Code", zipField)),
                        FxUtils.fieldsRow(
                                FxUtils.fieldGroup("City", cityField),
                                FxUtils.fieldGroup("State", stateField)))))
                .addCard(FxUtils.card("ACTIONS", new VBox(10, saveBtn, cancelBtn)))
                .build();

        cancelBtn.setOnAction(e -> stage.close());
        saveBtn.setOnAction(e -> {
            String street       = streetField.getText().trim();
            String neighborhood = neighborhoodField.getText().trim();
            String zip          = zipField.getText().trim();
            String city         = cityField.getText().trim();
            String state        = stateField.getText().trim();
            if (street.isEmpty() || neighborhood.isEmpty() || zip.isEmpty() || city.isEmpty() || state.isEmpty()) {
                FxUtils.errorDialog(stage, "All fields are required.");
                return;
            }
            saveBtn.setDisable(true);
            FxUtils.async(() -> {
                try {
                    api.createAddress(street, neighborhood, zip, city, state, MainApp.getAuthToken());
                    Platform.runLater(() -> { stage.close(); showStatus("Address created.", true); loadData(); });
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
                var list = api.listAddresses(MainApp.getAuthToken());
                Platform.runLater(() -> table.getItems().setAll(list));
            } catch (Exception e) {
                Platform.runLater(() -> showStatus(e.getMessage(), false));
            }
        });
    }

    private void openDetailWindow(AddressModel m) {
        TextField streetField       = FxUtils.styledTextField("Street name",   m.streetName());
        TextField neighborhoodField = FxUtils.styledTextField("Neighborhood",  m.neighborhood());
        TextField zipField          = FxUtils.styledTextField("00000-000",     m.zipCode());
        TextField cityField         = FxUtils.styledTextField("City",          m.city());
        TextField stateField        = FxUtils.styledTextField("SP",            m.state());
        zipField.setMaxWidth(110);
        stateField.setMaxWidth(80);

        Button saveBtn   = FxUtils.saveButton("Save Changes");
        Button deleteBtn = FxUtils.dangerButton("Delete Address");

        Stage stage = WindowBuilder.create(table.getScene().getWindow(), "Address — " + m.streetName())
                .header(m.streetName() + ", " + m.city(), "ID: " + m.id())
                .size(580, 500)
                .addCard(FxUtils.card("EDIT INFORMATION", new VBox(10,
                        FxUtils.fieldsRow(
                                FxUtils.fieldGroup("Street", streetField),
                                FxUtils.fieldGroup("Neighborhood", neighborhoodField),
                                FxUtils.fieldGroup("ZIP Code", zipField)),
                        FxUtils.fieldsRow(
                                FxUtils.fieldGroup("City", cityField),
                                FxUtils.fieldGroup("State", stateField)))))
                .addCard(FxUtils.recordInfoCard(m.creationDate(), m.updateDate(), m.deleted()))
                .addCard(FxUtils.card("ACTIONS", new VBox(10, saveBtn, deleteBtn)))
                .build();

        saveBtn.setOnAction(e -> {
            String street       = streetField.getText().trim();
            String neighborhood = neighborhoodField.getText().trim();
            String zip          = zipField.getText().trim();
            String city         = cityField.getText().trim();
            String state        = stateField.getText().trim();
            if (street.isEmpty() || neighborhood.isEmpty() || zip.isEmpty() || city.isEmpty() || state.isEmpty()) {
                FxUtils.errorDialog(stage, "All fields are required.");
                return;
            }
            FxUtils.async(() -> {
                try {
                    api.updateAddress(m.id(), street, neighborhood, zip, city, state, MainApp.getAuthToken());
                    Platform.runLater(() -> { stage.close(); showStatus("Address updated.", true); loadData(); });
                } catch (Exception ex) {
                    Platform.runLater(() -> FxUtils.errorDialog(stage, ex.getMessage()));
                }
            });
        });

        deleteBtn.setOnAction(e -> {
            if (FxUtils.confirm(stage, "Delete \"" + m.streetName() + ", " + m.city() + "\"? This cannot be undone.")) {
                FxUtils.async(() -> {
                    try {
                        api.deleteAddress(m.id(), MainApp.getAuthToken());
                        Platform.runLater(() -> { stage.close(); showStatus("Address deleted.", true); loadData(); });
                    } catch (Exception ex) {
                        Platform.runLater(() -> FxUtils.errorDialog(stage, ex.getMessage()));
                    }
                });
            }
        });

        stage.showAndWait();
    }
}
