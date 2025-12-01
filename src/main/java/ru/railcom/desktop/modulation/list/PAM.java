package ru.railcom.desktop.modulation.list;

import lombok.AllArgsConstructor;
import ru.railcom.desktop.modulation.Complex;

import static ru.railcom.desktop.modulation.Modulate.detector;
import static ru.railcom.desktop.modulation.Modulate.modulator;

@AllArgsConstructor
public class PAM {

    public static Complex[] modulate(int M, int[] d) {
        Complex[] ref = constructPAM(M);

        return modulator(M, d, ref);
    }

    public static int[] detect(int M, Complex[] r) {
        Complex[] ref = constructPAM(M);

        int[] indices0Based = detector(r, ref);
        int[] dCap = new int[indices0Based.length];

        for (int i = 0; i < indices0Based.length; i++) {
            dCap[i] = indices0Based[i] + 1;
        }

        return dCap;
    }

    public static Complex[] constructPAM(int M) {
        Complex[] ref = new Complex[M];
        for (int m = 1; m <= M; m++) {
            double amplitude = 2.0 * m - 1.0 - M; // вещественная амплитуда
            ref[m - 1] = new Complex(amplitude, 0.0); // Q = 0
        }
        return ref;
    }
}
