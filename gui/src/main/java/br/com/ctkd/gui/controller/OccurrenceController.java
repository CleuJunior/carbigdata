package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import br.com.ctkd.gui.model.AddressModel;
import br.com.ctkd.gui.model.ClientModel;
import br.com.ctkd.gui.model.OccurrenceModel;
import br.com.ctkd.gui.model.PhotoModel;
import br.com.ctkd.gui.service.ApiService;
import br.com.ctkd.gui.ui.DateConverters;
import br.com.ctkd.gui.ui.FxUtils;
import br.com.ctkd.gui.ui.WindowBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OccurrenceController extends BaseController {

    @FXML private TableView<OccurrenceModel> table;
    @FXML private TableColumn<OccurrenceModel, String> dateCol;
    @FXML private TableColumn<OccurrenceModel, String> statusCol;
    @FXML private TableColumn<OccurrenceModel, String> clientCol;
    @FXML private TableColumn<OccurrenceModel, String> cityCol;
    @FXML private TableColumn<OccurrenceModel, String> creationDateCol;
    @FXML private TableColumn<OccurrenceModel, String> updateDateCol;
    @FXML private TableColumn<OccurrenceModel, String> deletedCol;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ApiService api = ApiService.getInstance();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().occurrenceDate() != null ? DATE_FMT.format(d.getValue().occurrenceDate()) : ""));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        clientCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().clientName()));
        cityCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().addressCity()));
        creationDateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().creationDate() != null ? DT_FMT.format(d.getValue().creationDate()) : ""));
        updateDateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().updateDate() != null ? DT_FMT.format(d.getValue().updateDate()) : ""));
        deletedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().deleted() ? "Yes" : "No"));
        loadData();
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                OccurrenceModel sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) openDetailWindow(sel);
            }
        });
    }

    @FXML
    public void handleAddNew() {
        showStatus("Loading clients and addresses...", true);
        FxUtils.async(() -> {
            try {
                var clients   = api.listClients(MainApp.getAuthToken());
                var addresses = api.listAddresses(MainApp.getAuthToken());
                Platform.runLater(() -> {
                    statusLabel.setVisible(false);
                    statusLabel.setManaged(false);
                    openAddNewWindow(clients, addresses);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showStatus(e.getMessage(), false));
            }
        });
    }

    @Override
    protected void loadData() {
        FxUtils.async(() -> {
            try {
                var list = api.listOccurrences(MainApp.getAuthToken());
                Platform.runLater(() -> table.getItems().setAll(list));
            } catch (Exception e) {
                Platform.runLater(() -> showStatus(e.getMessage(), false));
            }
        });
    }

    // ── Add New window ────────────────────────────────────────────────────────

    private void openAddNewWindow(List<ClientModel> clients, List<AddressModel> addresses) {
        ComboBox<ClientModel> clientCombo   = clientComboBox(clients);
        ComboBox<AddressModel> addressCombo = addressComboBox(addresses);
        DatePicker datePicker = FxUtils.styledDatePicker(LocalDate.now());

        List<File> selectedPhotos = new ArrayList<>();
        FlowPane photosFlow       = emptyPhotosPane();
        Button addPhotoBtn        = new Button("+ Add Photos");
        addPhotoBtn.getStyleClass().add("btn-add-photo");

        Button saveBtn   = FxUtils.saveButton("Create Occurrence");
        Button cancelBtn = FxUtils.cancelButton();

        Stage stage = WindowBuilder.create(table.getScene().getWindow(), "New Occurrence")
                .header("New Occurrence", "Fill in all fields below")
                .size(620, 520)
                .addCard(FxUtils.card("OCCURRENCE DETAILS", FxUtils.fieldsRow(
                        FxUtils.fieldGroup("Client", clientCombo),
                        FxUtils.fieldGroup("Address", addressCombo),
                        FxUtils.fieldGroup("Date", datePicker))))
                .addCard(FxUtils.card("PHOTOS", new VBox(10, photosFlow, addPhotoBtn)))
                .addCard(FxUtils.card("ACTIONS", new VBox(10, saveBtn, cancelBtn)))
                .build();

        addPhotoBtn.setOnAction(e -> {
            FileChooser chooser = photoChooser("Select Photos");
            List<File> picked = chooser.showOpenMultipleDialog(stage);
            if (picked != null && !picked.isEmpty()) {
                selectedPhotos.addAll(picked);
                photosFlow.getChildren().setAll(selectedPhotos.stream().map(f -> FxUtils.photoChip(f.getName())).toList());
            }
        });

        cancelBtn.setOnAction(e -> stage.close());
        saveBtn.setOnAction(e -> {
            ClientModel  client  = clientCombo.getValue();
            AddressModel address = addressCombo.getValue();
            LocalDate    date    = datePicker.getValue();
            if (client == null || address == null || date == null) {
                FxUtils.errorDialog(stage, "All fields are required.");
                return;
            }
            saveBtn.setDisable(true);
            String token = MainApp.getAuthToken();
            FxUtils.async(() -> {
                try {
                    OccurrenceModel created = api.createOccurrence(client.id(), address.id(), date, token);
                    for (File f : new ArrayList<>(selectedPhotos)) api.uploadPhoto(created.id(), f, token);
                    Platform.runLater(() -> { stage.close(); showStatus("Occurrence created.", true); loadData(); });
                } catch (Exception ex) {
                    Platform.runLater(() -> { saveBtn.setDisable(false); FxUtils.errorDialog(stage, ex.getMessage()); });
                }
            });
        });
        stage.showAndWait();
    }

    // ── Detail window ─────────────────────────────────────────────────────────

    private void openDetailWindow(OccurrenceModel m) {
        String dateStr = m.occurrenceDate() != null ? DATE_FMT.format(m.occurrenceDate()) : "N/A";

        Label statusBadge = new Label(m.status());
        statusBadge.getStyleClass().add(
                "FINISHED".equals(m.status()) ? "status-badge-finished" : "status-badge-active");
        HBox badgeRow = new HBox(statusBadge);
        badgeRow.setPadding(new Insets(6, 0, 0, 0));

        FlowPane photosPane = buildPhotosPane(m.photos());
        Button addPhotoBtn  = new Button("+ Add Photo");
        addPhotoBtn.getStyleClass().add("btn-add-photo");

        VBox actionsContent = buildActionsContent(m);

        WindowBuilder builder = WindowBuilder.create(table.getScene().getWindow(),
                        "Occurrence — " + dateStr)
                .header("Occurrence — " + dateStr, badgeRow, "ID: " + m.id())
                .size(540, 700)
                .addCard(clientInfoCard(m))
                .addCard(addressInfoCard(m))
                .addCard(FxUtils.card("PHOTOS", new VBox(10, photosPane, addPhotoBtn)))
                .addCard(FxUtils.recordInfoCard(m.creationDate(), m.updateDate(), m.deleted()))
                .addCard(FxUtils.card("ACTIONS", actionsContent));

        Stage stage = builder.build();

        addPhotoBtn.setOnAction(e -> {
            File file = photoChooser("Select Photo").showOpenDialog(stage);
            if (file != null) {
                addPhotoBtn.setDisable(true);
                String token = MainApp.getAuthToken();
                FxUtils.async(() -> {
                    try {
                        api.uploadPhoto(m.id(), file, token);
                        var updated = api.listPhotosForOccurrence(m.id(), token);
                        Platform.runLater(() -> {
                            refreshPhotosPane(photosPane, updated);
                            addPhotoBtn.setDisable(false);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> { FxUtils.errorDialog(stage, ex.getMessage()); addPhotoBtn.setDisable(false); });
                    }
                });
            }
        });

        stage.showAndWait();
    }

    private VBox buildActionsContent(OccurrenceModel m) {
        VBox content = new VBox(10);
        if (!"FINISHED".equals(m.status())) {
            Button closeBtn = FxUtils.warningButton("Close Occurrence");
            closeBtn.setOnAction(e -> {
                // stage not available yet — wire lazily after stage is built
                // We get the stage from the button's scene
                Stage owner = (Stage) closeBtn.getScene().getWindow();
                if (FxUtils.confirm(owner, "Mark this occurrence as FINISHED?")) {
                    FxUtils.async(() -> {
                        try {
                            api.closeOccurrence(m.id(), MainApp.getAuthToken());
                            Platform.runLater(() -> { owner.close(); showStatus("Occurrence closed.", true); loadData(); });
                        } catch (Exception ex) {
                            Platform.runLater(() -> FxUtils.errorDialog(owner, ex.getMessage()));
                        }
                    });
                }
            });
            content.getChildren().add(closeBtn);
        }
        Label hint = FxUtils.placeholderLabel("Double-click a row to view details.");
        content.getChildren().add(hint);
        return content;
    }

    // ── Photos helpers ────────────────────────────────────────────────────────

    private FlowPane emptyPhotosPane() {
        FlowPane pane = new FlowPane(8, 8);
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.getChildren().add(FxUtils.placeholderLabel("No photos selected."));
        return pane;
    }

    private FlowPane buildPhotosPane(List<PhotoModel> photos) {
        FlowPane pane = new FlowPane(8, 8);
        pane.setAlignment(Pos.CENTER_LEFT);
        refreshPhotosPane(pane, photos);
        return pane;
    }

    private void refreshPhotosPane(FlowPane pane, List<PhotoModel> photos) {
        pane.getChildren().clear();
        if (photos == null || photos.isEmpty()) {
            pane.getChildren().add(FxUtils.placeholderLabel("No photos attached."));
        } else {
            for (PhotoModel p : photos) {
                String path = p.pathBucket();
                String name = path != null ? path.substring(path.lastIndexOf('/') + 1) : p.id();
                pane.getChildren().add(FxUtils.photoChip(name));
            }
        }
    }

    // ── Info cards ────────────────────────────────────────────────────────────

    private VBox clientInfoCard(OccurrenceModel m) {
        GridPane grid = new GridPane();
        grid.setHgap(32);
        grid.setVgap(6);
        FxUtils.infoRow(grid, "Name",  m.clientName(), 0);
        FxUtils.infoRow(grid, "CPF",   m.clientCpf(), 1);
        FxUtils.infoRow(grid, "Born",
                m.clientBirthdate() != null ? DATE_FMT.format(m.clientBirthdate()) : null, 2);
        return FxUtils.card("CLIENT", grid);
    }

    private VBox addressInfoCard(OccurrenceModel m) {
        GridPane grid = new GridPane();
        grid.setHgap(32);
        grid.setVgap(6);
        FxUtils.infoRow(grid, "Street",       m.addressStreetName(), 0);
        FxUtils.infoRow(grid, "Neighborhood", m.addressNeighborhood(), 1);
        FxUtils.infoRow(grid, "ZIP",          m.addressZipCode(), 2);
        FxUtils.infoRow(grid, "City",         m.addressCity(), 3);
        FxUtils.infoRow(grid, "State",        m.addressState(), 4);
        return FxUtils.card("ADDRESS", grid);
    }

    // ── Combo helpers ─────────────────────────────────────────────────────────

    private static ComboBox<ClientModel> clientComboBox(List<ClientModel> items) {
        ComboBox<ClientModel> combo = new ComboBox<>();
        combo.getItems().setAll(items);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPromptText("Select client");
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(ClientModel m) { return m != null ? m.name() + " (" + m.cpf() + ")" : ""; }
            @Override public ClientModel fromString(String s) { return null; }
        });
        return combo;
    }

    private static ComboBox<AddressModel> addressComboBox(List<AddressModel> items) {
        ComboBox<AddressModel> combo = new ComboBox<>();
        combo.getItems().setAll(items);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPromptText("Select address");
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(AddressModel m) { return m != null ? m.streetName() + ", " + m.city() : ""; }
            @Override public AddressModel fromString(String s) { return null; }
        });
        return combo;
    }

    private static FileChooser photoChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        return chooser;
    }
}
