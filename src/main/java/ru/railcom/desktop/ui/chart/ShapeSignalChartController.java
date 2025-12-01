package ru.railcom.desktop.ui.chart;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Setter;
import ru.railcom.desktop.modulation.Complex;
import ru.railcom.desktop.modulation.ModulateController;
import ru.railcom.desktop.simulation.EventBus;

import java.net.URL;
import java.util.ResourceBundle;

public class ShapeSignalChartController implements Initializable {

    @FXML
    public Pane shapeSignalChatPane;

    @FXML
    private Canvas canvas;

    // --- Параметры сигнала ---
    private int[] symbols = null; // 1-based
    private int M = 0;
    private String modulationType = "";

    // --- Параметры отображения ---
    private double timeScale = 1.0; // пикселей на условную единицу времени
    private double offsetX = 0.0;   // сдвиг по времени (в условных единицах)
    private double lastMouseX = 0;
    private boolean dragging = false;

    // --- Физические параметры ---
    private static final double Tb = 1.0; // длительность одного символа
    private static final double fc = 1.0; // частота несущей (Гц)

    @Setter
    private ModulateController modulateController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        canvas.widthProperty().bind(shapeSignalChatPane.widthProperty());
        canvas.heightProperty().set(500);

        EventBus.getInstance().subscribe(this::onSimulationUpdate);

        canvas.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
    }

    private void onSimulationUpdate() {
        if (modulateController == null) return;

        M = modulateController.getCountPoints();
        modulationType = modulateController.getModulation().toString();

        int symbolCount = Math.max(10, M); // для наглядности — меньше символов
        symbols = new int[symbolCount];
        for (int i = 0; i < symbolCount; i++) {
            symbols[i] = 1 + (int) (Math.random() * M);
        }

        // Сброс масштаба при изменении параметров
        offsetX = 0;
        timeScale = 1.0;
        redraw();
    }

    private String symbolToBitLabel(int symbol, int M) {
        int index = symbol - 1;
        String bits = Integer.toBinaryString(index);
        int bitsPerSymbol = (int) Math.ceil(Math.log(M) / Math.log(2));
        while (bits.length() < bitsPerSymbol) {
            bits = "0" + bits;
        }
        return bits;
    }

    private double getSignalValue(double t) {
        // Найти символ, которому принадлежит время t
        int symbolIndex = (int) (t / Tb);
        if (symbolIndex < 0 || symbolIndex >= symbols.length) {
            return 0.0;
        }

        // Преобразуем символ (1-based) в индекс (0-based)
        int sym0Based = symbols[symbolIndex] - 1;

        // Генерируем строку типа "PSK 2", "QAM 16"
        String modStr = modulationType + " "+M;
        Complex[] constellation = ru.railcom.desktop.modulation.Modulate.generateReferenceConstellation(modStr);

        if (sym0Based < 0 || sym0Based >= constellation.length) {
            return 0.0;
        }

        Complex point = constellation[sym0Based];
        double I = point.real();
        double Q = point.imag();

        // Время внутри символа
        double tInSymbol = t - symbolIndex * Tb;

        // Для PAM: Q = 0 → остаётся только I·cos(...)
        if ("PAM".equals(modulationType)) {
            return I * Math.cos(2.0 * Math.PI * fc * tInSymbol);
        }
        // Для PSK: точки лежат на окружности → можно использовать фазу
        else if ("PSK".equals(modulationType)) {
            double phase = Math.atan2(Q, I); // фаза символа
            return Math.cos(2.0 * Math.PI * fc * tInSymbol + phase);
            // или: return I * Math.cos(...) - Q * Math.sin(...);
        }
        // Для QAM и остальных — квадратурная модуляция
        else {
            // s(t) = I·cos(ωt) – Q·sin(ωt)
            double omega_t = 2.0 * Math.PI * fc * tInSymbol;
            return I * Math.cos(omega_t) - Q * Math.sin(omega_t);
        }
    }
    private void redraw() {
        if (symbols == null || symbols.length == 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        double centerY = height / 2;
        double vertScale = height * 0.3; // амплитуда ±0.3*высоты

        // Ось времени
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeLine(0, centerY, width, centerY);

        // Рисуем сигнал как непрерывную кривую
        gc.setStroke(Color.LIME);
        gc.setLineWidth(1.5);

        double prevX = -1, prevY = -1;
        double step = 0.01; // шаг по времени для гладкости

        for (double t = 0; t <= symbols.length * Tb; t += step) {
            double x = (t - offsetX) * timeScale;
            if (x < -10 || x > width + 10) continue;

            double y = centerY - getSignalValue(t) * vertScale;

            if (prevX >= 0) {
                gc.strokeLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }

        // === Отображение битов и границ символов ===
        gc.setFont(Font.font("Monospaced", 100));
        gc.setFill(Color.YELLOW);

        double labelY = centerY + 30;

        for (int i = 0; i < symbols.length; i++) {
            double xStart = (i * Tb - offsetX) * timeScale;
            double xEnd = ((i + 1) * Tb - offsetX) * timeScale;

            if (xStart > width || xEnd < 0) continue;

            // Вертикальная линия в начале символа
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(0.5);
            gc.strokeLine(xStart, 0, xStart, height);

            // Подпись бита
            String label = symbolToBitLabel(symbols[i], M);
            double labelX = (xStart + xEnd) / 2.0;
            double textWidth = label.length() * 6;
            gc.fillText(label, (float)(labelX - textWidth / 2), (float)labelY);
        }

        // Заголовок
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("BPSK Signal (Tb = 1, fc = " + fc + " Hz)", 10, 10);
    }

    // === Обработка событий мыши ===
    private void handleZoom(ScrollEvent event) {
        double delta = event.getDeltaY();
        // Чувствительность: можно уменьшить factor для плавности
        double factor = delta > 0 ? 1.15 : 1 / 1.15;
        timeScale *= factor;

        // Разрешаем сильное увеличение (до 500 пикселей на Tb)
        timeScale = Math.max(0.05, Math.min(timeScale, 500.0));

        redraw();
        event.consume();
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            dragging = true;
            lastMouseX = event.getX();
            canvas.setCursor(javafx.scene.Cursor.HAND);
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (dragging) {
            double dx = event.getX() - lastMouseX;
            offsetX -= dx / timeScale;
            lastMouseX = event.getX();
            redraw();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            dragging = false;
            canvas.setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }
}