//package ru.modulator.desktop.modulator.module;
//
//import javafx.animation.AnimationTimer;
//import javafx.scene.Group;
//import javafx.scene.Node;
//import javafx.scene.canvas.Canvas;
//import ru.modulator.desktop.modulator.SimulatorController;
//import ru.modulator.desktop.modulator.dto.BaseStation;
//import ru.modulator.desktop.modulator.ui.module.Road2DPaneUi;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Road2DPane {
//    private static final int DESIRED_WIDTH = 50;
//    private static final int DESIRED_HEIGHT = 30;
//    private static final int BASE_STATION_WIDTH = 30;
//    private static final int BASE_STATION_HEIGHT = 30;
//    private static final int DEFAULT_STATIONS = 3;
//
//    private static boolean draggingTrain = false;
//    private static double dragStartX = 0;
//    private static boolean running = false;
//    private static long lastUpdate = 0;
//    private static double trainPosition = 0;
//    private static double totalTrackLength;
//
//    private static AnimationTimer timer;
//
//    private static final List<BaseStation> stations = new ArrayList<>();
//    private static final Canvas canvas = new Canvas(1000, 200);
//    private static final Group root = new Group(canvas);
//
//    private static final Road2DPaneUi road2DPaneUi = new Road2DPaneUi();
//    private BaseStation draggingStation;
//    private Integer stationIndex;
//    private final Train3DScene train3DScene; // Сохраняем ссылку на 3D сцену
//
//    public Road2DPane(Train3DScene train3DScene) {
//        this.train3DScene = train3DScene;
//        addEventHandlers();
//        initializeDefaultScene();
//    }
//
//    private void initializeDefaultScene() {
//        // Получаем длину трека из 3D сцены
//        totalTrackLength = train3DScene.getTrackLength();
//
//        // Очищаем предыдущие станции
//        stations.clear();
//
//        // Рассчитываем расстояние между станциями
//        double stationDistance = totalTrackLength / (DEFAULT_STATIONS - 1);
//        double spacing = 900.0 / (DEFAULT_STATIONS - 1);
//
//        // Создаем станции с правильным масштабом
//        for (int i = 0; i < DEFAULT_STATIONS; i++) {
//            double position = stationDistance * i;
//            int x = (int) (spacing * i);
//            stations.add(new BaseStation(position, x));
//        }
//
//        trainPosition = 0;
//        road2DPaneUi.setData(stations, trainPosition, totalTrackLength);
//        road2DPaneUi.draw(canvas);
//    }
//
//    public void toggleAnimation() {
//        running = !running;
//        if (running) {
//            lastUpdate = 0;
//        }
//    }
//
//    public Node getNode() {
//        return root;
//    }
//
//
//    public void startSimulationPane(int numStations, double stationDistance) {
//        // Очищаем предыдущие станции
//        stations.clear();
//
//        // Рассчитываем общую длину трека
//        totalTrackLength = stationDistance * (numStations - 1);
//
//        // Создаем станции с правильным масштабом
//        double spacing = 900.0 / (numStations - 1);
//        for (int i = 0; i < numStations; i++) {
//            double position = stationDistance * i;
//            int x = (int) (spacing * i);
//            stations.add(new BaseStation(position, x));
//        }
//
//        trainPosition = 0;
//        lastUpdate = 0;
//        running = true;
//
//        if (timer != null) timer.stop();
//        timer = new AnimationTimer() {
//            @Override
//            public void handle(long now) {
//                if (!running) return;
//
//                if (lastUpdate == 0) {
//                    lastUpdate = now;
//                    return;
//                }
//
//                SimulatorController app = (SimulatorController) root.getUserData();
//                if (app == null) return;
//
//                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
//                double speedKmph = app.getCurrentSpeed();
//                trainPosition += (speedKmph / 3.6) * deltaTime;
//
//                if (trainPosition > totalTrackLength) trainPosition = 0;
//
//                lastUpdate = now;
//                road2DPaneUi.setData(stations, trainPosition, totalTrackLength);
//                road2DPaneUi.draw(canvas);
//            }
//        };
//        timer.start();
//    }
//
//    private void addEventHandlers() {
//        canvas.setOnMousePressed(e -> {
//            double mouseX = e.getX();
//            double mouseY = e.getY();
//
//            // Проверяем, нажали ли на поезд
//            double scale = 900.0 / totalTrackLength;
//            int trainX = (int)(trainPosition * scale);
//            if (Math.abs(mouseX - trainX) <= (double) DESIRED_WIDTH / 2 &&
//                    Math.abs(mouseY - 70) <= (double) DESIRED_HEIGHT / 2) {
//                draggingTrain = true;
//            }
//
//            int index = 0;
//            // Проверяем, нажали ли на станцию
//            for (BaseStation station : stations) {
//                int x = station.getX();
//                index++;
//                if (Math.abs(mouseX - x) <= (double) BASE_STATION_WIDTH / 2 &&
//                        Math.abs(mouseY - 85) <= (double) BASE_STATION_HEIGHT / 2) {
//                    draggingStation = station;
//                    dragStartX = mouseX - station.getX();
//                    stationIndex = index;
//                    break;
//                }
//            }
//        });
//
//        canvas.setOnMouseReleased(e -> {
//            draggingTrain = false;
//            draggingStation = null;
//        });
//
//        canvas.setOnMouseDragged(e -> {
//            double mouseX = e.getX();
//            double mouseY = e.getY();
//
//            if (draggingTrain) {
//                double scale = totalTrackLength / 900.0;
//                trainPosition = (mouseX - (double) DESIRED_WIDTH / 2) * scale;
//                if (trainPosition < 0) trainPosition = 0;
//                if (trainPosition > totalTrackLength) trainPosition = totalTrackLength;
//                train3DScene.setTrainPosition(trainPosition);
//                road2DPaneUi.setData(stations, trainPosition, totalTrackLength);
//                road2DPaneUi.draw(canvas);
//            }
//
//            if (draggingStation != null) {
//                draggingStation.setX((int)(mouseX - dragStartX));
//                // Обновляем позицию в 3D с учетом масштаба
//                double trackScale = train3DScene.getTrackLength() / 900.0;
//                double realX = draggingStation.getX() * trackScale;
//                train3DScene.setStationPosition(stationIndex-1, realX);
//                road2DPaneUi.setData(stations, trainPosition, totalTrackLength);
//                road2DPaneUi.draw(canvas);
//            }
//        });
//    }
//}