package ru.modulator.desktop.modulator.ui.module;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import ru.modulator.desktop.modulator.dto.BaseStation;

import java.util.List;

public class Train3DSceneUI {

    /**
        Создание поезда и рельс
     */
    public static Group setupSceneContent(Group root3D) {
        // Рельсы
        Cylinder rail1 = new Cylinder(3, 1500);
        rail1.setTranslateY(5);
        rail1.setTranslateZ(15);
        rail1.setMaterial(new PhongMaterial(Color.SILVER));
        Rotate rotation1 = new Rotate(90, Rotate.Z_AXIS);
        rail1.getTransforms().add(rotation1);

        Cylinder rail2 = new Cylinder(3, 1500);
        rail2.setTranslateY(5);
        rail2.setTranslateX(1);
        rail2.setTranslateZ(-15);
        rail2.setMaterial(new PhongMaterial(Color.SILVER));
        Rotate rotation2 = new Rotate(90, Rotate.Z_AXIS);
        rail2.getTransforms().add(rotation2);
        // Шпалы
        for (int i = -700; i < 700; i += 150) {
            Box sleeper = new Box(20, 2, 30);
            sleeper.setTranslateX(i);
            sleeper.setTranslateY(5);
            sleeper.setMaterial(new PhongMaterial(Color.DARKGRAY));
            root3D.getChildren().add(sleeper);
        }

        // Поезд
        Group trainGroup = new Group();
        Box locomotive = new Box(40, 25, 30);
        locomotive.setTranslateX(-580);
        locomotive.setMaterial(new PhongMaterial(Color.web("#FF5252")));

        Box wagon1 = new Box(30, 20, 30);
        wagon1.setTranslateX(-640);
        wagon1.setMaterial(new PhongMaterial(Color.web("#4FC3F7")));

        Box wagon2 = new Box(30, 20, 30);
        wagon2.setTranslateX(-700);
        wagon2.setMaterial(new PhongMaterial(Color.web("#69F0AE")));

        trainGroup.getChildren().addAll(locomotive, wagon1, wagon2);

        // Колеса с анимацией
        for (int i = 0; i < 4; i++) {
            double offset = i % 2 == 0 ? -15 : 15;
            Cylinder wheel = new Cylinder(5, 8, 32);
            wheel.setTranslateX(-580 - 15 + (i / 2) * 30);
            wheel.setTranslateY(12);
            wheel.setTranslateZ(offset);
            wheel.setMaterial(new PhongMaterial(Color.DARKGRAY));
            trainGroup.getChildren().add(wheel);

            RotateTransition rt = new RotateTransition(Duration.seconds(1), wheel);
            rt.setAxis(Rotate.Z_AXIS);
            rt.setByAngle(360);
            rt.setCycleCount(Timeline.INDEFINITE);
            rt.play();
        }

        root3D.getChildren().addAll(rail1, rail2, trainGroup);
        return trainGroup;
    }

    public static void createBaseStationModel(BaseStation station, List<BaseStation> baseStations, Group root3D) {
        // Башня
        Box tower = new Box(15, 60, 15);
        tower.setTranslateX(station.getX());
        tower.setTranslateY(-30);
        tower.setTranslateZ(40);
        tower.setMaterial(new PhongMaterial(Color.web("#B0BEC5")));

        // Антенна (светодиодный эффект)
        Cylinder antenna = new Cylinder(1, 30);
        antenna.setTranslateX(station.getX());
        antenna.setTranslateY(-65);
        antenna.setTranslateZ(40);
        antenna.setMaterial(new PhongMaterial(Color.web("#4CAF50")));

        // Эффект мигания антенны
        ColorAdjust colorAdjust = new ColorAdjust();
        antenna.setEffect(colorAdjust);

        Timeline blinkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(colorAdjust.brightnessProperty(), 0.5)),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(colorAdjust.brightnessProperty(), -0.5))
        );
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
        blinkTimeline.setAutoReverse(true);
        blinkTimeline.play();

        // Основание
        Box base = new Box(40, 5, 40);
        base.setTranslateX(station.getX());
        base.setTranslateY(0);
        base.setTranslateZ(40);
        base.setMaterial(new PhongMaterial(Color.web("#78909C")));

        // Создаем номер станции как 3D-текст через Box
        double numberSize = 5;
        Box stationNumber = new Box(numberSize, numberSize, numberSize);
        stationNumber.setTranslateX(station.getX());
        stationNumber.setTranslateY(75);
        stationNumber.setTranslateZ(40);
        stationNumber.setMaterial(new PhongMaterial(Color.WHITE));

        // Группа для номера станции
        Group numberGroup = new Group();
        numberGroup.setTranslateX(station.getX());
        numberGroup.setTranslateY(-75);
        numberGroup.setTranslateZ(40);

        // Создаем цифры из маленьких кубиков
        int stationIndex = baseStations.indexOf(station) + 1;
        String stationNumberStr = String.valueOf(stationIndex);

        for (int i = 0; i < stationNumberStr.length(); i++) {
            char digitChar = stationNumberStr.charAt(i);
            int digit = Character.getNumericValue(digitChar);

            // Создаем цифру из кубиков
            Group digitGroup = createDigit(digit, i * 8, 0, 0);
            numberGroup.getChildren().add(digitGroup);
        }

        station.setNode(new Group(tower, antenna, base, numberGroup));
        root3D.getChildren().add(station.getNode());
    }

    // Метод для создания 3D-цифры из кубиков
    private static Group createDigit(int digit, double offsetX, double offsetY, double offsetZ) {
        Group digitGroup = new Group();

        // Определяем форму цифры
        switch (digit) {
            case 0:
                addHorizontalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                break;
            case 1:
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 10, 0, offsetX, offsetY, offsetZ);
                break;
            case 2:
                addHorizontalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 10, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 5, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                break;
            case 3:
                addHorizontalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 10, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 5, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                break;
            case 4:
                addVerticalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 10, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 5, 0, offsetX, offsetY, offsetZ);
                break;
            case 5:
                addHorizontalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 5, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                break;
            case 6:
                addHorizontalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 5, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                break;
            case 7:
                addHorizontalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 10, 0, offsetX, offsetY, offsetZ);
                break;
            case 8:
                addHorizontalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 5, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                break;
            case 9:
                addHorizontalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 0, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 10, 10, 0, offsetX, offsetY, offsetZ);
                addVerticalSegment(digitGroup, 0, 10, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 5, 0, offsetX, offsetY, offsetZ);
                addHorizontalSegment(digitGroup, 0, 0, 0, offsetX, offsetY, offsetZ);
                break;
        }

        return digitGroup;
    }

    private static void addHorizontalSegment(Group parent, double x, double y, double z, double offsetX, double offsetY, double offsetZ) {
        Box segment = new Box(8, 1, 1);
        segment.setTranslateX(x + offsetX);
        segment.setTranslateY(y + offsetY);
        segment.setTranslateZ(z + offsetZ);
        segment.setMaterial(new PhongMaterial(Color.WHITE));
        parent.getChildren().add(segment);
    }

    private static void addVerticalSegment(Group parent, double x, double y, double z, double offsetX, double offsetY, double offsetZ) {
        Box segment = new Box(1, 8, 1);
        segment.setTranslateX(x + offsetX);
        segment.setTranslateY(y + offsetY);
        segment.setTranslateZ(z + offsetZ);
        segment.setMaterial(new PhongMaterial(Color.WHITE));
        parent.getChildren().add(segment);
    }

}