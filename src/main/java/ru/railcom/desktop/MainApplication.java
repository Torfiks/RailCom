package ru.railcom.desktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static ru.railcom.desktop.string.NameField.NameApp;
import static ru.railcom.desktop.ui.MainApplicationUI.*;
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
        // Создаем основной контроллер
        SimulatorController controller = new SimulatorController();

        // Создаем кастомный заголовок
        BorderPane titleBar = createTitleBar(primaryStage);
        // Оборачиваем UI в ScrollPane для прокрутки всего окна
        ScrollPane scrollPane = createScrollPane(controller);

        // Основной макет с кастомным заголовком
        VBox rootLayout = new VBox();
        rootLayout.getChildren().addAll(titleBar, scrollPane);
        rootLayout.setStyle("-fx-background-color: #121212;");
        rootLayout.setPrefSize(1200, 800);

        // Устанавливаем стиль окна без стандартной окантовки
        primaryStage.initStyle(StageStyle.UNDECORATED);

        Scene scene = new Scene(rootLayout, 1200, 800);

        // Добавляем обработку перетаскивания окна
        setupDragAndDrop(primaryStage, scene);

        // Устанавливаем иконку приложения
        setupApplicationIcon(primaryStage);

        primaryStage.setTitle(NameApp);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}