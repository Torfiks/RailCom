package ru.railcom.desktop.ui;

import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.railcom.desktop.ui.chart.BERChatController;
import ru.railcom.desktop.ui.chart.SignalChartController;
import ru.railcom.desktop.ui.component.TitleBarComponent;
import ru.railcom.desktop.ui.control.ControlPanelController;
import ru.railcom.desktop.ui.control.SimulationController;
import ru.railcom.desktop.ui.simulation.Simulation2DController;
import ru.railcom.desktop.ui.simulation.Simulation3DController;

import java.io.IOException;

public class MainScreen {

    private Stage stage;
    private TitleBarComponent titleBarComponent;
    private SimulationController simulationController;
    private SignalChartController signalChartController;

    public MainScreen() {}

    public Scene createScene(Stage stage) {
        this.stage = stage;
        this.simulationController = SimulationController.builder()
                .isRunning(false)
                .track(100)
                .currentSpeed(80)
                .countBaseStations(5)
                .trainPosition(0)
                .typeArea("urban")
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
        ScrollPane scrollPane = createScrollPane(content);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/control_panel.css").toExternalForm());

        return scene;
    }



    public BorderPane createTitleBar(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/title_bar.fxml"));

        BorderPane titleBar = loader.load();

        titleBarComponent = loader.getController();
        titleBarComponent.setup(stage);

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
                    titleBarComponent.setControlPanelController(controller);
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

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signal_chart.fxml"));

            // Установка фабрики контроллера для SignalChartController
            loader.setControllerFactory(clazz -> {
                if (clazz == SignalChartController.class) {
                    signalChartController = new SignalChartController();
                    signalChartController.setSimulationController(simulationController);
                    return signalChartController;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ber_chart.fxml"));

            // Установка фабрики контроллера для SignalChartController
            loader.setControllerFactory(clazz -> {
                if (clazz == BERChatController.class) {
//                    BERChatController controller = new BERChatController();
//                    controller.setSignalChartController(signalChartController);
//                    return controller;
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


        return mainLayout;
    }

    public static ScrollPane createScrollPane(VBox content) {

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #121212; -fx-background-color: #121212;");

        return scrollPane;
    }

}
