package ru.railcom.desktop.ui.chart;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;
import ru.railcom.desktop.ui.control.EventBus;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class BERChartController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private LineChart<Number, Number> chart; // теперь числовые оси
    @FXML
    private NumberAxis xAxis; // Eb/N0 (dB)
    @FXML
    private NumberAxis yAxis; // BER

    private final XYChart.Series<Number, Number> simBerSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> theoryBerSeries = new XYChart.Series<>();

    @Setter
    private SignalChartController signalChartController;

    private static final int N = 100_000; // уменьшено для скорости (можно 1M)
    private static final double[] EB_N0_DB_VALUES = new double[14]; // -3 до 10

    static {
        for (int i = 0; i < EB_N0_DB_VALUES.length; i++) {
            EB_N0_DB_VALUES[i] = i;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootPane.getStylesheets().add(getClass().getResource("/css/signal_chart.css").toExternalForm());

        // Настраиваем оси
        xAxis.setLabel("Eb/N₀ (dB)");
        yAxis.setLabel("BER");
//        yAxis.setLogarithmic(true); // BER логарифмический (важно!)
        yAxis.setUpperBound(2);
        yAxis.setLowerBound(1e-8);

        // Настраиваем серии
        simBerSeries.setName("Simulated BER");
        theoryBerSeries.setName("Theoretical BER");

        chart.getData().addAll(simBerSeries, theoryBerSeries);
        chart.setAnimated(false); // ускорение отрисовки

        EventBus.getInstance().subscribe(this::startSimulation);
    }

    public void startSimulation() {
        if (signalChartController == null) return;

        double pathLossDb = signalChartController.moduleOkumuraHata();

        // Вычисляем BER-кривую с учётом потерь
        var result = modulationWithLoss(0);

        Platform.runLater(() -> {
            simBerSeries.getData().clear();
            theoryBerSeries.getData().clear();

            for (int i = 0; i < EB_N0_DB_VALUES.length; i++) {
                double x = EB_N0_DB_VALUES[i];
                double ySim = result.simBer[i];
                double yTheory = result.theoryBer[i];

                // Не отображаем нулевые BER (меньше 1e-30) — график ломается
                if (ySim > 0) {
                    simBerSeries.getData().add(new XYChart.Data<>(x, ySim));
                }
                if (yTheory > 0) {
                    theoryBerSeries.getData().add(new XYChart.Data<>(x, yTheory));
                }
            }
        });
    }

    // Вспомогательный класс для возврата двух массивов
    private static class BERResult {
        final double[] simBer;
        final double[] theoryBer;
        BERResult(double[] sim, double[] theory) {
            this.simBer = sim;
            this.theoryBer = theory;
        }
    }

    private BERResult modulationWithLoss(double pathLossDb) {
        Random rand = new Random(100);
        Random randn = new Random(200);

        boolean[] ip = new boolean[N];
        for (int i = 0; i < N; i++) {
            ip[i] = rand.nextDouble() > 0.5;
        }

        double[] s = new double[N];
        for (int i = 0; i < N; i++) {
            s[i] = ip[i] ? 1.0 : -1.0;
        }

        int[] nErr = new int[EB_N0_DB_VALUES.length];

        for (int ii = 0; ii < EB_N0_DB_VALUES.length; ii++) {
            double effectiveEbN0dB = EB_N0_DB_VALUES[ii] - pathLossDb;
            if (effectiveEbN0dB < -20) effectiveEbN0dB = -20;

            double noiseFactor = Math.pow(10, -effectiveEbN0dB / 20.0);

            int errors = 0;
            for (int i = 0; i < N; i++) {
                double noise = randn.nextGaussian() / Math.sqrt(2);
                double y = s[i] + noiseFactor * noise;
                boolean ipHat = y > 0;
                if (ip[i] != ipHat) errors++;
            }
            nErr[ii] = errors;
        }

        double[] simBer = new double[EB_N0_DB_VALUES.length];
        double[] theoryBer = new double[EB_N0_DB_VALUES.length];

        for (int i = 0; i < EB_N0_DB_VALUES.length; i++) {
            simBer[i] = (double) nErr[i] / N;

            double effectiveEbN0dB = EB_N0_DB_VALUES[i] - pathLossDb;
            if (effectiveEbN0dB < -10) {
                theoryBer[i] = 0.5;
            } else {
                double snr = Math.pow(10, effectiveEbN0dB / 10.0);
                theoryBer[i] = 0.5 * complementaryErrorFunction(Math.sqrt(snr));
            }
        }

        return new BERResult(simBer, theoryBer);
    }

    public static double complementaryErrorFunction(double x) {
        if (x < 0) return 2.0 - complementaryErrorFunction(-x);
        if (x == 0) return 1.0;
        if (x > 6) return 0.0;
        double t = 1.0 / (1.0 + 0.3275911 * x);
        double poly = t * (0.254829592
                + t * (-0.284496736
                + t * (1.421413741
                + t * (-1.453152027
                + t * 1.061405429))));
        return poly * Math.exp(-x * x);
    }
}