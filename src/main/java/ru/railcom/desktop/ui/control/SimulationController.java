package ru.railcom.desktop.ui.control;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.railcom.desktop.module.BaseStation;

import java.util.List;

@Setter
@Getter
@Builder
public class SimulationController {
    private boolean isRunning;
    private double track; // Длина трассы
    private double currentSpeed; // Текущая скорость

    private int countBaseStations; // Количество базовых станций
    private List<BaseStation> baseStations; // Список станций

    private double trainPosition; // Позиция поезда
    private double trainSpeed; // Скорость поезда (для анимации)

    private String typeArea = "urban";
}