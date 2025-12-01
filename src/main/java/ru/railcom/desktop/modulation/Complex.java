package ru.railcom.desktop.modulation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Complex(double real, double imag) {

    public double squaredDistanceTo(Complex other) {
        double dr = this.real - other.real;
        double di = this.imag - other.imag;
        return dr * dr + di * di;
    }

    public double abs() {
        return Math.sqrt(real * real + imag * imag);
    }

    public double squaredAbs() {
        return real * real + imag * imag;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%.6f%+.6fi", real, imag);
    }
}