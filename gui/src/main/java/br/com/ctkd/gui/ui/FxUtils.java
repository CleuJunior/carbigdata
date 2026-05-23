package br.com.ctkd.gui.ui;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class FxUtils {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final StringConverter<java.time.LocalDate> DATE_CV = DateConverters.forPattern("dd/MM/yyyy");

    private FxUtils() {}

    // ── Async ─────────────────────────────────────────────────────────────────

    public static void async(Runnable task) {
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    // ── Layout builders ───────────────────────────────────────────────────────

    public static VBox card(String title, Node content) {
        Label lbl = new Label(title);
        lbl.getStyleClass().add("detail-section-lbl");
        Separator sep = new Separator();
        VBox card = new VBox(8, lbl, sep, content);
        card.getStyleClass().add("detail-card");
        return card;
    }

    public static VBox fieldGroup(String label, Control field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        return new VBox(4, lbl, field);
    }

    /** HBox row where every group except the last gets ALWAYS hgrow. */
    public static HBox fieldsRow(VBox... groups) {
        HBox row = new HBox(12);
        row.getChildren().addAll(groups);
        for (int i = 0; i < groups.length - 1; i++) {
            HBox.setHgrow(groups[i], Priority.ALWAYS);
        }
        return row;
    }

    public static void infoRow(GridPane grid, String label, String value, int row) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        Label val = new Label(nvl(value));
        val.getStyleClass().add("ro-value");
        val.setWrapText(true);
        grid.addRow(row, lbl, val);
    }

    /** Pre-built RECORD INFO card with Created / Updated / Deleted rows. */
    public static VBox recordInfoCard(OffsetDateTime created, OffsetDateTime updated, boolean deleted) {
        GridPane grid = new GridPane();
        grid.setHgap(32);
        grid.setVgap(6);
        infoRow(grid, "Created", created != null ? DT_FMT.format(created) : "—", 0);
        infoRow(grid, "Updated", updated != null ? DT_FMT.format(updated) : "—", 1);
        infoRow(grid, "Deleted", deleted ? "Yes" : "No", 2);
        return card("RECORD INFO", grid);
    }

    // ── Input controls ────────────────────────────────────────────────────────

    public static TextField styledTextField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    public static TextField styledTextField(String prompt, String value) {
        TextField f = styledTextField(prompt);
        f.setText(value);
        return f;
    }

    public static DatePicker styledDatePicker() {
        DatePicker dp = new DatePicker();
        dp.setPromptText("dd/MM/yyyy");
        dp.setConverter(DATE_CV);
        return dp;
    }

    public static DatePicker styledDatePicker(java.time.LocalDate value) {
        DatePicker dp = new DatePicker(value);
        dp.setConverter(DATE_CV);
        return dp;
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    public static Button saveButton(String text) {
        return styledButton(text, "btn-save");
    }

    public static Button dangerButton(String text) {
        return styledButton(text, "btn-danger");
    }

    public static Button cancelButton() {
        return styledButton("Cancel", "secondary-button");
    }

    public static Button warningButton(String text) {
        return styledButton(text, "btn-warning");
    }

    private static Button styledButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    public static Label photoChip(String name) {
        Label chip = new Label(name);
        chip.getStyleClass().add("photo-chip");
        return chip;
    }

    public static Label placeholderLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("placeholder-lbl");
        return lbl;
    }

    public static void errorDialog(Stage owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static boolean confirm(Stage owner, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        return alert.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    public static String nvl(String s) {
        return s != null ? s : "—";
    }
}
