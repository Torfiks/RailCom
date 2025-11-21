package ru.railcom.desktop.ui;

import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.railcom.desktop.module.BaseStation;
import ru.railcom.desktop.ui.component.TitleBarComponent;
import ru.railcom.desktop.ui.control.ControlPanelController;
import ru.railcom.desktop.ui.control.SimulationController;
import ru.railcom.desktop.ui.simulation.Simulation2DController;
import ru.railcom.desktop.ui.simulation.Simulation3DController;

import java.io.IOException;
import java.util.List;

public class MainScreen {

    private Stage stage;
    private SimulationController simulationController;

    public MainScreen() {}

    public Scene createScene(Stage stage) {
        this.stage = stage;
        this.simulationController = SimulationController.builder()
                .isRunning(false)
                .track(100)
                .currentSpeed(80)
                .countBaseStations(5)
                .trainPosition(0)
                .build();

        BorderPane root = new BorderPane();

        // Заголовок
        try {
            BorderPane titleBar = createTitleBar(stage);
            root.setTop(titleBar);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Основной контент
        VBox content = createContext();
        root.setCenter(content);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/control_panel.css").toExternalForm());

        return scene;
    }


    public BorderPane createTitleBar(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/title_bar.fxml"));

        BorderPane titleBar = loader.load();

        TitleBarComponent controller = loader.getController();
        controller.setup(stage);

        return titleBar;
    }

    public VBox createContext() {
        VBox mainLayout = new VBox(15);
        mainLayout.setStyle("-fx-background-color: #121212; -fx-padding: 15;");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/control_panel.fxml"));

            loader.setControllerFactory(clazz -> {
                if (clazz == ControlPanelController.class) {
                    ControlPanelController controller = new ControlPanelController();
                    controller.setup(simulationController);
                    return controller;
                }
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            mainLayout.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/simulation_2d.fxml"));
            loader.setControllerFactory(clazz -> {
                if (clazz == Simulation2DController.class) {
                    Simulation2DController controller = new Simulation2DController();
                    controller.setSimulationController(simulationController); // Передаем контроллер
                    return controller;
                }
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            mainLayout.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/simulation_3d.fxml"));
            loader.setControllerFactory(clazz -> {
                if (clazz == Simulation3DController.class) {
                    Simulation3DController controller = new Simulation3DController();
                    controller.setSimulationController(simulationController); // Передаем контроллер
                    return controller;
                }
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            mainLayout.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Блок графика мощности сигнала
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signal_chart.fxml"));
//            mainLayout.getChildren().add(loader.load());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return mainLayout;
    }

}
