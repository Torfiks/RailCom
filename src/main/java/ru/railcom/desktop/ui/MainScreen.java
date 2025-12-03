package ru.railcom.desktop.ui;

import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.railcom.desktop.modulation.ModulateController;
import ru.railcom.desktop.modulation.Modulation;
import ru.railcom.desktop.ui.chart.BERChartController;
import ru.railcom.desktop.ui.chart.ShapeSignalChartController;
import ru.railcom.desktop.ui.chart.SignalChartController;
import ru.railcom.desktop.ui.component.TitleBarComponent;
import ru.railcom.desktop.ui.control.ControlPanelController;
import ru.railcom.desktop.simulation.SimulationController;
import ru.railcom.desktop.ui.modulation.Modulation2DController;
import ru.railcom.desktop.ui.modulation.Modulation3DController;
import ru.railcom.desktop.ui.setting.SettingComponent;
import ru.railcom.desktop.ui.simulation.Simulation2DController;
import ru.railcom.desktop.ui.simulation.Simulation3DController;

import java.io.IOException;

public class MainScreen {

    private TitleBarComponent titleBarComponent;
    private SimulationController simulationController;
    private ModulateController modulateController;

    public MainScreen() {}

    public Scene createScene(Stage stage) {

        this.simulationController = SimulationController.builder()
                .isRunning(false)
                .track(100)
                .currentSpeed(80)
                .countBaseStations(5)
                .trainPosition(0)
                .typeArea("urban")
                .build();

        this.modulateController = ModulateController.builder()
                .modulation(Modulation.PSK)
                .countPoints(4)
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_content.fxml"));

            loader.setControllerFactory(clazz -> {
                // Control Panel
                if (clazz == ControlPanelController.class) {
                    ControlPanelController controller = new ControlPanelController();
                    controller.setSimulationController(simulationController);
                    titleBarComponent.setControlPanelController(controller);
                    return controller;
                }
                // 2D Simulation
                if (clazz == Simulation2DController.class) {
                    Simulation2DController controller = new Simulation2DController();
                    controller.setSimulationController(simulationController);
                    return controller;
                }
                // 3D Simulation
                if (clazz == Simulation3DController.class) {
                    Simulation3DController controller = new Simulation3DController();
                    controller.setSimulationController(simulationController);
                    return controller;
                }
                // Signal Chart
                if (clazz == SignalChartController.class) {
                    SignalChartController signalChartController = new SignalChartController();
                    signalChartController.setSimulationController(simulationController);
                    return signalChartController;
                }
                // Modulation 3D
                if (clazz == Modulation3DController.class) {
                    Modulation3DController controller = new Modulation3DController();
                    controller.setModulateController(modulateController);
                    return controller;
                }
                // Modulation 2D
                if (clazz == Modulation2DController.class) {
                    Modulation2DController controller = new Modulation2DController();
                    controller.setModulateController(modulateController);
                    return controller;
                }
                // Setting Component
                if (clazz == SettingComponent.class) {
                    SettingComponent controller = new SettingComponent();
                    controller.setModulateController(modulateController);
                    return controller;
                }
                // BER Chart
                if (clazz == BERChartController.class) {
                    BERChartController controller = new BERChartController();
                    controller.setSimulationController(simulationController);
                    controller.setModulateController(modulateController);
                    return controller;
                }
                // Shape Signal
                if (clazz == ShapeSignalChartController.class) {
                    ShapeSignalChartController controller = new ShapeSignalChartController();
                    controller.setModulateController(modulateController);
                    return controller;
                }

                // Fallback для других контроллеров (если появятся)
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Cannot instantiate controller: " + clazz.getName(), e);
                }
            });

            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load main_content.fxml", e);
        }
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
