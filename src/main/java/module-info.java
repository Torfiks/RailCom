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
    requires java.desktop;
    requires javafx.graphics;

    opens ru.railcom.desktop.ui to javafx.fxml;
    opens ru.railcom.desktop.ui.component to javafx.fxml;
    opens ru.railcom.desktop.ui.control to javafx.fxml;
    opens ru.railcom.desktop.ui.simulation to javafx.fxml;

    exports ru.railcom.desktop;
    exports ru.railcom.desktop.ui;
    exports ru.railcom.desktop.ui.component;
}