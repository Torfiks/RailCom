package ru.railcom.desktop.modulation;

import ru.railcom.desktop.modulation.list.FSK;
import ru.railcom.desktop.modulation.list.PAM;
import ru.railcom.desktop.modulation.list.PSK;
import ru.railcom.desktop.modulation.list.QAM;

public class Modulate {

    public void modulate(Modulation modulation, Integer m){
        switch(modulation){
            case FSK -> new FSK();
            case PAM -> new PAM();
            case PSK -> new PSK();
            case QAM -> new QAM();
        }
    }

    public void demodulate(Modulation modulation, Integer m){
        switch(modulation){
            case FSK -> new FSK();
            case PAM -> new PAM();
            case PSK -> new PSK();
            case QAM -> new QAM();
        }
    }

    /**
     * Модулирует символы d (1-based, диапазон [1, M]) в M-QAM сигнал.
     *
     * @param M порядок модуляции (4, 16, 64, ...)
     * @param d массив символов (1-based!)
     * @return массив комплексных модулированных символов
     */
    public static Complex[] modulator(int M, int[] d,Complex[] ref) {
        Complex[] s = new Complex[d.length];
        for (int i = 0; i < d.length; i++) {
            if (d[i] < 1 || d[i] > M) {
                throw new IllegalArgumentException("Symbol out of range [1, " + M + "]: " + d[i]);
            }
            s[i] = ref[d[i] - 1];
        }

        return s;
    }

    /**
     * Находит ближайшие точки из референсного созвездия для каждого принятого символа.
     *
     * @param received массив принятых комплексных символов
     * @param ref      массив опорных комплексных символов
     * @return массив индексов ближайших точек (0-based)
     */
    public static int[] detector(Complex[] received, Complex[] ref) {
        int[] indices = new int[received.length];
        for (int i = 0; i < received.length; i++) {
            Complex r = received[i];
            int bestIndex = 0;
            double minDist = r.squaredDistanceTo(ref[0]);

            for (int j = 1; j < ref.length; j++) {
                double dist = r.squaredDistanceTo(ref[j]);
                if (dist < minDist) {
                    minDist = dist;
                    bestIndex = j;
                }
            }
            indices[i] = bestIndex; // 0-based
        }
        return indices;
    }
}


