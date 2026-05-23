package br.com.ctkd.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static String authToken;

    public static String getAuthToken() {
        return authToken;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("CarBigData – Client Manager");
        primaryStage.setResizable(true);
        showLoginScreen();
        primaryStage.show();
    }

    public static void showLoginScreen() throws Exception {
        authToken = null;
        primaryStage.setResizable(false);
        loadScene("login.fxml", 420, 460);
    }

    public static void showMainScreen(String token) throws Exception {
        authToken = token;
        primaryStage.setResizable(true);
        loadScene("main.fxml", 1050, 680);
        primaryStage.centerOnScreen();
    }

    private static void loadScene(String fxml, double w, double h) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxml));
        Parent root = loader.load();
        Scene scene = new Scene(root, w, h);
        scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static String stylesheet() {
        return MainApp.class.getResource("style.css").toExternalForm();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
y