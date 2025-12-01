package ru.railcom.desktop.ui.setting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Setter;
import ru.railcom.desktop.modulation.ModulateController;
import ru.railcom.desktop.modulation.Modulation;
import ru.railcom.desktop.simulation.EventBus;
import ru.railcom.desktop.simulation.SimulationController;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingComponent implements Initializable {

    @FXML
    public VBox settingPanel;
    @FXML
    public ComboBox<String> modulationList;
    @FXML
    public TextField countPoints;
    @FXML
    public TextField powerSenderField;
    @FXML
    public TextField powerResenderField;
    @FXML
    public TextField lossesField;
    @FXML
    public Button startButton;

    @Setter
    private ModulateController modulateController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        settingPanel.getStylesheets().add(getClass().getResource("/css/setting-modulation.css").toExternalForm());
        setComboBox();

        EventBus.getInstance().subscribe(this::onSimulationUpdate);

    }

    private void setComboBox(){
        ObservableList<String> modulations = FXCollections.observableArrayList(
                "PSK",
                "QAM",
                "PAM",
                "FSK"
        );

        modulationList.setItems(modulations);
        modulationList.getSelectionModel().selectFirst();
    }

    private void onSimulationUpdate() {
        try {
            String modulation = modulationList.getSelectionModel().getSelectedItem();
            Integer count = Integer.parseInt(countPoints.getText());

            modulateController.setModulation(Modulation.valueOf(modulation));
            modulateController.setCountPoints(count);

        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

}
