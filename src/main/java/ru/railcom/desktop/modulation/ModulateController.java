package ru.railcom.desktop.modulation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.railcom.desktop.module.BaseStation;


@Setter
@Getter
@Builder
public class ModulateController {
    private Modulation modulation;
    private Integer countPoints;
}


