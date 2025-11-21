package ru.railcom.desktop.ui.control;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

import static ru.railcom.desktop.ui.value.NameField.simulationIsNotRunning;
import static ru.railcom.desktop.ui.value.NameField.simulationIsRunning;

public class ControlPanelController implements Initializable {

    @FXML
    private TextField speedField;
    @FXML
    private TextField baseStationsField;
    @FXML
    private TextField distanceField;
    @FXML
    private Button startButton;

    private SimulationController simulationController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startButton.setOnAction(e -> {
            boolean isRunning = simulationController.isRunning();
            simulationController.setRunning(!isRunning);

            startButton.setText(simulationController.isRunning() ? simulationIsNotRunning : simulationIsRunning);

            try {
                double speed = Double.parseDouble(speedField.getText());
                int stations = Integer.parseInt(baseStationsField.getText());
                double distance = Double.parseDouble(distanceField.getText());

                // Обновляем дистанцию и пересчитываем позиции
                double oldDistance = simulationController.getTrack();
                simulationController.setTrack(distance);

                if (oldDistance > 0) {
                    // Пересчитываем позиции станций пропорционально новому расстоянию
                    if (simulationController.getBaseStations() != null) {
                        for (var station : simulationController.getBaseStations()) {
                            double oldPos = station.getPosition();
                            double ratio = oldPos / oldDistance;
                            station.setPosition(distance * ratio);
                        }
                    }
                    // Пересчитываем позицию поезда
                    double oldTrainPos = simulationController.getTrainPosition();
                    double trainRatio = oldTrainPos / oldDistance;
                    simulationController.setTrainPosition(distance * trainRatio);
                }

                simulationController.setCurrentSpeed(speed);
                simulationController.setCountBaseStations(stations);

                // Публикуем событие обновления
                EventBus.getInstance().publish();

            } catch (NumberFormatException ex) {
                System.err.println("Неверный формат данных: " + ex.getMessage());
            }
        });
    }

    public void setup(SimulationController simulationController) {
        this.simulationController = simulationController;
    }
}