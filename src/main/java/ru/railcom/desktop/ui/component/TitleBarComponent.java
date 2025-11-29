package ru.railcom.desktop.ui.component;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Setter;
import ru.railcom.desktop.ui.control.ControlPanelController;

import java.net.URL;
import java.util.ResourceBundle;

import static ru.railcom.desktop.ui.value.NameField.*;

public class TitleBarComponent implements Initializable {

    @FXML
    public BorderPane titleBar;
    @FXML
    public Label titleLabel;
    @FXML
    public HBox buttonsBox;
    @FXML
    public Button minimizeButton;
    @FXML
    public Button maximizeButton;
    @FXML
    public Button closeButton;
    @FXML
    public Button pauseButton;
    @FXML
    public Button startButton;

    private double xOffset = 0;
    private double yOffset = 0;

    private Stage stage;

    @Setter
    private ControlPanelController controlPanelController; // <-- Добавлено

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleBar.getStylesheets().add(getClass().getResource("/css/title_bar.css").toExternalForm());

        setupButtons();
        setupDrag();
    }

    public void setup(Stage stage) {
        this.stage = stage;
        setupButtons();
        setupDrag();
    }

    private void setupButtons() {
        if (minimizeButton != null) {
            minimizeButton.setTooltip(new Tooltip(minimizeTooltip));
            minimizeButton.setOnAction(e -> stage.setIconified(true));
        }

        if (maximizeButton != null) {
            maximizeButton.setTooltip(new Tooltip(maximizeTooltip));
            maximizeButton.setOnAction(e -> {
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                    maximizeButton.setText(maximizeTooltipItem);
                    maximizeButton.setTooltip(new Tooltip(maximizeTooltip));
                } else {
                    stage.setMaximized(true);
                    maximizeButton.setText(restoreTooltipItem);
                    maximizeButton.setTooltip(new Tooltip(restoreTooltip));
                }
            });
        }

        if (closeButton != null) {
            closeButton.setTooltip(new Tooltip(closeTooltip));
            closeButton.setOnAction(e -> stage.close());
        }

        startButton.setOnAction(e -> {
            controlPanelController.startSimulation();
        });
        pauseButton.setOnAction(e -> {
            controlPanelController.startSimulation();
        });
    }

    private void setupDrag() {
        if (titleBar != null && stage != null) {
            titleBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            titleBar.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
        }
    }

}