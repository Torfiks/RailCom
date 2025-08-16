package ru.modulator.desktop.modulator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.jetbrains.annotations.NotNull;
import ru.modulator.desktop.modulator.module.SignalChartPane;

public class SimulatorController {

    private final R3D train3DScene = new R3D();
    private final SignalChartPane chartPane = new SignalChartPane();
    private final R2D road2DPane = new R2D();

    private final TextField baseStationsField = new TextField("5");
    private final TextField distanceField = new TextField("100");

    private final Label downlinkLabel = new Label("Downlink: 0 Mbps");
    private final Label uplinkLabel = new Label("Uplink: 0 Mbps");
    private final Label speedLabel = new Label("Скорость поезда: 300 км/ч");
    private final Label statusLabel = new Label("Статус: Готов к запуску");

    private Slider speedSlider;

    // Список ползунков для базовых станций
    private final ObservableList<Slider> stationSliders = FXCollections.observableArrayList();

    private Button toggleButton;
    private Button resetButton;


    public SimulatorController() {
        // Устанавливаем связь с контроллером
        train3DScene.setController(this);

        // Инициализация стилей для лейблов
        downlinkLabel.setStyle("-fx-text-fill: #4FC3F7; -fx-font-size: 16px;");
        uplinkLabel.setStyle("-fx-text-fill: #69F0AE; -fx-font-size: 16px;");
        speedLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #a5d6a7;");
    }

    public Node getUI() {
        // Позиционирование элементов
        road2DPane.getNode().setLayoutY(150);
        road2DPane.getNode().setLayoutX(20);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setStyle("-fx-background-color: #121212;");

        // Панель с 3D-сценой
        Pane train3DContainer = new Pane();
        train3DContainer.setPrefSize(1000, 350);
        train3DContainer.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 1);");
        train3DContainer.getChildren().add(train3DScene.getNode());

        // Панель управления
        GridPane controls = createControlPanel();

        // Правая панель с информацией
        VBox infoPanel = new VBox(15);
        infoPanel.setPadding(new Insets(20));
        infoPanel.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 1);");

        // Заголовок панели
        Label infoTitle = new Label("Информация о соединении");
        infoTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: linear-gradient(to right, #4FC3F7, #69F0AE); -fx-padding: 0 0 10 0;");

        // Индикаторы
        ProgressBar signalStrengthBar = new ProgressBar(0);
        signalStrengthBar.setPrefWidth(180);
        signalStrengthBar.setStyle("-fx-accent: #4FC3F7;");

        Label signalStrengthLabel = new Label("Сила сигнала: 0%");
        signalStrengthLabel.setStyle("-fx-text-fill: #e0e0e0;");

        ProgressBar downlinkBar = new ProgressBar(0);
        downlinkBar.setPrefWidth(180);
        downlinkBar.setStyle("-fx-accent: #FF5252;");

        Label downlinkInfoLabel = new Label("Downlink: 0 Mbps");
        downlinkInfoLabel.setStyle("-fx-text-fill: #e0e0e0;");

        ProgressBar uplinkBar = new ProgressBar(0);
        uplinkBar.setPrefWidth(180);
        uplinkBar.setStyle("-fx-accent: #69F0AE;");

        Label uplinkInfoLabel = new Label("Uplink: 0 Mbps");
        uplinkInfoLabel.setStyle("-fx-text-fill: #e0e0e0;");

        infoPanel.getChildren().addAll(
                infoTitle,
                new HBox(10, new Label("Сила сигнала:"), signalStrengthBar, signalStrengthLabel),
                new HBox(10, new Label("Downlink:"), downlinkBar, downlinkInfoLabel),
                new HBox(10, new Label("Uplink:"), uplinkBar, uplinkInfoLabel),
                statusLabel
        );

        // Графики
        Pane chartContainer = new Pane();
        chartContainer.setPrefSize(1000, 300);
        chartContainer.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 1);");

        chartContainer.getChildren().add(chartPane);

        // Настройка макета
        HBox contentBox = new HBox(15, train3DContainer, infoPanel);
        contentBox.setPadding(new Insets(10, 20, 20, 20));

        mainLayout.getChildren().addAll(controls, contentBox, chartContainer);

        // Привязка данных к индикаторам
