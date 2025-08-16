package ru.modulator.desktop.modulator.dto;


public record Train(double speedKmph) {

    public double getDownlink(double signalStrength) {
        // Реалистичный расчет Downlink в зависимости от скорости и сигнала
        double baseSpeed = 300; // Базовая скорость в км/ч
        double speedFactor = Math.min(speedKmph / baseSpeed, 1.0);
        return signalStrength * 100 * speedFactor;
    }

    public double getUplink(double signalStrength) {
        // Реалистичный расчет Uplink в зависимости от скорости и сигнала
        double baseSpeed = 300; // Базовая скорость в км/ч
        double speedFactor = Math.min(speedKmph() / baseSpeed, 1.0);
        return signalStrength * 50 * speedFactor * 0.9; // Uplink обычно слабее Downlink
    }
}