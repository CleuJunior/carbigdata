package br.com.ctkd.gui.ui;

import br.com.ctkd.gui.MainApp;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for the standard modal detail/form windows used across the application.
 * All modal windows share the same structure: dark header + scrollable card body.
 *
 * Usage:
 * <pre>
 *   Stage stage = WindowBuilder.create(owner, "Title")
 *       .header("Display Title", "Subtitle text")
 *       .size(560, 400)
 *       .addCard(FxUtils.card("SECTION", content))
 *       .build();
 *   stage.showAndWait();
 * </pre>
 */
public final class WindowBuilder {

    private final Stage stage;
    private VBox header;
    private final List<VBox> cards = new ArrayList<>();
    private double width = 540;
    private double height = 500;

    private WindowBuilder(Window owner, String title) {
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setResizable(false);
    }

    public static WindowBuilder create(Window owner, String title) {
        return new WindowBuilder(owner, title);
    }

    public WindowBuilder header(String title, String subtitle) {
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("detail-title-lbl");
        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.getStyleClass().add("detail-subtitle-lbl");
        header = new VBox(4, titleLbl, subtitleLbl);
        header.getStyleClass().add("detail-header");
        return this;
    }

    /** Variant that inserts an extra node (e.g. a status badge row) between title and subtitle. */
    public WindowBuilder header(String title, Node extra, String subtitle) {
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("detail-title-lbl");
        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.getStyleClass().add("detail-subtitle-lbl");
        header = new VBox(4, titleLbl, extra, subtitleLbl);
        header.getStyleClass().add("detail-header");
        return this;
    }

    public WindowBuilder size(double width, double height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public WindowBuilder addCard(VBox card) {
        cards.add(card);
        return this;
    }

    public Stage build() {
        VBox body = new VBox(16);
        body.getChildren().addAll(cards);
        body.setPadding(new Insets(20));
        body.setStyle("-fx-background-color: #f0f4f8;");

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f0f4f8;");

        VBox root = new VBox(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(MainApp.stylesheet());
        stage.setScene(scene);
        return stage;
    }
}
