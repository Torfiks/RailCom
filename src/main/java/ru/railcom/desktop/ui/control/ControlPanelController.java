package ru.railcom.desktop.ui.control;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

                simulationController.setCurrentSpeed(speed);
                simulationController.setCountBaseStations(stations);
                simulationController.setTrack(distance);

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