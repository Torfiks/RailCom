package ru.railcom.desktop.ui.setting;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Setter;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import ru.railcom.desktop.ui.control.SimulationController;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingComponent implements Initializable {

    @FXML
    public VBox settingPanel;
    @FXML
    public TextField powerSenderField;
    @FXML
    public TextField powerResenderField;
    @FXML
    public TextField lossesField;

    @Setter
    private SimulationController simulationController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        settingPanel.getStylesheets().add(getClass().getResource("/css/setting-modulation.css").toExternalForm());
    }

}
