package ru.railcom.desktop.module;

import javafx.scene.Node;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BaseStation {

    private double position; // позиция в процентах от общего расстояния
    private int x; // координата на оси Ox
    private Node node;
}