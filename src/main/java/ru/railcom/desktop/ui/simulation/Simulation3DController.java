package ru.railcom.desktop.ui.simulation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.Alert;
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
import java.util.ResourceBundle;

public class Simulation3DController implements Initializable {

    @FXML
    private Pane simulation3DContainer;

    private Group root3D;
    private SubScene subScene;

    // Переменные для управления камерой
    private double cameraDistance = 1000;
    private double cameraXAngle = 0;
    private double cameraYAngle = 0;
    private double lastMouseX, lastMouseY;

    // Анимация
    private boolean isRunning = false;
    private Timeline animationTimeline;

    // Элементы для отслеживания позиций
    private Group trainGroup;

    @Setter
    private SimulationController simulationController;

    // Список станций для 3D отображения, синхронизируемый с 2D
    private final List<BaseStation> baseStations = new ArrayList<>();
    private final List<Cylinder> radioChannelLines = new ArrayList<>(); // <-- Новые линии

    private double totalTrackLength = 1000; // Масштабный коэффициент: 1 км = 100 единиц


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root3D = new Group();

        subScene = new SubScene(root3D, 0, 0, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(simulation3DContainer.widthProperty());
        subScene.heightProperty().set(400);

        // Добавляем SubScene в контейнер
        simulation3DContainer.getChildren().add(subScene);
        // Устанавливаем стили
        simulation3DContainer.getStylesheets().add(getClass().getResource("/css/simulation/simulation_3d.css").toExternalForm());

        // Подписываемся на события
        EventBus.getInstance().subscribe(this::onSimulationUpdate);

        trainGroup = setupSceneContent();

        PerspectiveCamera camera = setupCamera();
        subScene.setCamera(camera);
        setupCameraControls();
    }

    /**
     * Обработчик событий симуляции
     */
    private void onSimulationUpdate() {
        if (simulationController == null) {
            return;
        }

        // Обновляем длину трассы
        double newTotalDistanceKm = simulationController.getTrack();
        double oldTotalDistanceKm = totalTrackLength / 100.0;
        totalTrackLength = newTotalDistanceKm * 100;

        // Обновляем симуляцию
        int count = simulationController.getCountBaseStations();
        // Используем обновленный метод, который синхронизирует станции
        updateStationsFromController(count, newTotalDistanceKm, oldTotalDistanceKm);

        // Обновляем состояние анимации
        if (simulationController.isRunning()) {
            if (!isRunning) {
                startAnimation();
            }
        } else {
            if (isRunning) {
                stopAnimation();
            }
        }

        // Обновляем позицию поезда
        updateTrainPosition();
    }

