package ru.railcom.desktop.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ru.railcom.desktop.SimulatorController;

import java.util.Objects;

import static ru.railcom.desktop.string.NameField.*;

public class MainApplicationUI  {

    private static double xOffset = 0;
    private static double yOffset = 0;

    public static BorderPane createTitleBar(Stage stage) {
        // Заголовок приложения
        Label titleLabel = new Label(TitleLabel);
        titleLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 14px;");
        titleLabel.setPadding(new Insets(10, 10, 10, 15));

        // Кнопка закрытия
        Button closeButton = createStyledButton(ClousItem, Clous, e -> stage.close());

        // Кнопка сворачивания
        Button minimizeButton = createStyledButton(RollUpItem, RollUp, e -> stage.setIconified(true));

        // Кнопка разворачивания/сворачивания
        Button maximizeButton = new Button(UnRollItem);
        maximizeButton.setTooltip(new Tooltip(UnRoll));
        maximizeButton.setStyle(
                "-fx-background-color: #121212; " +
                        "-fx-text-fill: #e0e0e0; " +
                        "-fx-font-size: 16px; " +
                        "-fx-pref-width: 45; " +
                        "-fx-pref-height: 30; " +
                        "-fx-background-radius: 0;"
        );

        maximizeButton.setOnMouseEntered(e ->
                maximizeButton.setStyle(
                        "-fx-background-color: #2d2d2d; " +
                                "-fx-text-fill: #e0e0e0; " +
                                "-fx-font-size: 16px; " +
                                "-fx-pref-width: 45; " +
                                "-fx-pref-height: 30; " +
                                "-fx-background-radius: 0;"
                )
        );
        maximizeButton.setOnMouseExited(e ->
                maximizeButton.setStyle(
                        "-fx-background-color: #121212; " +
                                "-fx-text-fill: #e0e0e0; " +
                                "-fx-font-size: 16px; " +
                                "-fx-pref-width: 45; " +
                                "-fx-pref-height: 30; " +
                                "-fx-background-radius: 0;"
                )
        );

        // Переключение между режимами окна
        maximizeButton.setOnAction(e -> {
            if (stage.isMaximized()) {
                stage.setMaximized(false);
                maximizeButton.setText(UnRollItem);
                maximizeButton.setTooltip(new Tooltip(UnRoll));
            } else {
                stage.setMaximized(true);
                maximizeButton.setText(RestoreItem);
                maximizeButton.setTooltip(new Tooltip(Restore));
            }
        });

        // Создаем группу для кнопок управления
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(0);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.getChildren().addAll(minimizeButton, maximizeButton, closeButton);

        // Создаем правую панель для кнопок
        Pane rightPane = new Pane(buttonsBox);
        rightPane.setPrefWidth(135); // Фиксированная ширина для кнопок

        // Устанавливаем цвет заголовка как у фона
        BorderPane titleBar = new BorderPane();
        titleBar.setPrefHeight(30);
        titleBar.setStyle("-fx-background-color: #121212; -fx-border-color: #1e1e1e; -fx-border-width: 0 0 1 0;");

        // Устанавливаем элементы в BorderPane
        titleBar.setLeft(titleLabel);
        titleBar.setRight(rightPane);

        // Добавляем обработку перетаскивания только для левой части заголовка
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        return titleBar;
    }

    public static ScrollPane createScrollPane(SimulatorController controller) {
        // Оборачиваем UI в ScrollPane для прокрутки всего окна

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(controller.getUI());
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #121212; -fx-background-color: #121212;");

        return scrollPane;
    }

    public static Button createStyledButton(String text, String tooltip, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setTooltip(new Tooltip(tooltip));
        button.setStyle(
                "-fx-background-color: #121212; " +
                        "-fx-text-fill: #e0e0e0; " +
                        "-fx-font-size: 16px; " +
                        "-fx-pref-width: 45; " +
                        "-fx-pref-height: 30; " +
                        "-fx-background-radius: 0;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 16px; " +
                                "-fx-pref-width: 45; " +
                                "-fx-pref-height: 30; " +
                                "-fx-background-radius: 0;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #121212; " +
                                "-fx-text-fill: #e0e0e0; " +
                                "-fx-font-size: 16px; " +
                                "-fx-pref-width: 45; " +
                                "-fx-pref-height: 30; " +
                                "-fx-background-radius: 0;"
                )
        );

        button.setOnAction(action);

        return button;
    }

    public static void setupDragAndDrop(Stage stage, Scene scene) {
        // Обработка перетаскивания окна
        scene.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public static void setupApplicationIcon(Stage stage) {
        try {
            // Создаем простую иконку в виде поезда
            Image icon = new Image(Objects.requireNonNull(MainApplicationUI.class.getResourceAsStream("/logo192.png")));

            // Если иконка не найдена, используем дефолтную
            if (icon.isError()) {
                System.err.println("Ошибка загрузки иконки. Используется дефолтная.");
                // Создаем простую иконку
                icon = new Image(Objects.requireNonNull(MainApplicationUI.class.getResourceAsStream("/logo192.png")));
            }

            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Не удалось загрузить иконку: " + e.getMessage());

            // Создаем простую иконку в памяти как резервный вариант
            try {
                // Создаем простой красный квадрат как иконку
                javafx.scene.image.WritableImage defaultIcon = new javafx.scene.image.WritableImage(32, 32);
                javafx.scene.paint.Color red = javafx.scene.paint.Color.RED;

                for (int x = 0; x < 32; x++) {
                    for (int y = 0; y < 32; y++) {
                        defaultIcon.getPixelWriter().setColor(x, y, red);
                    }
                }

                stage.getIcons().add(defaultIcon);
            } catch (Exception ex) {
                System.err.println("Не удалось создать дефолтную иконку: " + ex.getMessage());
            }
        }
    }

}