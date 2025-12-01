package ru.railcom.desktop.ui.modulation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Setter;
import ru.railcom.desktop.modulation.Complex;
import ru.railcom.desktop.modulation.ModulateController;
import ru.railcom.desktop.modulation.Modulation;
import ru.railcom.desktop.noise.AwgnNoise;
import ru.railcom.desktop.simulation.EventBus;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static ru.railcom.desktop.modulation.Modulate.*;

public class Modulation2DController implements Initializable {

    @FXML
    public Pane modulation2DContainer;
    @FXML
    public CheckBox showCloudCheckbox;
    @FXML
    private Canvas canvas;

    @Setter
    private ModulateController modulateController;

    private static final double PADDING = 30;
    private static final double AXIS_LENGTH = 20;
    private static final double DOT_RADIUS = 3;
    private static final double LABEL_OFFSET = 10;

    private boolean showReceivedPoints = true; // Управление отображением облака

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modulation2DContainer.getStylesheets().add(getClass().getResource("/css/modulation/modulation_2d.css").toExternalForm());

        canvas.widthProperty().set(500);
        canvas.heightProperty().set(500);

        EventBus.getInstance().subscribe(this::onSimulationUpdate);
    }

    private void onSimulationUpdate() {
        if (modulateController == null) return;

        int M = modulateController.getCountPoints();
        String mod = modulateController.getModulation().toString();
        // Идеальные точки (эталонное созвездие размера M)
        Complex[] referencePoints = generateReferenceConstellation(M+mod);
        System.out.println("ReferencePoints: " + Arrays.toString(referencePoints));

        // === 2. Генерируем случайные символы (1-based) ===
        int symbolCount = 1000; // минимум 100 для облака
        int[] d = new int[symbolCount];
        for (int i = 0; i < symbolCount; i++) {
            d[i] = 1 + (int) (Math.random() * M);
        }

        // === 3. Модуляция + шум ===
        Complex[] idealSignal = modulate(modulateController.getModulation(),M, d);
        Complex[] receivedPoints = AwgnNoise.addAwgnNoise(idealSignal, 16); // SNR = 10 dB
        System.out.println("ReceivedPoints: " + Arrays.toString(receivedPoints));
//        // Демодуляция (если нужно)
//        int[] detected = demodulate(modulateController.getModulation(), M, receivedPoints);
//        System.out.println("Match: " + Arrays.equals(d, detected));

        drawConstellation(receivedPoints, referencePoints);
    }

    private void drawConstellation(Complex[] receivedPoints, Complex[] referencePoints) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        if (referencePoints == null || referencePoints.length == 0) return;

        // === Определяем масштаб на основе эталонных точек ===
        double maxReal = 0, maxImag = 0;
        for (Complex p : referencePoints) {
            maxReal = Math.max(maxReal, Math.abs(p.real()));
            maxImag = Math.max(maxImag, Math.abs(p.imag()));
        }
        // Добавляем небольшой отступ
        double maxCoord = Math.max(maxReal, maxImag) * 1.1;
        if (maxCoord == 0) maxCoord = 1.0;

        double centerX = width / 2;
        double centerY = height / 2;
        double scale = Math.min(
                (width - 2 * PADDING) / (2 * maxCoord),
                (height - 2 * PADDING) / (2 * maxCoord)
        );

        // Оси
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeLine(PADDING, centerY, width - PADDING, centerY);
        gc.strokeLine(centerX, PADDING, centerX, height - PADDING);

        // Стрелки (опционально)
        drawArrow(gc, width - PADDING - AXIS_LENGTH, centerY, width - PADDING, centerY);
        drawArrow(gc, centerX, PADDING, centerX, PADDING + AXIS_LENGTH);

        gc.setFont(Font.font("Arial", 10));
        gc.setFill(Color.WHITE);
        gc.fillText("I", width - PADDING + 5, centerY - 5);
        gc.fillText("Q", centerX - 15, PADDING - 5);

        // === Эталонные точки ===
        for (int i = 0; i < referencePoints.length; i++) {
            Complex p = referencePoints[i];
            double x = centerX + p.real() * scale;
            double y = centerY - p.imag() * scale; // инверсия Y

            if (x >= 0 && x <= width && y >= 0 && y <= height) {
                gc.setFill(Color.WHITE);
                gc.fillOval(x - DOT_RADIUS, y - DOT_RADIUS, 2 * DOT_RADIUS, 2 * DOT_RADIUS);

                // Подписи — только для малых M (чтобы не засорять 64-QAM)
                if (referencePoints.length <= 16) {
                    String label = getBitLabel(i, referencePoints.length);
                    if (label != null) {
                        gc.fillText(label, x + LABEL_OFFSET, y - LABEL_OFFSET);
                    }
                }
            }
        }

        // === Облако принятых точек ===
        if (showReceivedPoints && receivedPoints != null) {
            for (Complex p : receivedPoints) {
                if (p == null) continue;
                double x = centerX + p.real() * scale;
                double y = centerY - p.imag() * scale;
                if (x >= 0 && x <= width && y >= 0 && y <= height) {
                    gc.setFill(Color.GREEN);
                    gc.fillOval(x - DOT_RADIUS, y - DOT_RADIUS, 2 * DOT_RADIUS, 2 * DOT_RADIUS);
                }
            }
        }
    }

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len == 0) return;

        double ux = dx / len, uy = dy / len;
        double size = 8;
        double ax1 = x2 - size * ux + size * 0.5 * uy;
        double ay1 = y2 - size * uy - size * 0.5 * ux;
        double ax2 = x2 - size * ux - size * 0.5 * uy;
        double ay2 = y2 - size * uy + size * 0.5 * ux;

        gc.strokeLine(x2, y2, ax1, ay1);
        gc.strokeLine(x2, y2, ax2, ay2);
    }


    private String getBitLabel(int index, int totalPoints) {
        return Integer.toBinaryString(index);
    }

}