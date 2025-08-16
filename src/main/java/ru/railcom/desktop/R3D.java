package ru.railcom.desktop;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import ru.railcom.desktop.dto.BaseStation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ru.railcom.desktop.RP.*;
import static ru.railcom.desktop.ui.module.Train3DSceneUI.createBaseStationModel;
import static ru.railcom.desktop.ui.module.Train3DSceneUI.setupSceneContent;

@Getter
@Setter
public class R3D {
    // Размер экрана
    private static final Integer WIDTH = 1000;
    private static final Integer HEIGHT = 350;

    private final Group root3D = new  Group();
    private final SubScene subScene = new SubScene(root3D, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);;

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

    // Метод для установки контроллера (для обратной связи)
    private SimulatorController controller;

    private final List<BaseStation> baseStations = new ArrayList<>();


    public R3D() {
        PerspectiveCamera camera = setupCamera();
        trainGroup = setupSceneContent(root3D);

        // Настройки элемента
        subScene.setFill(Color.rgb(30, 30, 50));
        subScene.setCamera(camera);
        subScene.setPickOnBounds(false);

        setupCameraControls();
    }

    /**
     * Запуск симуляции
     * @param numStations
     * @param stationDistanceKm
     */
    public void startSimulation(int numStations, double stationDistanceKm) {
        // Очищаем предыдущие станции
        for (BaseStation station : baseStations) {
            root3D.getChildren().remove(station.getNode());
        }
        baseStations.clear();

        // Рассчитываем общее расстояние в км
        double totalDistanceKm = (numStations - 1) * stationDistanceKm;

        // Если расстояние слишком мало для отображения
        if (totalDistanceKm < 1) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Общее расстояние должно быть больше 0 км");
                alert.showAndWait();
            });
            return;
        }

        // Размещаем станции равномерно
        double stationSpacing = (double) TRACK_LENGTH / (numStations - 1);

        for (int i = 0; i < numStations; i++) {
            double positionX = TRACK_START + i * stationSpacing;
            BaseStation station = new BaseStation((positionX - TRACK_START) / TRACK_LENGTH, (int) positionX);
            baseStations.add(station);

            // Создаем 3D-модель базовой станции
            createBaseStationModel(station, baseStations, root3D);
        }

        // Останавливаем текущую анимацию, если она есть
        stopAnimation();

        // Устанавливаем поезд в начальную позицию
        // Учитываем переворот на 180 градусов
        trainGroup.setTranslateX(TRACK_START);

        // Запускаем анимацию движения поезда
        startAnimation();
    }

    /**
     * Параметры камеры
     */
    private PerspectiveCamera setupCamera()     {
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
                    if (t instanceof Rotate) {
                        Rotate r = (Rotate) t;
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
     * Устанавливает состояние анимации
     */
    public void toggleAnimation() {
        if (isRunning) {
        } else {
        }
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
                    double currentX = trainGroup.getTranslateX();
                    double newSpeed = controller.getCurrentSpeed() / 100.0;
                    trainGroup.setTranslateX(currentX + newSpeed);

//                    updateSignalStrength();

                    if (trainGroup.getTranslateX() > TRACK_END) {
                        trainGroup.setTranslateX(TRACK_START);
                    }
                })
        );
        animationTimeline.setCycleCount(Animation.INDEFINITE);
        animationTimeline.play();

        isRunning = true;
    }

    /**
     * Запуск остановка анимации
     */
    private void stopAnimation() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        isRunning = false;
    }

    /**
     * Перезапуск анимации
     */
    public void resetSimulation() {
        stopAnimation();
        trainGroup.setTranslateX(TRACK_START);
        isRunning = false;

    }

    /**
     * Устанавливает позицию поезда в процентах от общей длины пути
     */
    public void setTrainPosition(double positionPercent) {
        // Преобразуем проценты в позицию на сцене
//        double scenePosition = TRACK_START + (TRACK_LENGTH * positionPercent / 100.0);
//        trainGroup.setTranslateX(scenePosition);

    }
    /**
     * Изменение цвета поезда в зависимости от силы сигнала
     */
    private void updateTrainColor(Color color) {
        for (Node node : trainGroup.getChildren()) {
            if (node instanceof Box) {
                ((Box) node).setMaterial(new PhongMaterial(color));
            }
        }
    }

    /**
     * Устанавливает позицию базовой станции в процентах от общей длины пути
     */
    public void setStationPosition(int stationIndex, double positionPercent) {
        if (stationIndex < 0 || stationIndex >= baseStations.size()) {
            return;
        }

        // Преобразуем проценты в позицию на сцене
        double scenePosition = TRACK_START + (TRACK_LENGTH * positionPercent / 100.0);

        // Обновляем позицию станции
        BaseStation station = baseStations.get(stationIndex);
        Group stationNode = (Group) station.getNode();

        // Удаляем старую позицию
        root3D.getChildren().remove(stationNode);

        // Создаем новую позицию
        station = new BaseStation(positionPercent / 100.0, (int) scenePosition);
        baseStations.set(stationIndex, station);

        // Сортируем станции по позиции
        baseStations.sort(Comparator.comparingDouble(BaseStation::getPosition));

        // Создаем 3D-модель базовой станции
        createBaseStationModel(station, baseStations, root3D);


    }

    public Node getNode() {
        return subScene;
    }
}
