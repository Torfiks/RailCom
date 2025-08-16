module ru.modulator.desktop.modulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires annotations;
    requires static lombok;

    opens ru.railcom.desktop to javafx.fxml;
    exports ru.railcom.desktop;
    exports ru.railcom.desktop.dto;
    opens ru.railcom.desktop.dto to javafx.fxml;
    exports ru.railcom.desktop.module;
    opens ru.railcom.desktop.module to javafx.fxml;
}