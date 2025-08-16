package ru.railcom.desktop.module;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.chart.*;
import javafx.scene.layout.Pane;


public class SignalChartPane extends Pane {
    private final XYChart.Series<String, Number> signalSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> downlinkSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> uplinkSeries = new XYChart.Series<>();

    private int tick = 0;
    private final int MAX_DATA_POINTS = 200; // Увеличено для возможности прокрутки

    private final DoubleProperty downlinkProperty = new SimpleDoubleProperty(0);
    private final DoubleProperty uplinkProperty = new SimpleDoubleProperty(0);

    public SignalChartPane() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Время");
        yAxis.setLabel("Значение");
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Сигнал и связь");
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setPrefWidth(MAX_DATA_POINTS * 2); // Увеличенная ширина для прокрутки

        signalSeries.setName("Сила сигнала (%)");
        downlinkSeries.setName("Downlink (Mbps)");
        uplinkSeries.setName("Uplink (Mbps)");

        chart.getData().addAll(signalSeries, downlinkSeries, uplinkSeries);

        chart.setStyle(
                ".default-color0.chart-series-line { -fx-stroke: #4FC3F7; -fx-stroke-width: 2; }" +
                        ".default-color1.chart-series-line { -fx-stroke: #FF5252; -fx-stroke-width: 2; }" +
                        ".default-color2.chart-series-line { -fx-stroke: #69F0AE; -fx-stroke-width: 2; }" +
                        ".chart-plot-background { -fx-background-color: #1e1e1e; }" +
                        ".axis { -fx-tick-label-fill: #e0e0e0; }" +
                        ".chart-title { -fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #e0e0e0; }" +
                        ".chart-legend { -fx-text-fill: #e0e0e0; }"
        );

        this.getChildren().add(chart);
        this.setMinSize(1000, 300);
        this.setPrefSize(MAX_DATA_POINTS * 2, 300); // Увеличенная ширина
    }

    public void update(double signalStrength, double downlink, double uplink) {
        Platform.runLater(() -> {
            // Ограничиваем количество точек
            if (tick >= MAX_DATA_POINTS) {
                signalSeries.getData().remove(0);
                downlinkSeries.getData().remove(0);
                uplinkSeries.getData().remove(0);
            }

            String tickLabel = String.valueOf(tick);
            signalSeries.getData().add(new XYChart.Data<>(tickLabel, signalStrength));
            downlinkSeries.getData().add(new XYChart.Data<>(tickLabel, downlink));
            uplinkSeries.getData().add(new XYChart.Data<>(tickLabel, uplink));

            downlinkProperty.set(downlink);
            uplinkProperty.set(uplink);

            tick++;
        });
    }

    public DoubleProperty downlinkProperty() {
        return downlinkProperty;
    }

    public DoubleProperty uplinkProperty() {
        return uplinkProperty;
    }
}