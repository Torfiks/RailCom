package ru.railcom.desktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.railcom.desktop.ui.MainScreen;


/*
██████╗  █████╗ ██╗██╗      ██████╗ ██████╗ ███╗   ███╗
██╔══██╗██╔══██╗██║██║     ██╔════╝██╔═══██╗████╗ ████║
██████╔╝███████║██║██║     ██║     ██║   ██║██╔████╔██║
██╔══██╗██╔══██║██║██║     ██║     ██║   ██║██║╚██╔╝██║
██║  ██║██║  ██║██║███████╗╚██████╗╚██████╔╝██║ ╚═╝ ██║
╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝     ╚═╝
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UNDECORATED);

        MainScreen mainScreen = new MainScreen();
        Scene scene = mainScreen.createScene(primaryStage);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}