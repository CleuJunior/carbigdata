package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import br.com.ctkd.gui.model.AddressModel;
import br.com.ctkd.gui.service.ApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class AddressController {

    @FXML private TableView<AddressModel> table;
    @FXML private TableColumn<AddressModel, String> streetCol;
    @FXML private TableColumn<AddressModel, String> neighborhoodCol;
    @FXML private TableColumn<AddressModel, String> zipCol;
    @FXML private TableColumn<AddressModel, String> cityCol;
    @FXML private TableColumn<AddressModel, String> stateCol;
    @FXML private TableColumn<AddressModel, String> creationDateCol;
    @FXML private TableColumn<AddressModel, String> updateDateCol;
    @FXML private TableColumn<AddressModel, String> deletedCol;
    @FXML private Label statusLabel;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ApiService api = ApiService.getInstance();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setupColumns();
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
        openAddNewWindow();
    }

    @FXML
    public void handleRefresh() {
        loadData();
    }

    // ── Add New window ────────────────────────────────────────────────────────

    private void openAddNewWindow() {
        Stage add = new Stage();
        add.initOwner(table.getScene().getWindow());
        add.initModality(Modality.APPLICATION_MODAL);
        add.setTitle("New Address");
        add.setResizable(false);

        Label titleLbl = new Label("New Address");
        titleLbl.getStyleClass().add("detail-title-lbl");
        Label subtitleLbl = new Label("Fill in all fields below");
        subtitleLbl.getStyleClass().add("detail-subtitle-lbl");
        VBox header = new VBox(4, titleLbl, subtitleLbl);
        header.getStyleClass().add("detail-header");

        // Row 1
        TextField streetField = new TextField();
        streetField.setMaxWidth(Double.MAX_VALUE);
        streetField.setPromptText("Street name");
        TextField neighborhoodField = new TextField();
        neighborhoodField.setMaxWidth(Double.MAX_VALUE);
        neighborhoodField.setPromptText("Neighborhood");
        TextField zipField = new TextField();
        zipField.setPrefWidth(110);
        zipField.setPromptText("00000-000");

        Label streetLbl = new Label("Street");
        streetLbl.getStyleClass().add("field-label");
        Label neighLbl = new Label("Neighborhood");
        neighLbl.getStyleClass().add("field-label");
        Label zipLbl = new Label("ZIP Code");
        zipLbl.getStyleClass().add("field-label");

        VBox streetGrp = new VBox(4, streetLbl, streetField);
        VBox neighGrp = new VBox(4, neighLbl, neighborhoodField);
        VBox zipGrp = new VBox(4, zipLbl, zipField);
        HBox.setHgrow(streetGrp, Priority.ALWAYS);
        HBox.setHgrow(neighGrp, Priority.ALWAYS);
        HBox row1 = new HBox(12, streetGrp, neighGrp, zipGrp);

        // Row 2
        TextField cityField = new TextField();
        cityField.setMaxWidth(Double.MAX_VALUE);
        cityField.setPromptText("City");
        TextField stateField = new TextField();
        stateField.setPrefWidth(80);
        stateField.setPromptText("SP");

        Label cityLbl = new Label("City");
        cityLbl.getStyleClass().add("field-label");
        Label stateLbl = new Label("State");
        stateLbl.getStyleClass().add("field-label");

        VBox cityGrp = new VBox(4, cityLbl, cityField);
        VBox stateGrp = new VBox(4, stateLbl, stateField);
        HBox.setHgrow(cityGrp, Priority.ALWAYS);
        HBox row2 = new HBox(12, cityGrp, stateGrp);

        VBox formCard = buildCard("ADDRESS INFORMATION", new VBox(10, row1, row2));

        Button saveBtn = new Button("Create Address");
        saveBtn.getStyleClass().add("btn-save");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        VBox actionsCard = buildCard("ACTIONS", new VBox(10, saveBtn, cancelBtn));

        VBox body = new VBox(16, formCard, actionsCard);
        body.setPadding(new Insets(20));
        body.setStyle("-fx-background-color: #f0f4f8;");

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f0f4f8;");

        VBox root = new VBox(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 580, 400);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        add.setScene(scene);

        cancelBtn.setOnAction(e -> add.close());

        saveBtn.setOnAction(e -> {
            String street = streetField.getText().trim();
            String neighborhood = neighborhoodField.getText().trim();
            String zip = zipField.getText().trim();
            String city = cityField.getText().trim();
            String state = stateField.getText().trim();
            if (street.isEmpty() || neighborhood.isEmpty() || zip.isEmpty() || city.isEmpty() || state.isEmpty()) {
                showDetailError(add, "All fields are required.");
                return;
            }
            String token = MainApp.getAuthToken();
            saveBtn.setDisable(true);
            bg(() -> {
                try {
                    api.createAddress(street, neighborhood, zip, city, state, token);
                    Platform.runLater(() -> {
                        add.close();
                        showStatus("Address created.", true);
                        loadData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        saveBtn.setDisable(false);
                        showDetailError(add, ex.getMessage());
                    });
                }
            });
        });

        add.showAndWait();
    }

    // ── Detail window ─────────────────────────────────────────────────────────

    private void openDetailWindow(AddressModel m) {
        Stage detail = new Stage();
        detail.initOwner(table.getScene().getWindow());
        detail.initModality(Modality.APPLICATION_MODAL);
        detail.setTitle("Address — " + m.streetName());
        detail.setResizable(false);

        Label titleLbl = new Label(m.streetName() + ", " + m.city());
        titleLbl.getStyleClass().add("detail-title-lbl");
        Label subtitleLbl = new Label("ID: " + m.id());
        subtitleLbl.getStyleClass().add("detail-subtitle-lbl");
        VBox header = new VBox(4, titleLbl, subtitleLbl);
        header.getStyleClass().add("detail-header");

        // Edit fields — row 1
        TextField editStreet = new TextField(m.streetName());
        editStreet.setMaxWidth(Double.MAX_VALUE);
        TextField editNeighborhood = new TextField(m.neighborhood());
        editNeighborhood.setMaxWidth(Double.MAX_VALUE);
        TextField editZip = new TextField(m.zipCode());
        editZip.setPrefWidth(110);

        Label streetLbl = new Label("Street");
        streetLbl.getStyleClass().add("field-label");
        Label neighLbl = new Label("Neighborhood");
        neighLbl.getStyleClass().add("field-label");
        Label zipLbl = new Label("ZIP Code");
        zipLbl.getStyleClass().add("field-label");

        VBox streetGrp = new VBox(4, streetLbl, editStreet);
        VBox neighGrp = new VBox(4, neighLbl, editNeighborhood);
        VBox zipGrp = new VBox(4, zipLbl, editZip);
        HBox.setHgrow(streetGrp, Priority.ALWAYS);
        HBox.setHgrow(neighGrp, Priority.ALWAYS);
        HBox row1 = new HBox(12, streetGrp, neighGrp, zipGrp);

        // Edit fields — row 2
        TextField editCity = new TextField(m.city());
        editCity.setMaxWidth(Double.MAX_VALUE);
        TextField editState = new TextField(m.state());
        editState.setPrefWidth(80);

        Label cityLbl = new Label("City");
        cityLbl.getStyleClass().add("field-label");
        Label stateLbl = new Label("State");
        stateLbl.getStyleClass().add("field-label");

        VBox cityGrp = new VBox(4, cityLbl, editCity);
        VBox stateGrp = new VBox(4, stateLbl, editState);
        HBox.setHgrow(cityGrp, Priority.ALWAYS);
        HBox row2 = new HBox(12, cityGrp, stateGrp);

        VBox editCard = buildCard("EDIT INFORMATION", new VBox(10, row1, row2));

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(32);
        infoGrid.setVgap(6);
        addInfoRow(infoGrid, "Created", m.creationDate() != null ? DATETIME_FMT.format(m.creationDate()) : "—", 0);
        addInfoRow(infoGrid, "Updated", m.updateDate() != null ? DATETIME_FMT.format(m.updateDate()) : "—", 1);
        addInfoRow(infoGrid, "Deleted", m.deleted() ? "Yes" : "No", 2);
        VBox infoCard = buildCard("RECORD INFO", infoGrid);

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("btn-save");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        Button deleteBtn = new Button("Delete Address");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        VBox actionsCard = buildCard("ACTIONS", new VBox(10, saveBtn, deleteBtn));

        VBox body = new VBox(16, editCard, infoCard, actionsCard);
        body.setPadding(new Insets(20));
        body.setStyle("-fx-background-color: #f0f4f8;");

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f0f4f8;");

        VBox root = new VBox(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 560, 520);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        detail.setScene(scene);

        saveBtn.setOnAction(e -> {
            String street = editStreet.getText().trim();
            String neighborhood = editNeighborhood.getText().trim();
            String zip = editZip.getText().trim();
            String city = editCity.getText().trim();
            String state = editState.getText().trim();
            if (street.isEmpty() || neighborhood.isEmpty() || zip.isEmpty() || city.isEmpty() || state.isEmpty()) {
                showDetailError(detail, "All fields are required.");
                return;
            }
            String token = MainApp.getAuthToken();
            bg(() -> {
                try {
                    api.updateAddress(m.id(), street, neighborhood, zip, city, state, token);
                    Platform.runLater(() -> {
                        detail.close();
                        showStatus("Address updated.", true);
                        loadData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showDetailError(detail, ex.getMessage()));
                }
            });
        });

        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete \"" + m.streetName() + ", " + m.city() + "\"? This cannot be undone.",
                    ButtonType.YES, ButtonType.NO);
            confirm.initOwner(detail);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    bg(() -> {
                        try {
                            api.deleteAddress(m.id(), MainApp.getAuthToken());
                            Platform.runLater(() -> {
                                detail.close();
                                showStatus("Address deleted.", true);
                                loadData();
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> showDetailError(detail, ex.getMessage()));
                        }
                    });
                }
            });
        });

        detail.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VBox buildCard(String sectionTitle, Node content) {
        Label lbl = new Label(sectionTitle);
        lbl.getStyleClass().add("detail-section-lbl");
        Separator sep = new Separator();
        VBox card = new VBox(8, lbl, sep, content);
        card.getStyleClass().add("detail-card");
        return card;
    }

    private void addInfoRow(GridPane grid, String label, String value, int row) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        Label val = new Label(value);
        val.getStyleClass().add("ro-value");
        grid.addRow(row, lbl, val);
    }

    private void showDetailError(Stage owner, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void loadData() {
        bg(() -> {
            try {
                var list = api.listAddresses(MainApp.getAuthToken());
                Platform.runLater(() -> table.getItems().setAll(list));
            } catch (Exception e) {
                Platform.runLater(() -> showStatus(e.getMessage(), false));
            }
        });
    }

    private void setupColumns() {
        streetCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().streetName()));
        neighborhoodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().neighborhood()));
        zipCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().zipCode()));
        cityCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().city()));
        stateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().state()));
        creationDateCol.setCellValueFactory(d -> {
            var dt = d.getValue().creationDate();
            return new SimpleStringProperty(dt != null ? DATETIME_FMT.format(dt) : "");
        });
        updateDateCol.setCellValueFactory(d -> {
            var dt = d.getValue().updateDate();
            return new SimpleStringProperty(dt != null ? DATETIME_FMT.format(dt) : "");
        });
        deletedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().deleted() ? "Yes" : "No"));
    }

    private void showStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add(ok ? "success-label" : "error-label");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void bg(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }
}
