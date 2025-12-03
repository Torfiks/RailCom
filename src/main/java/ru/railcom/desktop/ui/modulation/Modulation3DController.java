package ru.railcom.desktop.ui.modulation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import lombok.Setter;
import ru.railcom.desktop.modulation.Complex;
import ru.railcom.desktop.modulation.ModulateController;
import ru.railcom.desktop.modulation.Modulation;
import ru.railcom.desktop.simulation.EventBus;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static ru.railcom.desktop.modulation.Modulate.generateReferenceConstellation;
import static ru.railcom.desktop.modulation.Modulate.modulate;

public class Modulation3DController implements Initializable {

    @FXML
    public Pane modulation3DContainer;

    private Group root3D;
    private SubScene subScene;
    private Group rotationGroup;

    @Setter
    private ModulateController modulateController;

    private double mouseLastX = 0;
    private double mouseLastY = 0;
    private boolean dragging = false;

    private final Group constellationGroup = new Group();

    // Сохраняем углы поворота
    private double rotationX = 0;
    private double rotationY = 0;

    // Параметры
    private static final double SYMBOL_SPACING = 50;
    private static final double AXIS_LENGTH = 25;
    private static final double AXIS_THICKNESS = 0.5;
    private static final double DOT_RADIUS = 3;
    private static final double SCALE_FACTOR = 40; // Оптимальный масштаб для отображения
    private static final double LABEL_OFFSET = 15; // Смещение подписей

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Создаем группу для управления вращением
        rotationGroup = new Group();
        root3D = new Group();
        root3D.getChildren().add(rotationGroup);
        rotationGroup.getChildren().add(constellationGroup);

        subScene = new SubScene(root3D, 0, 0, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(modulation3DContainer.widthProperty());
        subScene.heightProperty().set(500);
        modulation3DContainer.getChildren().add(subScene);

        modulation3DContainer.getStylesheets().add(getClass().getResource("/css/modulation/modulation_3d.css").toExternalForm());

        setupCamera();
        addLights();
        setupMouseControl();

        EventBus.getInstance().subscribe(this::onSimulationUpdate);
    }

