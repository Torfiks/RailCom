package ru.modulator.desktop.modulator;


import lombok.Data;
import lombok.Getter;
import ru.modulator.desktop.modulator.dto.BaseStation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class RP {
    public static double TRACK_LENGTH;
    public static double TRACK_START;
    public static double TRACK_END;

    private Integer countBaseStations;
    private final List<BaseStation> baseStations = new ArrayList<>();

    private void setNewBaseStations() {}

    private void setPositionBaseStations (int baseStationIndex, double positionBaseStation) {

        if (baseStationIndex < 0 || baseStationIndex >= baseStations.size()) {
            return;
        }

        // Обновляем позицию станции
        BaseStation station =  new BaseStation(positionBaseStation / 100.0, (int) (TRACK_LENGTH * positionBaseStation));

        baseStations.set(baseStationIndex, station);

        // Сортируем станции по позиции
        baseStations.sort(Comparator.comparingDouble(BaseStation::getPosition));

    }

    private void createDefaultPositionBaseStations() {

        double spacing = (double) countBaseStations / 100;

        for(int i = 0; i < countBaseStations; i++ ){

            double position = spacing * i;
            int x = (int) (TRACK_LENGTH * (spacing * i));

            baseStations.add(new BaseStation(position, x));

        }

    }

}