//        train3DScene.signalProperty().addListener((obs, oldVal, newVal) -> {
//            double value = newVal.doubleValue();
//            signalStrengthBar.setProgress(value);
//            signalStrengthLabel.setText(String.format("Сила сигнала: %.0f%%", value * 100));
//        });

        chartPane.downlinkProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            downlinkBar.setProgress(value / 100.0);
            downlinkInfoLabel.setText(String.format("Downlink: %.2f Mbps", value));
        });

        chartPane.uplinkProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            uplinkBar.setProgress(value / 50.0);
            uplinkInfoLabel.setText(String.format("Uplink: %.2f Mbps", value));
        });


        road2DPane.getNode().setUserData(this); // <-- Критически важно!
        return mainLayout;
    }

    // Настройка верхней панели
    private GridPane createControlPanel() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(12);
        grid.setHgap(15);
        grid.setStyle("-fx-background-color: #1e1e1e; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 1);");

        Label bsLabel = new Label("Количество БС:");
        bsLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold;");
        Label distLabel = new Label("Расстояние между БС (км):");
        distLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold;");

        Button startButton = getStartButton();

        // Скорость поезда
        Label speedControlLabel = new Label("Скорость поезда:");
        speedControlLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold;");
        speedSlider = new Slider(0, 350, 300);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(50);
        speedSlider.setBlockIncrement(10);
        speedSlider.setStyle(
                "-fx-background-radius: 4; " +
                        "-fx-control-inner-background: #2d4059; " +
                        "-fx-border-color: #4a6580;"
        );
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedLabel.setText(String.format("Скорость поезда: %.0f км/ч", newVal.doubleValue()));
        });

        // Метка текущей скорости
        Label currentSpeedLabel = new Label();
        currentSpeedLabel.textProperty().bind(speedSlider.valueProperty().asString("%.0f км/ч"));
        currentSpeedLabel.setStyle("-fx-text-fill: #4FC3F7; -fx-font-size: 14px;");

        // Кнопки управления
        toggleButton = new Button("Остановить симуляцию");
        toggleButton.setStyle(
                "-fx-background-radius: 20; " +
                        "-fx-background-color: #2d4059; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 8 15;"
        );
        toggleButton.setOnMouseEntered(e ->
                toggleButton.setStyle(
                        "-fx-background-radius: 20; " +
                                "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 8 15;"
                )
        );
        toggleButton.setOnMouseExited(e ->
                toggleButton.setStyle(
                        "-fx-background-radius: 20; " +
                                "-fx-background-color: #2d4059; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 8 15;"
                )
        );
        toggleButton.setOnAction(e -> toggleSimulation());

        resetButton = new Button("Сбросить");
        resetButton.setStyle(
                "-fx-background-radius: 20; " +
                        "-fx-background-color: #2d4059; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 8 15;"
        );
        resetButton.setOnMouseEntered(e ->
                resetButton.setStyle(
                        "-fx-background-radius: 20; " +
                                "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 8 15;"
                )
        );
        resetButton.setOnMouseExited(e ->
                resetButton.setStyle(
                        "-fx-background-radius: 20; " +
                                "-fx-background-color: #2d4059; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 8 15;"
                )
        );
        resetButton.setOnAction(e -> resetSimulation());

        // Размещение элементов
        grid.add(bsLabel, 0, 0);
        grid.add(baseStationsField, 1, 0);
        grid.add(distLabel, 2, 0);
        grid.add(distanceField, 3, 0);
        grid.add(startButton, 4, 0);

        grid.add(speedControlLabel, 0, 1);
        grid.add(speedSlider, 1, 1, 3, 1);
        grid.add(currentSpeedLabel, 4, 1);

