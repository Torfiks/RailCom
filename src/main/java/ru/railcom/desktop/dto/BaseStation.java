package ru.modulator.desktop.modulator.dto;

import javafx.scene.Node;
import lombok.Data;

@Data
public class BaseStation {
    private double position; // позиция в процентах от общего расстояния
    private int x; // координата на оси Ox
    private Node node;

    public BaseStation(double position, int x) {
        this.position = position;
        this.x = x;
    }

}