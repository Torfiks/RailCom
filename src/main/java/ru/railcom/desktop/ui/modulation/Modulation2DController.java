package ru.railcom.desktop.ui.modulation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class Modulation2DController implements Initializable {

    @FXML
    public Pane modulation2DContainer;
    @FXML
    private Canvas canvas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modulation2DContainer.getStylesheets().add(getClass().getResource("/css/modulation/modulation_2d.css").toExternalForm());

//        canvas.widthProperty().bind(modulation2DContainer.widthProperty());
        canvas.widthProperty().set(500);
        canvas.heightProperty().set(200);

    }
}
