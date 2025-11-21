package ru.railcom.desktop.ui.simulation;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.util.Duration;
import lombok.Setter;
import ru.railcom.desktop.module.BaseStation;
import ru.railcom.desktop.ui.control.EventBus;
import ru.railcom.desktop.ui.control.SimulationController;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class Simulation2DController implements Initializable {

    @FXML
    private Pane simulation2DContainer;
    @FXML
    private Canvas canvas;

    @Setter
    private SimulationController simulationController;

    private static final int DESIRED_WIDTH = 50;
    private static final int DESIRED_HEIGHT = 30;
    private static final int BASE_STATION_WIDTH = 30;
    private static final int BASE_STATION_HEIGHT = 30;
    private static final int TRAIN_WIDTH = 50;
    private static final int TRAIN_HEIGHT = 30;

    private AnimationTimer timer;

    private Image trainImage;
    private Image baseStationImage;

    private double totalTrackLength;
    private double trainPosition;
    // Используем отдельный список для отображения, чтобы не изменять оригинальные данные при перетаскивании
    private List<BaseStation> displayedStations;

    private boolean draggingTrain = false;
    private BaseStation draggingStation = null;
    private double dragStartX = 0;
    private long lastUpdate = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        canvas.widthProperty().bind(simulation2DContainer.widthProperty());
        canvas.heightProperty().set(200);

        try {
            trainImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/train.png")));
            baseStationImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/base_station.png")));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить изображения: " + e.getMessage());
        }

        // Устанавливаем стили
        simulation2DContainer.getStylesheets().add(getClass().getResource("/css/simulation_2d.css").toExternalForm());

        // Подписываемся на события
        EventBus.getInstance().subscribe(this::onSimulationUpdate);
        // Добавляем обработчики событий
        addEventHandlers();
    }

    /**
     * Обработчик событий симуляции
     */
    private void onSimulationUpdate() {
        if (simulationController == null){
            return;
        }

        // Обновляем данные из контроллера
        totalTrackLength = simulationController.getTrack();
        trainPosition = simulationController.getTrainPosition();

        // Если станции не инициализированы — создаем их
        if (displayedStations == null) {
            displayedStations = new ArrayList<>();
            int count = simulationController.getCountBaseStations();
            createBaseStations(count);
        }

        // Синхронизируем станции с контроллером (если были перемещены в 3D)
        syncStationsFromController();

        // Запускаем/останавливаем таймер в зависимости от состояния
        updateTimer();
    }

    /**
     * Синхронизация станций с контроллера (для синхронизации с 3D)
     */
    private void syncStationsFromController() {
        List<BaseStation> controllerStations = simulationController.getBaseStations();
        if (controllerStations != null && controllerStations.size() == displayedStations.size()) {
            for (int i = 0; i < controllerStations.size(); i++) {
                BaseStation controllerStation = controllerStations.get(i);
                BaseStation displayedStation = displayedStations.get(i);

                // Обновляем позиции, если они изменились
                if (Math.abs(displayedStation.getPosition() - controllerStation.getPosition()) > 0.001) {
                    displayedStation.setPosition(controllerStation.getPosition());
                    // Пересчитываем X на основе новой позиции
                    if (totalTrackLength > 0) {
                        double scale = canvas.getWidth() / totalTrackLength;
                        displayedStation.setX((int) (controllerStation.getPosition() * scale));
                    }
                }
            }
        }
    }

    /**
     * Создание базовых станций
     */
    private void createBaseStations(int count) {
        if (count <= 0) return;

        displayedStations.clear();
        double spacing = totalTrackLength / (count - 1);

        for (int i = 0; i < count; i++) {
            double position = spacing * i;
            // Масштабируем позицию для отображения на Canvas (ширина 900px)
            int x = (int) ((position / totalTrackLength) * canvas.getWidth());
            displayedStations.add(new BaseStation(position, x, null));
        }
    }

    /**
     * Обновление таймера анимации
     */
    private void updateTimer() {
        if (simulationController.isRunning()) {
            if (timer == null) {
                startAnimation();
            }
        } else {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }
        draw(); // Обновляем отрисовку
    }

    /**
     * Запуск анимации
     */
    private void startAnimation() {
        lastUpdate = 0;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!simulationController.isRunning()) {
                    stopTimer();
                    return;
                }

                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                // Обновляем позицию поезда
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                double speedKmph = simulationController.getCurrentSpeed();
                trainPosition += (speedKmph / 3.6) * deltaTime;

                // Зацикливаем движение
                if (trainPosition > totalTrackLength) {
                    trainPosition = 0;
                }

                // Обновляем позицию в контроллере
                simulationController.setTrainPosition(trainPosition);

                lastUpdate = now;
                draw();
            }
        };
        timer.start();
    }

    /**
     * Остановка таймера
     */
    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    /**
     * Отрисовка сцены
     */
    public void draw() {
        if (canvas == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Путь (линия трассы)
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.strokeLine(0, 100, canvas.getWidth(), 100);

        // Базовые станции
        drawBaseStations(gc);

        // Поезд
        drawTrain(gc);
    }

    /**
     * Отрисовка поезда
     */
    private void drawTrain(GraphicsContext gc) {
        if (totalTrackLength <= 0 || canvas.getWidth() <= 0) return;

        // Масштабируем позицию поезда для отображения
        double scale = canvas.getWidth() / totalTrackLength;
        int trainX = (int) (trainPosition * scale);

        // Рисуем изображение поезда
        if (trainImage != null && trainImage.getProgress() == 1.0) {
            gc.drawImage(trainImage,
                    trainX - (double) DESIRED_WIDTH / 2,   // X
                    60,                           // Y
                    DESIRED_WIDTH,                // Ширина
                    DESIRED_HEIGHT);              // Высота
        } else {
            // Если нет картинки — рисуем красный кружок
            gc.setFill(Color.RED);
            gc.fillOval(trainX - 10, 70, 20, 20);
        }
    }

    /**
     * Отрисовка базовых станций
     */
    private void drawBaseStations(GraphicsContext gc) {
        if (displayedStations == null || canvas.getWidth() <= 0) return;

        for (BaseStation station : displayedStations) {
            int x = station.getX();

            // Рисуем линию с меткой расстояния над станцией
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeLine(x, 40, x, 60);

            // Рисуем метку расстояния
            gc.setFill(Color.WHITE);
            gc.fillText(String.format("%.0f", station.getPosition()), x - 15, 35);

            // Рисуем изображение станции
            if (baseStationImage != null && baseStationImage.getProgress() == 1.0) {
                gc.drawImage(baseStationImage,
                        x - (double) BASE_STATION_WIDTH / 2,  // X
                        85,                           // Y
                        BASE_STATION_WIDTH,           // Ширина
                        BASE_STATION_HEIGHT);         // Высота
            } else {
                // Резерв: рисуем синий кружок
                gc.setFill(Color.BLUE);
                gc.fillOval(x - 8, 92, 16, 16);
            }
        }
    }

    /**
     * Добавление обработчиков событий
     */
    private void addEventHandlers() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseDragged(this::handleMouseDragged);
    }

    private void handleMousePressed(MouseEvent e) {
        if (simulationController == null) return;

        double mouseX = e.getX();
        double mouseY = e.getY();

        // Проверяем, нажали ли на поезд
        if (totalTrackLength > 0) {
            double scale = canvas.getWidth() / totalTrackLength;
            int trainX = (int)(trainPosition * scale);
            if (Math.abs(mouseX - trainX) <= (double) TRAIN_WIDTH / 2 &&
                    Math.abs(mouseY - 70) <= (double) TRAIN_HEIGHT / 2) {
                draggingTrain = true;
            }
        }

        // Проверяем, нажали ли на станцию
        if (displayedStations != null) {
            for (BaseStation station : displayedStations) {
                int x = station.getX();
                if (Math.abs(mouseX - x) <= (double) BASE_STATION_WIDTH / 2 &&
                        Math.abs(mouseY - 85) <= (double) BASE_STATION_HEIGHT / 2) {
                    draggingStation = station;
                    dragStartX = mouseX - station.getX();
                    break;
                }
            }
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (draggingStation != null) {
            // Обновляем позицию в контроллере и синхронизируем с 3D
            syncStationToController(draggingStation);
        }
        draggingTrain = false;
        draggingStation = null;
    }

    private void handleMouseDragged(MouseEvent e) {
        if (simulationController == null) return;

        double mouseX = e.getX();
        double mouseY = e.getY();

        if (draggingTrain && totalTrackLength > 0) {
            // Исправленная формула для вычисления позиции поезда
            double scale = canvas.getWidth() / totalTrackLength;
            trainPosition = mouseX / scale; // Просто делим координату мыши на масштаб

            if (trainPosition < 0) trainPosition = 0;
            if (trainPosition > totalTrackLength) trainPosition = totalTrackLength;

            // Обновляем позицию в контроллере
            simulationController.setTrainPosition(trainPosition);
            draw();
        }

        if (draggingStation != null) {
            int newX = (int)(mouseX - dragStartX);
            // Ограничиваем перемещение по оси X
            newX = Math.max(0, Math.min((int)canvas.getWidth(), newX));
            draggingStation.setX(newX);

            // Обновляем позицию станции в километрах
            double newPosition = (newX * totalTrackLength) / canvas.getWidth();
            draggingStation.setPosition(newPosition);

            draw();
        }
    }

    /**
     * Синхронизация позиции станции с контроллером (для передачи в 3D)
     */
    private void syncStationToController(BaseStation station) {
        if (simulationController != null) {
            List<BaseStation> controllerStations = simulationController.getBaseStations();
            if (controllerStations != null) {
                // Найдем соответствующую станцию в контроллере
                for (BaseStation controllerStation : controllerStations) {
                    if (Math.abs(controllerStation.getPosition() - station.getPosition()) < 0.1) {
                        // Обновляем позицию в контроллере
                        controllerStation.setPosition(station.getPosition());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Очистка ресурсов
     */
    public void cleanup() {
        stopTimer();
    }
}
