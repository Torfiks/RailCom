package ru.railcom.desktop.ui.chart;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;
import ru.railcom.desktop.module.BaseStation;
import ru.railcom.desktop.simulation.EventBus;
import ru.railcom.desktop.simulation.SimulationController;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SignalChartController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private LineChart<String, Number> chart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private final XYChart.Series<String, Number> signalLossSeries = new XYChart.Series<>();

    private int tick = 0;
    private final int MAX_DATA_POINTS = 100;

    private final DoubleProperty signalLossProperty = new SimpleDoubleProperty(0);

    @Setter
    private SimulationController simulationController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootPane.getStylesheets().add(getClass().getResource("/css/signal_chart.css").toExternalForm());

        // Устанавливаем имена серий

        signalLossSeries.setName("Потери сигнала (дБ)");

        // Добавляем серии на график
        chart.getData().add(signalLossSeries);

        // Устанавливаем стиль
        chart.getStyleClass().add("signal-chart");

        EventBus.getInstance().subscribe(this::startSimulation);

        // Устанавливаем диапазон оси Y (примерный диапазон потерь)
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(150);
        yAxis.setTickUnit(20);
    }

    public void startSimulation() {
        if (simulationController == null) {
            return;
        }

        double loss = moduleOkumuraHata(); // Теперь это потери

        Platform.runLater(() -> {
            // Ограничиваем количество точек
            if (tick >= MAX_DATA_POINTS) {
                signalLossSeries.getData().remove(0);
            }

            String trainPosition = String.valueOf(simulationController.getTrainPosition());
            signalLossSeries.getData().add(new XYChart.Data<>(trainPosition, loss));
            if(simulationController.getTrainPosition() % 9 == 0){

                signalLossProperty.set(loss);
            }

            tick++;
        });
    }

    public double moduleOkumuraHata(){
        double distention = getDistention();
        double f = 800;
        double hte = 10;
        double hre = 5;
        String area = simulationController.getTypeArea();

        // Корректирующий фактор a(Hre)
        double a = (1.1 * Math.log10(f) - 0.7) * hre - (1.56 * Math.log10(f) - 0.8);

        // Базовая формула для городской среды
        double L = 69.55 + 16 * Math.log10(f) - 13.83 * Math.log10(hte) - a - (44.9 - 6.55 * Math.log10(hte)) * Math.log10(distention);

        return switch (area) {
            case "urban" -> L;
            case "suburban" -> L - 2 * Math.pow(Math.log10(f / 28), 2) - 5.4;
            case "rural" -> L - 4.78 * Math.pow(Math.log10(f), 2) - 18.33 * Math.log10(f) - 40.98;
            default -> 0;
        };
    }

    private double getDistention() {
        double distention = 0;

        List<BaseStation> baseStationList = simulationController.getBaseStations();
        double trainPosition = simulationController.getTrainPosition();

        for (int i = 0; i <= baseStationList.size()-2; i++){

            double bs1 = baseStationList.get(i).getPosition();
            double bs2 = baseStationList.get(i+1).getPosition();

            if(bs1 <= trainPosition && trainPosition <= bs2){

                if (Math.abs(bs1 -  trainPosition) < Math.abs(bs2 -  trainPosition)){
                    distention = trainPosition - bs1 ;
                }
                else {
                    distention = bs2- trainPosition;
                }
            }

        }

        if (distention <= 0) {
            return 0;
        }

        return distention;
    }

}