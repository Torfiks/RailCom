package ru.railcom.desktop.modulation.list;

import lombok.AllArgsConstructor;
import ru.railcom.desktop.modulation.Complex;

import static ru.railcom.desktop.modulation.Modulate.detector;
import static ru.railcom.desktop.modulation.Modulate.modulator;

@AllArgsConstructor
public class PSK {

    public static Complex[] modulate(int M, int[] d) {
        Complex[] ref = constructPSK(M);

        return modulator(M,d,ref);
    }

    public static int[] detect(int M, Complex[] r) {

        Complex[] ref = constructPSK(M);

        int[] indices0Based = detector(r, ref);
        int[] dCap = new int[indices0Based.length];

        for (int i = 0; i < indices0Based.length; i++) {
            dCap[i] = indices0Based[i] + 1;
        }

        return dCap;
    }

    /**
     * Получение координат эталонных значений точек созвездия
     * @param M - количество точек
     * @return массив координат точек
     */
    public static Complex[] constructPSK(int M) {
        Complex[] ref = new Complex[M];
        double angleStep = 2.0 * Math.PI / M;
//        double scale = 1.0 / Math.sqrt(2.0);
        double scale = 1.0;

        for (int i = 0; i < M; i++) {
            double angle = i * angleStep;
            double refI = scale * Math.cos(angle);
            double refQ = scale * Math.sin(angle);
            ref[i] = new Complex(refI, refQ);
        }
        return ref;
    }
}