//        grid.add(road2DPane.getNode(),1,2,3,1);

        HBox buttonBox = new HBox(10, toggleButton, resetButton);
        grid.add(buttonBox, 4, 3);
        grid.add(speedLabel, 0, 3, 4, 1);
        grid.add(downlinkLabel, 0, 4, 5, 1);
        grid.add(uplinkLabel, 0, 5, 5, 1);

        // Добавляем подсказку для интерактивного управления
        Label instructions = new Label(
                "ЛКМ + перемещение: вращение камеры | Колесо мыши: приближение/удаление | " +
                        "ПКМ: добавить базовую станцию");
        instructions.setStyle("-fx-font-style: italic; -fx-text-fill: #80deea; -fx-font-size: 12px;");
        grid.add(instructions, 0, 6, 5, 1);

        return grid;
    }

    @NotNull
    private Button getStartButton() {
        Button startButton = new Button("Запустить симуляцию");
        startButton.setStyle(
                "-fx-background-radius: 20; " +
                        "-fx-background-color: #2d4059; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 8 15;"
        );
        startButton.setOnMouseEntered(e ->
                startButton.setStyle(
                        "-fx-background-radius: 20; " +
                                "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 8 15;"
                )
        );
        startButton.setOnMouseExited(e ->
                startButton.setStyle(
                        "-fx-background-radius: 20; " +
                                "-fx-background-color: #2d4059; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 8 15;"
                )
        );
        startButton.setOnAction(e -> startSimulation());
        return startButton;
    }


    public void resetSimulation() {
        train3DScene.resetSimulation();
        updateSlidersFromSimulation();
        statusLabel.setText("Статус: Симуляция сброшена");
    }

    public void toggleSimulation() {
        train3DScene.toggleAnimation();
        road2DPane.toggleAnimation();
        toggleButton.setText(train3DScene.isRunning() ?
                "Остановить симуляцию" : "Продолжить симуляцию");

        statusLabel.setText(train3DScene.isRunning() ?
                "Статус: Симуляция запущена" : "Статус: Симуляция приостановлена");
    }

    private void startSimulation() {
        try {
            int numStations = Integer.parseInt(baseStationsField.getText());
            double stationDistance = Double.parseDouble(distanceField.getText());

            if (numStations < 1) {
                throw new NumberFormatException("Количество БС должно быть > 0");
            }
            if (stationDistance <= 0) {
                throw new NumberFormatException("Расстояние должно быть > 0");
            }

            train3DScene.startSimulation(numStations, stationDistance);

            // Обновляем интерфейс управления станциями
            road2DPane.startSimulation(numStations, stationDistance);

            updateSlidersFromSimulation();

            toggleButton.setText("Остановить симуляцию");
            statusLabel.setText("Статус: Симуляция запущена");

        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Введите корректные числа.\n" + ex.getMessage());
            alert.setTitle("Ошибка ввода");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void updateSlidersFromSimulation() {

        // Обновляем позиции базовых станций
//        int numStations = train3DScene.getNumStations();
//        for (int i = 0; i < numStations && i < stationSliders.size(); i++) {
//            double stationPosition = train3DScene.getStationPosition(i);
//            stationSliders.get(i).setValue(stationPosition);
//        }

    }

    public void updateLinks(double signalStrength, double downlink, double uplink) {
        downlinkLabel.setText(String.format("Downlink: %.2f Mbps", downlink));
        uplinkLabel.setText(String.format("Uplink: %.2f Mbps", uplink));
        statusLabel.setText("Статус: Связь активна");

        Platform.runLater(() -> chartPane.update(signalStrength * 100, downlink, uplink));
    }

    public double getCurrentSpeed() {
        return speedSlider.getValue();
    }
}