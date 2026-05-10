package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import br.com.ctkd.gui.model.AddressModel;
import br.com.ctkd.gui.model.ClientModel;
import br.com.ctkd.gui.model.OccurrenceModel;
import br.com.ctkd.gui.model.PhotoModel;
import br.com.ctkd.gui.service.ApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OccurrenceController {

    @FXML private TableView<OccurrenceModel> table;
    @FXML private TableColumn<OccurrenceModel, String> dateCol;
    @FXML private TableColumn<OccurrenceModel, String> statusCol;
    @FXML private TableColumn<OccurrenceModel, String> clientCol;
    @FXML private TableColumn<OccurrenceModel, String> cityCol;
    @FXML private TableColumn<OccurrenceModel, String> creationDateCol;
    @FXML private TableColumn<OccurrenceModel, String> updateDateCol;
    @FXML private TableColumn<OccurrenceModel, String> deletedCol;
    @FXML private Label statusLabel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ApiService api = ApiService.getInstance();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setupColumns();
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
        bg(() -> {
            try {
                var clients = api.listClients(MainApp.getAuthToken());
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

    @FXML
    public void handleRefresh() {
        loadData();
    }

    // ── Add New window ────────────────────────────────────────────────────────

    private void openAddNewWindow(List<ClientModel> clients, List<AddressModel> addresses) {
        Stage add = new Stage();
        add.initOwner(table.getScene().getWindow());
        add.initModality(Modality.APPLICATION_MODAL);
        add.setTitle("New Occurrence");
        add.setResizable(false);

        Label titleLbl = new Label("New Occurrence");
        titleLbl.getStyleClass().add("detail-title-lbl");
        Label subtitleLbl = new Label("Fill in all fields below");
        subtitleLbl.getStyleClass().add("detail-subtitle-lbl");
        VBox header = new VBox(4, titleLbl, subtitleLbl);
        header.getStyleClass().add("detail-header");

        // Combos and date picker
        ComboBox<ClientModel> clientCombo = new ComboBox<>();
        clientCombo.getItems().setAll(clients);
        clientCombo.setConverter(new StringConverter<>() {
            @Override public String toString(ClientModel m) { return m != null ? m.name() + " (" + m.cpf() + ")" : ""; }
            @Override public ClientModel fromString(String s) { return null; }
        });
        clientCombo.setMaxWidth(Double.MAX_VALUE);
        clientCombo.setPromptText("Select client");

        ComboBox<AddressModel> addressCombo = new ComboBox<>();
        addressCombo.getItems().setAll(addresses);
        addressCombo.setConverter(new StringConverter<>() {
            @Override public String toString(AddressModel m) { return m != null ? m.streetName() + ", " + m.city() : ""; }
            @Override public AddressModel fromString(String s) { return null; }
        });
        addressCombo.setMaxWidth(Double.MAX_VALUE);
        addressCombo.setPromptText("Select address");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate d) { return d != null ? DATE_FMT.format(d) : ""; }
            @Override public LocalDate fromString(String s) {
                try { return s != null && !s.isBlank() ? LocalDate.parse(s, DATE_FMT) : null; }
                catch (Exception e) { return null; }
            }
        });

        Label clientLbl = new Label("Client");
        clientLbl.getStyleClass().add("field-label");
        Label addrLbl = new Label("Address");
        addrLbl.getStyleClass().add("field-label");
        Label dateLbl = new Label("Date");
        dateLbl.getStyleClass().add("field-label");

        VBox clientGrp = new VBox(4, clientLbl, clientCombo);
        VBox addrGrp = new VBox(4, addrLbl, addressCombo);
        VBox dateGrp = new VBox(4, dateLbl, datePicker);
        HBox.setHgrow(clientGrp, Priority.ALWAYS);
        HBox.setHgrow(addrGrp, Priority.ALWAYS);
        HBox fieldsRow = new HBox(12, clientGrp, addrGrp, dateGrp);

        VBox detailsCard = buildCard("OCCURRENCE DETAILS", fieldsRow);

        // Photos section
        List<File> selectedPhotos = new ArrayList<>();
        FlowPane photosFlow = new FlowPane(8, 8);
        photosFlow.setAlignment(Pos.CENTER_LEFT);
        Label noPhotosLbl = new Label("No photos selected.");
        noPhotosLbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 12px;");
        photosFlow.getChildren().add(noPhotosLbl);

        Button addPhotoBtn = new Button("+ Add Photos");
        addPhotoBtn.getStyleClass().add("btn-add-photo");
        addPhotoBtn.setOnAction(ev -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Photos");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
            List<File> files = chooser.showOpenMultipleDialog(add);
            if (files != null && !files.isEmpty()) {
                selectedPhotos.addAll(files);
                photosFlow.getChildren().clear();
                for (File f : selectedPhotos) {
                    Label chip = new Label(f.getName());
                    chip.getStyleClass().add("photo-chip");
                    photosFlow.getChildren().add(chip);
                }
            }
        });

        VBox photosCard = buildCard("PHOTOS", new VBox(10, photosFlow, addPhotoBtn));

        // Actions
        Button saveBtn = new Button("Create Occurrence");
        saveBtn.getStyleClass().add("btn-save");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        VBox actionsCard = buildCard("ACTIONS", new VBox(10, saveBtn, cancelBtn));

        VBox body = new VBox(16, detailsCard, photosCard, actionsCard);
        body.setPadding(new Insets(20));
        body.setStyle("-fx-background-color: #f0f4f8;");

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f0f4f8;");

        VBox root = new VBox(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 620, 520);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        add.setScene(scene);

        cancelBtn.setOnAction(e -> add.close());

        saveBtn.setOnAction(e -> {
            ClientModel client = clientCombo.getValue();
            AddressModel address = addressCombo.getValue();
            LocalDate date = datePicker.getValue();
            if (client == null || address == null || date == null) {
                showDetailError(add, "All fields are required.");
                return;
            }
            saveBtn.setDisable(true);
            String token = MainApp.getAuthToken();
            bg(() -> {
                try {
                    OccurrenceModel created = api.createOccurrence(client.id(), address.id(), date, token);
                    for (File f : new ArrayList<>(selectedPhotos)) {
                        api.uploadPhoto(created.id(), f, token);
                    }
                    Platform.runLater(() -> {
                        add.close();
                        showStatus("Occurrence created.", true);
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

    private void openDetailWindow(OccurrenceModel m) {
        Stage detail = new Stage();
        detail.initOwner(table.getScene().getWindow());
        detail.initModality(Modality.APPLICATION_MODAL);
        detail.setTitle("Occurrence — " + (m.occurrenceDate() != null ? DATE_FMT.format(m.occurrenceDate()) : "N/A"));
        detail.setResizable(false);

        String dateStr = m.occurrenceDate() != null ? DATE_FMT.format(m.occurrenceDate()) : "N/A";
        Label titleLbl = new Label("Occurrence — " + dateStr);
        titleLbl.getStyleClass().add("detail-title-lbl");

        Label statusBadge = new Label(m.status());
        statusBadge.getStyleClass().add("FINISHED".equals(m.status()) ? "status-badge-finished" : "status-badge-active");
        HBox badgeRow = new HBox(statusBadge);
        badgeRow.setPadding(new Insets(6, 0, 0, 0));

        Label subtitleLbl = new Label("ID: " + m.id());
        subtitleLbl.getStyleClass().add("detail-subtitle-lbl");

        VBox header = new VBox(4, titleLbl, badgeRow, subtitleLbl);
        header.getStyleClass().add("detail-header");

        // Client card
        GridPane clientGrid = new GridPane();
        clientGrid.setHgap(32);
        clientGrid.setVgap(6);
        addInfoRow(clientGrid, "Name", nvl(m.clientName()), 0);
        addInfoRow(clientGrid, "CPF", nvl(m.clientCpf()), 1);
        addInfoRow(clientGrid, "Born",
                m.clientBirthdate() != null ? DATE_FMT.format(m.clientBirthdate()) : "—", 2);
        VBox clientCard = buildCard("CLIENT", clientGrid);

        // Address card
        GridPane addrGrid = new GridPane();
        addrGrid.setHgap(32);
        addrGrid.setVgap(6);
        addInfoRow(addrGrid, "Street", nvl(m.addressStreetName()), 0);
        addInfoRow(addrGrid, "Neighborhood", nvl(m.addressNeighborhood()), 1);
        addInfoRow(addrGrid, "ZIP", nvl(m.addressZipCode()), 2);
        addInfoRow(addrGrid, "City", nvl(m.addressCity()), 3);
        addInfoRow(addrGrid, "State", nvl(m.addressState()), 4);
        VBox addrCard = buildCard("ADDRESS", addrGrid);

        // Photos card
        FlowPane photosPane = buildPhotosPane(m.photos());
        Button addPhotoBtn = new Button("+ Add Photo");
        addPhotoBtn.getStyleClass().add("btn-add-photo");
        VBox photosContent = new VBox(10, photosPane, addPhotoBtn);
        VBox photosCard = buildCard("PHOTOS", photosContent);

        addPhotoBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Photo");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
            File file = chooser.showOpenDialog(detail);
            if (file != null) {
                addPhotoBtn.setDisable(true);
                String token = MainApp.getAuthToken();
                bg(() -> {
                    try {
                        api.uploadPhoto(m.id(), file, token);
                        var updated = api.listPhotosForOccurrence(m.id(), token);
                        Platform.runLater(() -> {
                            refreshPhotosPane(photosPane, updated);
                            addPhotoBtn.setDisable(false);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            showDetailError(detail, ex.getMessage());
                            addPhotoBtn.setDisable(false);
                        });
                    }
                });
            }
        });

        // Record info card
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(32);
        infoGrid.setVgap(6);
        addInfoRow(infoGrid, "Created", m.creationDate() != null ? DATETIME_FMT.format(m.creationDate()) : "—", 0);
        addInfoRow(infoGrid, "Updated", m.updateDate() != null ? DATETIME_FMT.format(m.updateDate()) : "—", 1);
        addInfoRow(infoGrid, "Deleted", m.deleted() ? "Yes" : "No", 2);
        VBox infoCard = buildCard("RECORD INFO", infoGrid);

        // Actions card
        VBox actionsContent = new VBox(10);
        if (!"FINISHED".equals(m.status())) {
            Button closeBtn = new Button("Close Occurrence");
            closeBtn.getStyleClass().add("btn-warning");
            closeBtn.setMaxWidth(Double.MAX_VALUE);
            closeBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Mark this occurrence as FINISHED?", ButtonType.YES, ButtonType.NO);
                confirm.initOwner(detail);
                confirm.setHeaderText(null);
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.YES) {
                        bg(() -> {
                            try {
                                api.closeOccurrence(m.id(), MainApp.getAuthToken());
                                Platform.runLater(() -> {
                                    detail.close();
                                    showStatus("Occurrence closed.", true);
                                    loadData();
                                });
                            } catch (Exception ex) {
                                Platform.runLater(() -> showDetailError(detail, ex.getMessage()));
                            }
                        });
                    }
                });
            });
            actionsContent.getChildren().add(closeBtn);
        }

        Label hintLbl = new Label("Double-click a row to view details. Close this window to go back.");
        hintLbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;");
        hintLbl.setWrapText(true);
        actionsContent.getChildren().add(hintLbl);

        VBox actionsCard = buildCard("ACTIONS", actionsContent);

        VBox body = new VBox(16, clientCard, addrCard, photosCard, infoCard, actionsCard);
        body.setPadding(new Insets(20));
        body.setStyle("-fx-background-color: #f0f4f8;");

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f0f4f8;");

        VBox root = new VBox(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 540, 700);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        detail.setScene(scene);
        detail.showAndWait();
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
            Label empty = new Label("No photos attached.");
            empty.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 12px;");
            pane.getChildren().add(empty);
        } else {
            for (PhotoModel p : photos) {
                String path = p.pathBucket();
                String name = path != null ? path.substring(path.lastIndexOf('/') + 1) : p.id();
                Label chip = new Label(name);
                chip.getStyleClass().add("photo-chip");
                pane.getChildren().add(chip);
            }
        }
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
        val.setWrapText(true);
        grid.addRow(row, lbl, val);
    }

    private void showDetailError(Stage owner, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String nvl(String s) {
        return s != null ? s : "—";
    }

    private void loadData() {
        bg(() -> {
            try {
                var list = api.listOccurrences(MainApp.getAuthToken());
                Platform.runLater(() -> table.getItems().setAll(list));
            } catch (Exception e) {
                Platform.runLater(() -> showStatus(e.getMessage(), false));
            }
        });
    }

    private void setupColumns() {
        dateCol.setCellValueFactory(d -> {
            LocalDate dt = d.getValue().occurrenceDate();
            return new SimpleStringProperty(dt != null ? DATE_FMT.format(dt) : "");
        });
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        clientCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().clientName()));
        cityCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().addressCity()));
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