    private void setupCamera() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setFarClip(10000);
        camera.setNearClip(0.1);
        camera.setTranslateZ(-800);
        camera.setTranslateY(-100);
        subScene.setCamera(camera);
    }

    private void addLights() {
        AmbientLight ambientLight = new AmbientLight(Color.rgb(200, 200, 200));
        root3D.getChildren().add(ambientLight);

        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateY(100);
        pointLight.setTranslateZ(500);
        pointLight.setConstantAttenuation(0.5);
        root3D.getChildren().add(pointLight);
    }

    private void setupMouseControl() {
        subScene.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                dragging = true;
                mouseLastX = event.getSceneX();
                mouseLastY = event.getSceneY();
            }
            event.consume();
        });

        subScene.setOnMouseDragged(event -> {
            if (dragging) {
                double dx = event.getSceneX() - mouseLastX;
                double dy = event.getSceneY() - mouseLastY;

                // Сохраняем углы поворота
                rotationY += dx * 0.5;
                rotationX += dy * 0.5;

                // Применяем повороты к группе вращения
                rotationGroup.getTransforms().clear();
                rotationGroup.getTransforms().add(new Rotate(rotationX, Rotate.X_AXIS));
                rotationGroup.getTransforms().add(new Rotate(rotationY, Rotate.Y_AXIS));
            }
            mouseLastX = event.getSceneX();
            mouseLastY = event.getSceneY();
            event.consume();
        });

        subScene.setOnMouseReleased(event -> {
            dragging = false;
            event.consume();
        });
    }

    private void onSimulationUpdate() {
        if (modulateController == null) return;

        int M = modulateController.getCountPoints();
        Modulation modulation = modulateController.getModulation();

        Complex[] ref = generateReferenceConstellation(modulation.toString() + " " + M);
        int symbolCount1 = 1000;
        int[] d = new int[symbolCount1];
        for (int i = 0; i < symbolCount1; i++) {
            d[i] = 1 + (int) (Math.random() * M);
        }

        // === 3. Модуляция + шум ===
        Complex[] received = modulate(modulateController.getModulation(), M, d);
        int symbolCount = 10;
        int[] symbols = new int[symbolCount];
        for (int i = 0; i < symbolCount; i++) {
            symbols[i] = 1 + (int) (Math.random() * M);
        }

        // Очищаем только содержимое констелляции, но сохраняем поворот
        constellationGroup.getChildren().clear();

        PhongMaterial idealMat = new PhongMaterial(Color.WHITE);
        PhongMaterial receivedMat = new PhongMaterial(Color.LIMEGREEN);
        PhongMaterial axisMat = new PhongMaterial(Color.GRAY);
        PhongMaterial timeAxisMat = new PhongMaterial(Color.WHITE);

        // Ось времени (X)
        Box timeAxis = new Box(SYMBOL_SPACING * symbolCount, AXIS_THICKNESS, AXIS_THICKNESS);
        timeAxis.setTranslateX(SYMBOL_SPACING * (symbolCount - 1) / 2.0);
        timeAxis.setMaterial(timeAxisMat);
        constellationGroup.getChildren().add(timeAxis);

        List<Group> labels = new ArrayList<>();
        Double prevIdealX = null, prevIdealY = null, prevIdealZ = null;

        for (int i = 0; i < symbolCount; i++) {
            int symIndex = symbols[i] - 1;
            Complex idealPoint = ref[symIndex];
            Complex receivedPoint = received != null && i < received.length ?
                    received[i] : idealPoint;

            double x = i * SYMBOL_SPACING;
            // Масштабируем координаты для лучшего отображения
            double idealI = idealPoint.real() * SCALE_FACTOR;
            double idealQ = idealPoint.imag() * SCALE_FACTOR;
            double receivedI = receivedPoint.real() * SCALE_FACTOR;
            double receivedQ = receivedPoint.imag() * SCALE_FACTOR;

            // Ось I (Y) и Q (Z) в текущей позиции
            Box iAxis = new Box(AXIS_THICKNESS, AXIS_LENGTH * 2, AXIS_THICKNESS);
            iAxis.setTranslateX(x);
            iAxis.setMaterial(axisMat);
            constellationGroup.getChildren().add(iAxis);

            Box qAxis = new Box(AXIS_THICKNESS, AXIS_THICKNESS, AXIS_LENGTH * 2);
            qAxis.setTranslateX(x);
            qAxis.setMaterial(axisMat);
            constellationGroup.getChildren().add(qAxis);

            // Точка идеальной констелляции (белая)
            Sphere idealDot = new Sphere(DOT_RADIUS);
            idealDot.setTranslateX(x);
            idealDot.setTranslateY(-idealI);  // Отрицательное значение для правильной ориентации
            idealDot.setTranslateZ(idealQ);
            idealDot.setMaterial(idealMat);
            constellationGroup.getChildren().add(idealDot);

            // Точка полученной констелляции (зеленая)
            Sphere receivedDot = new Sphere(DOT_RADIUS);
            receivedDot.setTranslateX(x);
            receivedDot.setTranslateY(-receivedI);
            receivedDot.setTranslateZ(receivedQ);
            receivedDot.setMaterial(receivedMat);
            constellationGroup.getChildren().add(receivedDot);

            // Подпись - создаем в 3D-пространстве
            String bitLabel = Integer.toBinaryString(symIndex);
            int bitsPerSymbol = (int) Math.ceil(Math.log(M) / Math.log(2));
            while (bitLabel.length() < bitsPerSymbol) {
                bitLabel = "0" + bitLabel;
            }

            // Создаем группу для подписи в 3D-пространстве
            Group labelGroup = new Group();
            labelGroup.setTranslateX(x);
            labelGroup.setTranslateY(-idealI + LABEL_OFFSET);
            labelGroup.setTranslateZ(idealQ);

            Text label = new Text(bitLabel);
            label.setFill(Color.YELLOW);
            label.setFont(javafx.scene.text.Font.font("Monospaced", 10));
            labelGroup.getChildren().add(label);

            // Добавляем подпись в 3D-сцену
            constellationGroup.getChildren().add(labelGroup);
            labels.add(labelGroup);

            // Соединяем идеальные точки последовательно
//            if (prevIdealX != null) {
//                double dx = x - prevIdealX;
//                double dy = -idealI - (-prevIdealY * SCALE_FACTOR);
//                double dz = idealQ - (prevIdealZ * SCALE_FACTOR);
//                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
//
//                if (length > 0.001) {
//                    Box connector = new Box(AXIS_THICKNESS, AXIS_THICKNESS, length);
//                    connector.setTranslateX((x + prevIdealX) / 2);
//                    connector.setTranslateY((-idealI + (-prevIdealY * SCALE_FACTOR)) / 2);
//                    connector.setTranslateZ((idealQ + (prevIdealZ * SCALE_FACTOR)) / 2);
//
//                    double angleY = Math.atan2(dz, dx);
//                    double angleX = Math.atan2(Math.sqrt(dx * dx + dz * dz), dy);
//
//                    connector.getTransforms().addAll(
//                            new Rotate(Math.toDegrees(angleY), Rotate.Y_AXIS),
//                            new Rotate(Math.toDegrees(angleX), Rotate.X_AXIS)
//                    );
//
//                    connector.setMaterial(idealMat);
//                    constellationGroup.getChildren().add(connector);
//                }
//            }

            prevIdealX = x;
            prevIdealY = idealPoint.real();
            prevIdealZ = idealPoint.imag();
        }
    }
}