package ru.railcom.desktop.ui.chart;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;
import ru.railcom.desktop.modulation.Complex;
import ru.railcom.desktop.modulation.Modulate;
import ru.railcom.desktop.modulation.ModulateController;
import ru.railcom.desktop.modulation.Modulation;
import ru.railcom.desktop.noise.AwgnNoise;
import ru.railcom.desktop.simulation.EventBus;
import ru.railcom.desktop.simulation.SimulationController;

import java.net.URL;
import java.util.ResourceBundle;

public class BERChartController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private LineChart<Number, Number> chart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private final XYChart.Series<Number, Number> simSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> theorySeries = new XYChart.Series<>();

    @Setter
    private SimulationController simulationController;
    @Setter
    private ModulateController modulateController;

    // Параметры симуляции
    private static final int SYMBOL_COUNT = 200_000; // ~1M бит для BPSK
    private static final double[] EB_N0_DB_VALUES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootPane.getStylesheets().add(getClass().getResource("/css/signal_chart.css").toExternalForm());

        xAxis.setLabel("Eb/N₀ (dB)");
        yAxis.setLabel("Error Rate");
        yAxis.setAutoRanging(true);
        yAxis.setLowerBound(1e-10);
        yAxis.setUpperBound(1e-1);

        simSeries.setName("Simulation");
        theorySeries.setName("Theory");

        chart.getData().addAll(simSeries, theorySeries);
        chart.setAnimated(false);

        EventBus.getInstance().subscribe(this::startSimulation);
    }

    public void startSimulation() {
        if (simulationController == null) return;

        new Thread(this::runSimulation).start();
    }

    private void runSimulation() {
        Modulation modulation = modulateController.getModulation();
        int M = modulateController.getCountPoints();

        double[] simErrors = new double[EB_N0_DB_VALUES.length];
        double[] theoryErrors = new double[EB_N0_DB_VALUES.length];

        // Генерация случайных символов (1-based, как в ваших модуляторах)
        int[] symbols = new int[SYMBOL_COUNT];
        for (int i = 0; i < SYMBOL_COUNT; i++) {
            symbols[i] = 1 + (int) (Math.random() * M);
        }

        // Модуляция — получаем эталонный сигнал (без шума)
        Complex[] txSignal = Modulate.modulate(modulation, M, symbols);

        // Нормализация энергии на символ = 1
        double avgEnergy = 0.0;
        for (Complex c : txSignal) {
            avgEnergy += c.squaredAbs();
        }
        avgEnergy /= txSignal.length;

        double scale = 1.0 / Math.sqrt(avgEnergy);
        for (int i = 0; i < txSignal.length; i++) {
            txSignal[i] = new Complex(txSignal[i].real() * scale, txSignal[i].imag() * scale);
        }


        int bitsPerSymbol = (int) Math.round(Math.log(M) / Math.log(2));
        boolean useBER = (modulation == Modulation.PSK && M == 2); // Только BPSK — точный BER

        for (int i = 0; i < EB_N0_DB_VALUES.length; i++) {
            double ebN0dB = EB_N0_DB_VALUES[i];

            // Пересчёт Eb/N0 → Es/N0
            double esN0dB = ebN0dB + 10.0 * Math.log10(bitsPerSymbol);

            // Используем ваш AwgnNoise с Es/N0
            Complex[] rxSignal = AwgnNoise.addAwgnNoise(txSignal, esN0dB);

            // Демодуляция
            int[] detected = Modulate.demodulate(modulation, M, rxSignal);

            // Подсчёт ошибок
            int symbolErrors = 0;
            for (int j = 0; j < symbols.length; j++) {
                if (symbols[j] != detected[j]) {
                    symbolErrors++;
                }
            }

            double ser = (double) symbolErrors / SYMBOL_COUNT;
            double ber = useBER ? ser : ser / bitsPerSymbol; // для BPSK SER = BER

            simErrors[i] = useBER ? ber : ser;
            theoryErrors[i] = useBER
                    ? computeTheoreticalBER_BPSK(ebN0dB)
                    : computeTheoreticalSER(modulation, M, esN0dB);
        }


        Platform.runLater(() -> {
            simSeries.getData().clear();
            theorySeries.getData().clear();
            for (int i = 0; i < EB_N0_DB_VALUES.length; i++) {
                if (simErrors[i] > 0) {
                    simSeries.getData().add(new XYChart.Data<>(EB_N0_DB_VALUES[i], simErrors[i]));
                }
                if (theoryErrors[i] > 0) {
                    theorySeries.getData().add(new XYChart.Data<>(EB_N0_DB_VALUES[i], theoryErrors[i]));
                }
            }
        });
    }

    // === Теоретические формулы ===

    private double computeTheoreticalBER(Modulation modulation, int M, double ebN0dB) {
        if (modulation == Modulation.PSK) {
            if (M == 2) { // BPSK
                double ebN0 = Math.pow(10, ebN0dB / 10.0);
                return 0.5 * complementaryErrorFunction(Math.sqrt(ebN0));
            } else if (M == 4) { // QPSK = 2xBPSK
                double ebN0 = Math.pow(10, ebN0dB / 10.0);
                return complementaryErrorFunction(Math.sqrt(ebN0)) - 0.5 * Math.pow(complementaryErrorFunction(Math.sqrt(ebN0)), 2);
            }
        }
        return 0.0;
    }

    private double computeTheoreticalBER_BPSK(double ebN0dB) {
        double ebN0 = Math.pow(10, ebN0dB / 10.0);
        return 0.5 * complementaryErrorFunction(Math.sqrt(ebN0));
    }

    private double computeTheoreticalSER(Modulation modulation, int M, double esN0dB) {
        double esN0 = Math.pow(10, esN0dB / 10.0);
        if (modulation == Modulation.QAM) {
            int sqrtM = (int) Math.sqrt(M);
            if (sqrtM * sqrtM != M) return 0.0; // не квадратная QAM

            double p = 2 * (1 - 1.0 / sqrtM) * complementaryErrorFunction(Math.sqrt(esN0 * 3.0 / (2.0 * (M - 1))));
            return p - (p * p) / 4.0;
        } else if (modulation == Modulation.PAM) {
            double p = 2 * (1 - 1.0 / M) * complementaryErrorFunction(Math.sqrt(esN0 * 6.0 / (M * M - 1)));
            return p;
        }
        return 0.0;
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