//package ru.modulator.desktop.modulator.module;
//
//import javafx.animation.*;
//import javafx.application.Platform;
//import javafx.beans.property.DoubleProperty;
//import javafx.beans.property.SimpleDoubleProperty;
//import javafx.geometry.Point3D;
//import javafx.scene.*;
//import javafx.scene.control.Alert;
//import javafx.scene.input.*;
//import javafx.scene.paint.*;
//import javafx.scene.shape.*;
//import javafx.scene.transform.*;
//import javafx.util.Duration;
//import lombok.Getter;
//import lombok.Setter;
//import ru.modulator.desktop.modulator.SimulatorController;
//import ru.modulator.desktop.modulator.dto.BaseStation;
//import ru.modulator.desktop.modulator.dto.Train;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//
//import static ru.modulator.desktop.modulator.ui.module.Train3DSceneUI.createBaseStationModel;
//import static ru.modulator.desktop.modulator.ui.module.Train3DSceneUI.setupSceneContent;
//
//public class Train3DScene {
//    // Переменные для управления камерой
//    private double cameraDistance = 1000;
//    private double cameraXAngle = 0;
//    private double cameraYAngle = 0;
//    private double lastMouseX, lastMouseY;
//
//    // Переменные для анимации
//    private TranslateTransition moveTrain;
//    @Getter
//    private boolean isRunning = false;
//    private Timeline animationTimeline;
//    private double trainSpeed = 1.5; // Скорость движения поезда
//
//    // Графики сигнала
//    private double signalStrength = 0;
//    private final DoubleProperty signalProperty = new SimpleDoubleProperty(0);
//
//    // Ссылка на поезд для отслеживания позиции
//    @Setter
//    private Group trainGroup;
//    private final Group root3D;
//    private final SubScene subScene;
//
//    // Позиции базовых станций
//    private final List<BaseStation> baseStations = new ArrayList<>();
//
//    // Константы для сцены
//    private static final double TRACK_START = 0 ;
//    private static final double TRACK_END = 1000;
//    private static final double TRACK_LENGTH = TRACK_END - TRACK_START;
//    private static final double RAIL_Y = 5; // Уровень рельс
//
//    // Метод для установки контроллера (для обратной связи)
//    @Setter
//    private SimulatorController controller;
//
//    public Train3DScene() {
//        root3D = new Group();
//        PerspectiveCamera camera = setupCamera(root3D);
//        trainGroup = setupSceneContent(root3D);
//
//        subScene = new SubScene(root3D, 1000, 350, true, SceneAntialiasing.BALANCED);
//        subScene.setFill(Color.rgb(30, 30, 50));
//        subScene.setCamera(camera);
//        subScene.setPickOnBounds(false);
//
//        setupCameraControls();
//    }
//
//    private PerspectiveCamera setupCamera(Group root3D) {
//        PerspectiveCamera camera = new PerspectiveCamera(true);
//        camera.setFarClip(10000);
//        camera.setNearClip(0.1);
//
//        // Группа для управления камерой
//        Group cameraGroup = new Group();
//
//        // Создаем повороты для камеры
//        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
//        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
//        cameraGroup.getTransforms().addAll(rotateY, rotateX);
//
//        // Устанавливаем начальное положение камеры
//        camera.setTranslateZ(-cameraDistance);
//        camera.setTranslateY(-150);
//
//        // Добавляем камеру как дочерний узел группы
//        cameraGroup.getChildren().add(camera);
//
//        // Добавляем группу с камерой в корневую группу
//        root3D.getChildren().add(cameraGroup);
//
//        // Добавляем освещение
//        AmbientLight ambientLight = new AmbientLight(Color.rgb(200, 200, 200));
//        root3D.getChildren().add(ambientLight);
//
////        DirectionalLight directionalLight = new DirectionalLight();
////        directionalLight.setColor(Color.rgb(255, 255, 220));
////        directionalLight.setDirection(new Point3D(-1, -1, -1));
////        root3D.getChildren().add(directionalLight);
//
//        // Добавляем дополнительный источник света для подсветки моделей
//        PointLight pointLight = new PointLight(Color.WHITE);
//        pointLight.setTranslateY(100);
//        pointLight.setTranslateZ(500);
//        pointLight.setConstantAttenuation(0.5);
//        root3D.getChildren().add(pointLight);
//
//        return camera;
//    }
//
//
//    public void startSimulation(int numStations, double stationDistanceKm) {
//        // Очищаем предыдущие станции
//        for (BaseStation station : baseStations) {
//            root3D.getChildren().remove(station.getNode());
//        }
//        baseStations.clear();
//
//        // Рассчитываем общее расстояние в км
//        double totalDistanceKm = (numStations - 1) * stationDistanceKm;
//
//        // Если расстояние слишком мало для отображения
//        if (totalDistanceKm < 1) {
//            Platform.runLater(() -> {
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("Ошибка");
//                alert.setHeaderText(null);
//                alert.setContentText("Общее расстояние должно быть больше 0 км");
//                alert.showAndWait();
//            });
//            return;
//        }
//
//        // Размещаем станции равномерно
//        double stationSpacing = TRACK_LENGTH / (numStations - 1);
//
//        for (int i = 0; i < numStations; i++) {
//            double positionX = TRACK_START + i * stationSpacing;
//            BaseStation station = new BaseStation((positionX - TRACK_START) / TRACK_LENGTH, (int) positionX);
//            baseStations.add(station);
//
//            // Создаем 3D-модель базовой станции
//            createBaseStationModel(station, baseStations, root3D);
//        }
//
//        // Останавливаем текущую анимацию, если она есть
//        stopAnimation();
//
//        // Устанавливаем поезд в начальную позицию
//        // Учитываем переворот на 180 градусов
//        trainGroup.setTranslateX(TRACK_START);
//
//        // Запускаем анимацию движения поезда
//        startAnimation();
//    }
//
//    private void startAnimation() {
//        if (animationTimeline != null) {
//            animationTimeline.stop();
//        }
//
//        animationTimeline = new Timeline(
//                new KeyFrame(Duration.millis(20), e -> {
//                    double currentX = trainGroup.getTranslateX();
//                    double newSpeed = controller.getCurrentSpeed() / 100.0;
//                    trainGroup.setTranslateX(currentX + newSpeed);
//                    trainSpeed = newSpeed;
//
//                    updateSignalStrength();
//
//                    if (trainGroup.getTranslateX() > TRACK_END) {
//                        trainGroup.setTranslateX(TRACK_START);
//                    }
//                })
//        );
//        animationTimeline.setCycleCount(Animation.INDEFINITE);
//        animationTimeline.play();
//
//        isRunning = true;
//    }
//
//    private void stopAnimation() {
//        if (animationTimeline != null) {
//            animationTimeline.stop();
//        }
//        isRunning = false;
//    }
//
//    public void toggleAnimation() {
//        if (isRunning) {
//            stopAnimation();
//        } else {
//            startAnimation();
//        }
//    }
//
//    public void resetSimulation() {
//        stopAnimation();
//        trainGroup.setTranslateX(TRACK_START);
//        isRunning = false;
//
//        // Сбрасываем все данные
//        signalStrength = 0;
//        signalProperty.set(0);
//    }
//
//    public DoubleProperty signalProperty() {
//        return signalProperty;
//    }
//
//    private void updateSignalStrength() {
//        double trainX = trainGroup.getTranslateX();
//
//        // Рассчитываем позицию в относительных координатах
//        double positionRatio = (trainX - TRACK_START) / TRACK_LENGTH;
//
//        if (baseStations.isEmpty()) {
//            signalStrength = 0;
//        } else {
//            double minDistance = Double.MAX_VALUE;
//
//            for (BaseStation station : baseStations) {
//                double distance = Math.abs(station.getPosition() - positionRatio);
//                if (distance < minDistance) {
//                    minDistance = distance;
//                }
//            }
//
//            double maxRange = 0.15;
//            signalStrength = 1 - Math.min(minDistance / maxRange, 1);
//            signalStrength = Math.max(0, signalStrength);
//        }
//
//        double r = 1 - signalStrength;
//        double g = signalStrength;
//        updateTrainColor(Color.color(r, g, 0));
//
//        signalProperty.set(signalStrength);
//
//        if (controller != null) {
//            double speedKmph = controller.getCurrentSpeed();
//            Train trainModel = new Train(speedKmph);
//
//            double downlink = trainModel.getDownlink(signalStrength);
//            double uplink = trainModel.getUplink(signalStrength);
//
//            controller.updateLinks(signalStrength, downlink, uplink);
//        }
//    }
//
//    private void updateTrainColor(Color color) {
//        for (Node node : trainGroup.getChildren()) {
//            if (node instanceof Box) {
//                ((Box) node).setMaterial(new PhongMaterial(color));
//            }
//        }
//    }
//
//    public Node getNode() {
//        return subScene;
//    }
//
//    private void setupCameraControls() {
//        subScene.setOnMousePressed(event -> {
//            if (event.isPrimaryButtonDown()) {
//                lastMouseX = event.getSceneX();
//                lastMouseY = event.getSceneY();
//            }
//        });
//
//        subScene.setOnMouseDragged(event -> {
//            if (event.isPrimaryButtonDown()) {
//                double dx = (event.getSceneX() - lastMouseX) * 0.5;
//                double dy = (event.getSceneY() - lastMouseY) * 0.5;
//
//                cameraYAngle += dx;
//                cameraXAngle = Math.max(-90, Math.min(90, cameraXAngle - dy));
//
//                PerspectiveCamera camera = (PerspectiveCamera) subScene.getCamera();
//                Group cameraGroup = (Group) camera.getParent();
//                for (Transform t : cameraGroup.getTransforms()) {
//                    if (t instanceof Rotate) {
//                        Rotate r = (Rotate) t;
//                        if (r.getAxis() == Rotate.Y_AXIS) {
//                            r.setAngle(cameraYAngle);
//                        } else if (r.getAxis() == Rotate.X_AXIS) {
//                            r.setAngle(cameraXAngle);
//                        }
//                    }
//                }
//
//                lastMouseX = event.getSceneX();
//                lastMouseY = event.getSceneY();
//            }
//        });
//
//        subScene.setOnScroll(event -> {
//            cameraDistance = Math.max(100, Math.min(2000,
//                    cameraDistance - event.getDeltaY()));
//
//            PerspectiveCamera camera = (PerspectiveCamera) subScene.getCamera();
//            camera.setTranslateZ(-cameraDistance);
//        });
//
//        subScene.setOnMouseClicked(event -> {
//            if (event.getButton() == MouseButton.SECONDARY) {
//                PickResult pickResult = event.getPickResult();
//                if (pickResult != null && pickResult.getIntersectedNode() != null) {
//                    addBaseStationAt(pickResult);
//                }
//            }
//        });
//    }
//
//    private void addBaseStationAt(PickResult pickResult) {
//        Point3D intersectedPoint = pickResult.getIntersectedPoint();
//        double x = intersectedPoint.getX();
//
//        // Проверяем, что точка находится на треке
//        if (x < TRACK_START || x > TRACK_END) {
//            return;
//        }
//
//        for (BaseStation station : baseStations) {
//            if (Math.abs(station.getX() - x) < 50) {
//                Platform.runLater(() -> {
//                    Alert alert = new Alert(Alert.AlertType.WARNING);
//                    alert.setTitle("Ошибка размещения");
//                    alert.setHeaderText(null);
//                    alert.setContentText("Базовые станции должны быть разнесены минимум на 50 единиц");
//                    alert.showAndWait();
//                });
//                return;
//            }
//        }
//
//        BaseStation station = new BaseStation((x - TRACK_START) / TRACK_LENGTH, (int) x);
//        baseStations.add(station);
//
//        baseStations.sort(Comparator.comparingDouble(BaseStation::getPosition));
//
//        createBaseStationModel(station, baseStations, root3D);
//
//    }
//
//    /**
//     * Устанавливает позицию поезда в процентах от общей длины пути
//     */
//    public void setTrainPosition(double positionPercent) {
//        // Преобразуем проценты в позицию на сцене
//        double scenePosition = TRACK_START + (TRACK_LENGTH * positionPercent / 100.0);
//        trainGroup.setTranslateX(scenePosition);
//
//        // Обновляем сигнал
//        updateSignalStrength();
//    }
//
//    /**
//     * Возвращает позицию поезда в процентах от общей длины пути
//     */
//    public double getTrainPosition() {
//        // Преобразуем позицию на сцене в проценты
//        double positionPercent = ((trainGroup.getTranslateX() - TRACK_START) / TRACK_LENGTH) * 100.0;
//        return Math.max(0, Math.min(100, positionPercent));
//    }
//
//    /**
//     * Устанавливает позицию базовой станции в процентах от общей длины пути
//     */
//    public void setStationPosition(int stationIndex, double positionPercent) {
//        if (stationIndex < 0 || stationIndex >= baseStations.size()) {
//            return;
//        }
//
//        // Преобразуем проценты в позицию на сцене
//        double scenePosition = TRACK_START + (TRACK_LENGTH * positionPercent / 100.0);
//
//        // Обновляем позицию станции
//        BaseStation station = baseStations.get(stationIndex);
//        Group stationNode = (Group) station.getNode();
//
//        // Удаляем старую позицию
//        root3D.getChildren().remove(stationNode);
//
//        // Создаем новую позицию
//        station = new BaseStation(positionPercent / 100.0, (int) scenePosition);
//        baseStations.set(stationIndex, station);
//
//        // Сортируем станции по позиции
//        baseStations.sort(Comparator.comparingDouble(BaseStation::getPosition));
//
//        // Создаем 3D-модель базовой станции
//        createBaseStationModel(station, baseStations, root3D);
//
//
//        // Обновляем сигнал
//        updateSignalStrength();
//    }
//
//    /**
//     * Возвращает позицию базовой станции в процентах от общей длины пути
//     */
//    public double getStationPosition(int stationIndex) {
//        if (stationIndex < 0 || stationIndex >= baseStations.size()) {
//            return 0;
//        }
//
//        // Преобразуем позицию на сцене в проценты
//        BaseStation station = baseStations.get(stationIndex);
//        double positionPercent = station.getPosition() * 100.0;
//        return Math.max(0, Math.min(100, positionPercent));
//    }
//
//    /**
//     * Возвращает количество базовых станций
//     */
//    public int getNumStations() {
//        return baseStations.size();
//    }
//
//    public double getTrackLength() {
//        return TRACK_LENGTH; // Ваша константа длины трека
//    }
//
//}