    /**
     * Обновление станций на основе данных из контроллера, включая позиции, измененные в 2D
     */
    public void updateStationsFromController(int numStations, double newTotalDistanceKm, double oldTotalDistanceKm) {
        if (numStations <= 0 || newTotalDistanceKm <= 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Количество станций и общее расстояние должны быть больше 0");
                alert.showAndWait();
            });
            return;
        }

        // Получаем текущий список станций из контроллера (включая измененные в 2D)
        List<BaseStation> controllerStations = simulationController.getBaseStations();

        // Очищаем предыдущие станции
        for (BaseStation station : baseStations) {
            if (station.getNode() != null) {
                root3D.getChildren().remove(station.getNode());
            }
        }
        baseStations.clear();

        if (controllerStations != null && !controllerStations.isEmpty()) {
            // Копируем станции из контроллера и создаем 3D модели
            for (BaseStation controllerStation : controllerStations) {
                // Масштабируем позицию из км в 3D координаты
                double positionX = (controllerStation.getPosition() / newTotalDistanceKm) * totalTrackLength;

                // Создаем новую станцию для 3D сцены с обновленной позицией X
                BaseStation station3D = new BaseStation(
                        controllerStation.getPosition() / newTotalDistanceKm, // positionPercent
                        (int) positionX,
                        null // Node будет установлен в createBaseStationModel
                );
                baseStations.add(station3D);

                // Создаем 3D-модель базовой станции
                createBaseStationModel(station3D, root3D);
            }
        } else {
            // Если станции не заданы, создаем равномерно
            double stationSpacing = totalTrackLength / (numStations - 1);

            for (int i = 0; i < numStations; i++) {
                double positionX = i * stationSpacing;
                double positionPercent = positionX / totalTrackLength;
                BaseStation station = new BaseStation(positionPercent, (int) positionX, null);
                baseStations.add(station);

                // Создаем 3D-модель базовой станции
                createBaseStationModel(station, root3D);
            }
        }

        // Останавливаем текущую анимацию, если она есть
        stopAnimation();

        // Запускаем анимацию движения поезда, если симуляция запущена
        if (simulationController != null && simulationController.isRunning()) {
            startAnimation();
        }
    }


    /**
     * Параметры камеры
     */
    private PerspectiveCamera setupCamera() {
        PerspectiveCamera camera = new PerspectiveCamera(true);

        camera.setFarClip(10000);
        camera.setNearClip(0.1);

        // Группа для управления камерой
        Group cameraGroup = new Group();

        // Создаем повороты для камеры
        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        cameraGroup.getTransforms().addAll(rotateY, rotateX);

        // Устанавливаем начальное положение камеры
        camera.setTranslateZ(-cameraDistance);
        camera.setTranslateY(-150);

        // Добавляем камеру как дочерний узел группы
        cameraGroup.getChildren().add(camera);

        // Добавляем группу с камерой в корневую группу
        root3D.getChildren().add(cameraGroup);

        // Добавляем освещение
        AmbientLight ambientLight = new AmbientLight(Color.rgb(200, 200, 200));
        root3D.getChildren().add(ambientLight);

        // Добавляем дополнительный источник света для подсветки моделей
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateY(100);
        pointLight.setTranslateZ(500);
        pointLight.setConstantAttenuation(0.5);
        root3D.getChildren().add(pointLight);
        return camera;
    }

    /**
     * Параметры управления камерой
     */
    private void setupCameraControls() {
        subScene.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                lastMouseX = event.getSceneX();
                lastMouseY = event.getSceneY();
            }
        });

        subScene.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double dx = (event.getSceneX() - lastMouseX) * 0.5;
                double dy = (event.getSceneY() - lastMouseY) * 0.5;

                cameraYAngle += dx;
                cameraXAngle = Math.max(-90, Math.min(90, cameraXAngle - dy));

                PerspectiveCamera camera = (PerspectiveCamera) subScene.getCamera();
                Group cameraGroup = (Group) camera.getParent();
                for (Transform t : cameraGroup.getTransforms()) {
                    if (t instanceof Rotate r) {
                        if (r.getAxis() == Rotate.Y_AXIS) {
                            r.setAngle(cameraYAngle);
                        } else if (r.getAxis() == Rotate.X_AXIS) {
                            r.setAngle(cameraXAngle);
                        }
                    }
                }

                lastMouseX = event.getSceneX();
                lastMouseY = event.getSceneY();
            }
        });

        subScene.setOnScroll(event -> {
            cameraDistance = Math.max(100, Math.min(2000,
                    cameraDistance - event.getDeltaY()));

            PerspectiveCamera camera = (PerspectiveCamera) subScene.getCamera();
            camera.setTranslateZ(-cameraDistance);
        });
    }

    /**
     * Создание поезда и рельс
     */
    private Group setupSceneContent() {
        // Рельсы
//        Cylinder rail1 = new Cylinder(3, totalTrackLength);
//        rail1.setTranslateY(5);
//        rail1.setTranslateZ(15);
//        rail1.setMaterial(new PhongMaterial(Color.SILVER));
//        Rotate rotation1 = new Rotate(90, Rotate.Z_AXIS);
//        rail1.getTransforms().add(rotation1);
//
//        Cylinder rail2 = new Cylinder(3, totalTrackLength);
//        rail2.setTranslateY(5);
//        rail2.setTranslateX(1);
//        rail2.setTranslateZ(-15);
//        rail2.setMaterial(new PhongMaterial(Color.SILVER));
//        Rotate rotation2 = new Rotate(90, Rotate.Z_AXIS);
//        rail2.getTransforms().add(rotation2);

        // Шпалы
        for (int i = 0; i < totalTrackLength; i += 100) {
            Box sleeper = new Box(20, 2, 30);
            sleeper.setTranslateX(i);
            sleeper.setTranslateY(5);
            sleeper.setMaterial(new PhongMaterial(Color.DARKGRAY));
            root3D.getChildren().add(sleeper);
        }

        // Поезд
        Group trainGroup = new Group();
        Box locomotive = new Box(40, 25, 30);
        locomotive.setTranslateX(0); // Начальная позиция
        locomotive.setMaterial(new PhongMaterial(Color.web("#FF5252")));

        Box wagon1 = new Box(30, 20, 30);
        wagon1.setTranslateX(40);
        wagon1.setMaterial(new PhongMaterial(Color.web("#4FC3F7")));

        Box wagon2 = new Box(30, 20, 30);
        wagon2.setTranslateX(80);
        wagon2.setMaterial(new PhongMaterial(Color.web("#69F0AE")));

        trainGroup.getChildren().addAll(locomotive, wagon1, wagon2);

        // Колеса с анимацией
        for (int i = 0; i < 4; i++) {
            double offset = i % 2 == 0 ? -15 : 15;
            Cylinder wheel = new Cylinder(5, 8, 32);
            wheel.setTranslateX(15 + (i / 2) * 30);
            wheel.setTranslateY(12);
            wheel.setTranslateZ(offset);
            wheel.setMaterial(new PhongMaterial(Color.DARKGRAY));
            trainGroup.getChildren().add(wheel);

            javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(Duration.seconds(1), wheel);
            rt.setAxis(Rotate.Z_AXIS);
            rt.setByAngle(360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.play();
        }

//        root3D.getChildren().addAll(rail1, rail2, trainGroup);
        root3D.getChildren().addAll(trainGroup);

        return trainGroup;
    }

    public static void createBaseStationModel(BaseStation station, Group root3D) {
        // Башня
        Box tower = new Box(15, 60, 15);
        tower.setTranslateX(station.getX());
        tower.setTranslateY(-30);
        tower.setTranslateZ(40);
        tower.setMaterial(new PhongMaterial(Color.web("#B0BEC5")));

        // Основание
        Box base = new Box(40, 5, 40);
        base.setTranslateX(station.getX());
        base.setTranslateY(0);
        base.setTranslateZ(40);
        base.setMaterial(new PhongMaterial(Color.web("#78909C")));

        station.setNode(new Group(tower, base));
        root3D.getChildren().add(station.getNode());
    }

    /**
     * Запуск анимации
     */
    private void startAnimation() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        animationTimeline = new Timeline(
                new KeyFrame(Duration.millis(20), e -> {
                    if (simulationController != null && simulationController.isRunning()) {
                        double currentSpeed = simulationController.getCurrentSpeed();
                        double deltaTime = 0.02; // 20 мс
                        double displacement = (currentSpeed / 3.6) * 100 * deltaTime; // масштабируем к 3D

                        double currentX = trainGroup.getTranslateX();
                        double newX = currentX + displacement;

                        if (newX > totalTrackLength) {
                            newX = 0;
                        }

                        trainGroup.setTranslateX(newX);

                        // Обновляем позицию в контроллере
                        double newTrainPosition = (newX / totalTrackLength) * (simulationController.getTrack());
                        simulationController.setTrainPosition(newTrainPosition);

                        // Публикуем событие для синхронизации с 2D
                        EventBus.getInstance().publish();
                    }
                })
        );
        animationTimeline.setCycleCount(Animation.INDEFINITE);
        animationTimeline.play();

        isRunning = true;
    }

    /**
     * Остановка анимации
     */
    private void stopAnimation() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        isRunning = false;
    }

    /**
     * Сброс симуляции
     */
    public void resetSimulation() {
        stopAnimation();
        trainGroup.setTranslateX(0);
        isRunning = false;
    }

    /**
     * Устанавливает позицию поезда в зависимости от текущего контроллера
     */
    public void updateTrainPosition() {
        if (simulationController != null) {
            double positionPercent = simulationController.getTrainPosition() / simulationController.getTrack();
            double scenePosition = totalTrackLength * positionPercent;
            trainGroup.setTranslateX(scenePosition);
        }
    }

    public Node getNode() {
        return subScene;
    }
}