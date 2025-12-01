package ru.railcom.desktop.ui.modulation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.net.URL;
import java.util.ResourceBundle;

public class Modulation3DController implements Initializable {

    @FXML
    public Pane modulation3DContainer;

    private Group root3D;
    private SubScene subScene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root3D = new Group();

        subScene = new SubScene(root3D, 0, 0, true, SceneAntialiasing.BALANCED);
//        subScene.widthProperty().bind(modulation3DContainer.widthProperty());
        subScene.widthProperty().set(400);
        subScene.heightProperty().set(400);
//
        // Добавляем SubScene в контейнер
//        modulation3DContainer.getChildren().add(subScene);
        // Устанавливаем стили
        modulation3DContainer.getStylesheets().add(getClass().getResource("/css/modulation/modulation_3d.css").toExternalForm());
        PerspectiveCamera camera = setupCamera();
        subScene.setCamera(camera);
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
//        camera.setTranslateZ(-cameraDistance);
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
}
