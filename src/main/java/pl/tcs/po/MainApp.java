package pl.tcs.po;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import pl.tcs.po.controllers.MainMenuController;
import pl.tcs.po.controllers.MainWindowController;
import pl.tcs.po.models.Board;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    public static MainApp instance;

    Stage mainStage;
    boolean hasBoardToLoad = false;
    Board loadedBoard = null;

    @Override
    public void start(Stage stage) throws IOException {

        instance = this;
        Parent root = getMainMenuWindow();
        mainStage = stage;

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);


        stage.setTitle("Traffic Sandbox");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private Parent getMainWindow() throws IOException {
        var url = MainWindowController.class.getClassLoader().getResource("MainWindow.fxml");
        return FXMLLoader.load(Objects.requireNonNull(url));
    }

    private Parent getMainWindowPannableAndZoomable() throws IOException {
        var url = MainWindowController.class.getClassLoader().getResource("MainWindowZoomableAndPannable.fxml");
        return FXMLLoader.load(Objects.requireNonNull(url));
    }

    private Parent getMainMenuWindow() throws IOException {
        var url = MainMenuController.class.getClassLoader().getResource("MainMenu.fxml");
        return FXMLLoader.load(Objects.requireNonNull(url));
    }

    public void runNewSandbox() throws IOException {
        Parent root = getMainWindowPannableAndZoomable();
        Scene scene = new Scene(root);
        mainStage.setScene(scene);
    }

    public void returnToMainMenu() throws IOException {
        Parent root = getMainMenuWindow();
        Scene scene = new Scene(root);
        mainStage.setScene(scene);
    }

    public Window getMainStage() {
        return mainStage;
    }

    public void runNewSandbox(Board board) throws IOException {
        // TODO: finish implementation
        hasBoardToLoad = true;
        loadedBoard = board;
        runNewSandbox();
    }

    public boolean hasBoardToLoad() {
        if(hasBoardToLoad) {
            hasBoardToLoad = false;
            return true;
        }
        return false;
    }

    public Board getLoadedBoard() {
        return loadedBoard;
    }
}
