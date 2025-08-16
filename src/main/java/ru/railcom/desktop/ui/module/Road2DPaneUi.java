package ru.railcom.desktop.ui.module;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import ru.railcom.desktop.dto.BaseStation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Road2DPaneUi {
    private static final int DESIRED_WIDTH = 50;
    private static final int DESIRED_HEIGHT = 30;
    private static final int BASE_STATION_WIDTH = 30;
    private static final int BASE_STATION_HEIGHT = 30;

    private static double trainPosition = 0;
    private static double totalTrackLength = 0;

    private static Image trainImage;
    private static Image baseStationImage;

    private static List<BaseStation> stations = new ArrayList<>();
    public Road2DPaneUi(){
        try {
            trainImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/train.png")));
            baseStationImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/base_station.png")));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить изображение поезда");
            e.printStackTrace();
        }
    }

    public void setData(List<BaseStation> stations, double trainPosition,  double totalTrackLength) {
        Road2DPaneUi.stations = stations;
        Road2DPaneUi.trainPosition = trainPosition;
        Road2DPaneUi.totalTrackLength = totalTrackLength;
    }

    public void draw(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Путь
        gc.setStroke(Color.GRAY);
        gc.strokeLine(0, 100, 900, 100);

        // Базовые станции
        drawBaseStations(gc);

        // Поезд — рисуем изображение
        double scale = 900.0 / totalTrackLength;
        int trainX = (int) (trainPosition * scale);

        // Рисуем изображение нужного размера
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

    private static void drawBaseStations(GraphicsContext gc) {
        for (BaseStation station : stations) {
            int x = station.getX();

            if (baseStationImage != null && baseStationImage.getProgress() == 1.0) {
                // Рисуем изображение
                gc.drawImage(baseStationImage,
                        x - (double) BASE_STATION_WIDTH / 2,  // X
                        85,                           // Y
                        BASE_STATION_WIDTH,           // Ширина
                        BASE_STATION_HEIGHT);         // Высота
            } else {
                // Резерв: рисуем синий кружок, если картинки нет
                System.out.println("Базовая станция не загружена");
                gc.setFill(Color.BLUE);
                gc.fillOval(x - 8, 92, 16, 16);
            }
        }
    }
}
