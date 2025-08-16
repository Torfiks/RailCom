package ru.modulator.desktop.modulator;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import lombok.Data;
import lombok.Getter;
import ru.modulator.desktop.modulator.dto.BaseStation;
import ru.modulator.desktop.modulator.ui.module.Road2DPaneUi;

import java.util.ArrayList;
import java.util.List;

@Data
public class R2D {

    // Размер экрана
    private static final Integer WIDTH = 1000;
    private static final Integer HEIGHT = 400;

    private static final Canvas canvas = new Canvas(WIDTH, HEIGHT);
    private static final Group node = new Group(canvas);

    // Размеры иконок
    private static final int TRAIN_WIDTH = 50;
    private static final int TRAIN_HEIGHT = 30;
    private static final int BASE_STATION_WIDTH = 30;
    private static final int BASE_STATION_HEIGHT = 30;

    private static boolean draggingTrain = false;
    private static double dragStartX = 0;
    private static double trainPosition = 0;
    private static double totalTrackLength;
    private static long lastUpdate = 0;

    // Анимация
    private boolean isRunning = false;
    private static AnimationTimer timer;

    private BaseStation draggingStation;
    private static final Road2DPaneUi road2DPaneUi = new Road2DPaneUi();
    private static final List<BaseStation> stations = new ArrayList<>();


    public R2D(){

    }

    /**
     * Инициализирует первое появление
     */
    private void initializeDefaultScene() {

    }

    /**
     * Запуск симуляции
     * @param numStations
     * @param stationDistance
     */
    public void startSimulation(int numStations, double stationDistance) {
        // Очищаем предыдущие станции
        stations.clear();

        // Рассчитываем общую длину трека
        totalTrackLength = stationDistance * (numStations - 1);

        // Создаем станции с правильным масштабом
        double spacing = 900.0 / (numStations - 1);
        for (int i = 0; i < numStations; i++) {
            double position = stationDistance * i;
            int x = (int) (spacing * i);
            stations.add(new BaseStation(position, x));
        }

        trainPosition = 0;
        lastUpdate = 0;
        isRunning = true;

        if (timer != null) timer.stop();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isRunning) return;

                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                SimulatorController app = (SimulatorController) node.getUserData();
                if (app == null) return;

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                double speedKmph = app.getCurrentSpeed();
                trainPosition += (speedKmph / 3.6) * deltaTime;

                if (trainPosition > totalTrackLength) trainPosition = 0;

                lastUpdate = now;
                road2DPaneUi.setData(stations, trainPosition, totalTrackLength);
                road2DPaneUi.draw(canvas);
            }
        };
        timer.start();
    }


    /**
     * Устанавливает состояние анимации
     */
    public void toggleAnimation() {
        isRunning = !isRunning;
        if (isRunning) {
        }
    }

    /**
     * Перемещение объектов
     */
    private void addEventHandlers() {
        canvas.setOnMousePressed(e -> {
            double mouseX = e.getX();
            double mouseY = e.getY();

            // Проверяем, нажали ли на поезд
            double scale = 900.0 / totalTrackLength;
            int trainX = (int)(trainPosition * scale);
            if (Math.abs(mouseX - trainX) <= (double) TRAIN_WIDTH / 2 &&
                    Math.abs(mouseY - 70) <= (double) TRAIN_HEIGHT / 2) {
                draggingTrain = true;
            }

            int index = 0;
            // Проверяем, нажали ли на станцию
            for (BaseStation station : stations) {
                int x = station.getX();
                index++;
                if (Math.abs(mouseX - x) <= (double) BASE_STATION_WIDTH / 2 &&
                        Math.abs(mouseY - 85) <= (double) BASE_STATION_HEIGHT / 2) {
                    draggingStation = station;
                    dragStartX = mouseX - station.getX();
                    break;
                }
            }
        });

        canvas.setOnMouseReleased(e -> {
            draggingTrain = false;
            draggingStation = null;
        });

        canvas.setOnMouseDragged(e -> {
            double mouseX = e.getX();
            double mouseY = e.getY();

            if (draggingTrain) {
                double scale = totalTrackLength / 900.0;
                trainPosition = (mouseX - (double) WIDTH / 2) * scale;
                if (trainPosition < 0) trainPosition = 0;
                if (trainPosition > totalTrackLength) trainPosition = totalTrackLength;
                road2DPaneUi.setData(stations, trainPosition, totalTrackLength);
                road2DPaneUi.draw(canvas);
            }

            if (draggingStation != null) {
                draggingStation.setX((int)(mouseX - dragStartX));
                // Обновляем позицию в 3D с учетом масштаба
                road2DPaneUi.setData(stations, trainPosition, totalTrackLength);
                road2DPaneUi.draw(canvas);
            }
        });
    }

    public Node getNode(){
        return node;
    }
}
