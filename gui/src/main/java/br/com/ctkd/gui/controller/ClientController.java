package br.com.ctkd.gui.controller;

import br.com.ctkd.gui.MainApp;
import br.com.ctkd.gui.model.ClientModel;
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
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class ClientController {

    @FXML private TableView<ClientModel> table;
    @FXML private TableColumn<ClientModel, String> nameCol;
    @FXML private TableColumn<ClientModel, String> cpfCol;
    @FXML private TableColumn<ClientModel, String> birthdateCol;
    @FXML private TableColumn<ClientModel, String> creationDateCol;
    @FXML private TableColumn<ClientModel, String> updateDateCol;
    @FXML private TableColumn<ClientModel, String> deletedCol;
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
                ClientModel sel = table.getSelectionModel().getSelectedItem();
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
        add.setTitle("New Client");
        add.setResizable(false);

        Label titleLbl = new Label("New Client");
        titleLbl.getStyleClass().add("detail-title-lbl");
        Label subtitleLbl = new Label("Fill in all fields below");
        subtitleLbl.getStyleClass().add("detail-subtitle-lbl");
        VBox header = new VBox(4, titleLbl, subtitleLbl);
        header.getStyleClass().add("detail-header");

        TextField nameField = new TextField();
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setPromptText("Full name");
        TextField cpfField = new TextField();
        cpfField.setMaxWidth(Double.MAX_VALUE);
        cpfField.setPromptText("000.000.000-00");
        DatePicker birthdatePicker = new DatePicker();
        birthdatePicker.setPromptText("dd/MM/yyyy");
        birthdatePicker.setConverter(makeDateConverter());
        setupCpfFormatterOn(cpfField);

        Label nameLbl = new Label("Full Name");
        nameLbl.getStyleClass().add("field-label");
        Label cpfLbl = new Label("CPF");
        cpfLbl.getStyleClass().add("field-label");
        Label birthLbl = new Label("Birthdate");
        birthLbl.getStyleClass().add("field-label");

        VBox nameGrp = new VBox(4, nameLbl, nameField);
        VBox cpfGrp = new VBox(4, cpfLbl, cpfField);
        VBox birthGrp = new VBox(4, birthLbl, birthdatePicker);
        HBox.setHgrow(nameGrp, Priority.ALWAYS);
        HBox.setHgrow(cpfGrp, Priority.ALWAYS);
        HBox fieldsRow = new HBox(12, nameGrp, cpfGrp, birthGrp);

        VBox formCard = buildCard("CLIENT INFORMATION", fieldsRow);

        Button saveBtn = new Button("Create Client");
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

        Scene scene = new Scene(root, 560, 360);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        add.setScene(scene);

        cancelBtn.setOnAction(e -> add.close());

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            LocalDate bd = birthdatePicker.getValue();
            String cpf = cpfField.getText().trim();
            if (name.isEmpty() || bd == null || cpf.isEmpty()) {
                showDetailError(add, "All fields are required.");
                return;
            }
            String token = MainApp.getAuthToken();
            saveBtn.setDisable(true);
            bg(() -> {
                try {
                    api.createClient(name, bd, cpf, token);
                    Platform.runLater(() -> {
                        add.close();
                        showStatus("Client created.", true);
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

    private void openDetailWindow(ClientModel m) {
        Stage detail = new Stage();
        detail.initOwner(table.getScene().getWindow());
        detail.initModality(Modality.APPLICATION_MODAL);
        detail.setTitle("Client — " + m.name());
        detail.setResizable(false);

        Label titleLbl = new Label(m.name());
        titleLbl.getStyleClass().add("detail-title-lbl");
        Label subtitleLbl = new Label("ID: " + m.id());
        subtitleLbl.getStyleClass().add("detail-subtitle-lbl");
        VBox header = new VBox(4, titleLbl, subtitleLbl);
        header.getStyleClass().add("detail-header");

        TextField editName = new TextField(m.name());
        editName.setMaxWidth(Double.MAX_VALUE);
        TextField editCpf = new TextField(m.cpf());
        editCpf.setMaxWidth(Double.MAX_VALUE);
        DatePicker editBirth = new DatePicker(m.birthdate());
        editBirth.setConverter(makeDateConverter());
        setupCpfFormatterOn(editCpf);

        Label nameLbl = new Label("Full Name");
        nameLbl.getStyleClass().add("field-label");
        Label cpfLbl = new Label("CPF");
        cpfLbl.getStyleClass().add("field-label");
        Label birthLbl = new Label("Birthdate");
        birthLbl.getStyleClass().add("field-label");

        VBox nameGroup = new VBox(4, nameLbl, editName);
        VBox cpfGroup = new VBox(4, cpfLbl, editCpf);
        VBox birthGroup = new VBox(4, birthLbl, editBirth);
        HBox.setHgrow(nameGroup, Priority.ALWAYS);
        HBox.setHgrow(cpfGroup, Priority.ALWAYS);
        HBox editRow = new HBox(12, nameGroup, cpfGroup, birthGroup);

        VBox editCard = buildCard("EDIT INFORMATION", editRow);

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
        Button deleteBtn = new Button("Delete Client");
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

        Scene scene = new Scene(root, 540, 520);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        detail.setScene(scene);

        saveBtn.setOnAction(e -> {
            String name = editName.getText().trim();
            LocalDate bd = editBirth.getValue();
            String cpf = editCpf.getText().trim();
            if (name.isEmpty() || bd == null || cpf.isEmpty()) {
                showDetailError(detail, "All fields are required.");
                return;
            }
            String token = MainApp.getAuthToken();
            bg(() -> {
                try {
                    api.updateClient(m.id(), name, bd, cpf, token);
                    Platform.runLater(() -> {
                        detail.close();
                        showStatus("Client updated.", true);
                        loadData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showDetailError(detail, ex.getMessage()));
                }
            });
        });

        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete \"" + m.name() + "\"? This cannot be undone.",
                    ButtonType.YES, ButtonType.NO);
            confirm.initOwner(detail);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    bg(() -> {
                        try {
                            api.deleteClient(m.id(), MainApp.getAuthToken());
                            Platform.runLater(() -> {
                                detail.close();
                                showStatus("Client deleted.", true);
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
                var list = api.listClients(MainApp.getAuthToken());
                Platform.runLater(() -> table.getItems().setAll(list));
            } catch (Exception e) {
                Platform.runLater(() -> showStatus(e.getMessage(), false));
            }
        });
    }

    private void setupColumns() {
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        cpfCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().cpf()));
        birthdateCol.setCellValueFactory(d -> {
            LocalDate bd = d.getValue().birthdate();
            return new SimpleStringProperty(bd != null ? DATE_FMT.format(bd) : "");
        });
        creationDateCol.setCellValueFactory(d -> {
            OffsetDateTime dt = d.getValue().creationDate();
            return new SimpleStringProperty(dt != null ? DATETIME_FMT.format(dt) : "");
        });
        updateDateCol.setCellValueFactory(d -> {
            OffsetDateTime dt = d.getValue().updateDate();
            return new SimpleStringProperty(dt != null ? DATETIME_FMT.format(dt) : "");
        });
        deletedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().deleted() ? "Yes" : "No"));
    }

    private void setupCpfFormatterOn(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 11) digits = digits.substring(0, 11);
            String fmt = formatCpf(digits);
            if (!newVal.equals(fmt)) {
                field.setText(fmt);
                field.positionCaret(fmt.length());
            }
        });
    }

    private StringConverter<LocalDate> makeDateConverter() {
        return new StringConverter<>() {
            @Override public String toString(LocalDate d) { return d != null ? DATE_FMT.format(d) : ""; }
            @Override public LocalDate fromString(String s) {
                try { return s != null && !s.isBlank() ? LocalDate.parse(s, DATE_FMT) : null; }
                catch (Exception e) { return null; }
            }
        };
    }

    private void showStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add(ok ? "success-label" : "error-label");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private String formatCpf(String d) {
        if (d.length() <= 3) return d;
        if (d.length() <= 6) return d.substring(0, 3) + "." + d.substring(3);
        if (d.length() <= 9) return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6);
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    private void bg(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }
}
