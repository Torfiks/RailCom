package ru.railcom.desktop.modulation;

import ru.railcom.desktop.modulation.list.PAM;
import ru.railcom.desktop.modulation.list.PSK;
import ru.railcom.desktop.modulation.list.QAM;

public class Modulate {

    public static Complex[] modulate(Modulation modulation, Integer M, int[] d){
        return switch(modulation){
//            case FSK -> FSK.modulate(M, d, true);
            case PAM -> PAM.modulate(M, d);
            case PSK -> PSK.modulate(M, d);
            case QAM -> QAM.modulate(M, d);
        };
    }

    public static int[] demodulate(Modulation modulation, Integer M, Complex[] r){
        return switch(modulation){
//            case FSK -> FSK.detect(M,r,true,);
            case PAM -> PAM.detect(M,r);
            case PSK -> PSK.detect(M,r);
            case QAM -> QAM.detect(M,r);
        };
    }

    /**
     * Модулирует символы d (1-based, диапазон [1, M]) в M-QAM сигнал.
     *
     * @param M порядок модуляции (4, 16, 64, ...)
     * @param d массив символов (1-based!)
     * @return массив комплексных модулированных символов
     */
    public static Complex[] modulator(int M, int[] d, Complex[] ref) {
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

    public static Complex[] generateReferenceConstellation(String modulationType) {
        if (modulationType == null) {
            throw new IllegalArgumentException("Modulation type cannot be null");
        }

        // --- PSK ---
        if (modulationType.equals("PSK 2")) {
            return generateMpskConstellation(2);
        } else if (modulationType.equals("PSK 4")) {
            return generateMpskConstellation(4);
        } else if (modulationType.startsWith("PSK")) {
            int M = Integer.parseInt(modulationType.replace("PSK ", ""));
            return generateMpskConstellation(M);
        }

        // --- PAM ---
        else if (modulationType.startsWith("PAM")) {
            int M = Integer.parseInt(modulationType.replace("PAM ", ""));
            return generateMpamConstellation(M);
        }

        // --- QAM ---
        else if (modulationType.startsWith("QAM")) {
            int M = Integer.parseInt(modulationType.replace("QAM ", ""));
            if (M == 4) {
                // QPSK и 4-QAM эквивалентны, но QAM обычно без нормировки
                return generateSquareMqamConstellation(M);
            }
            return generateSquareMqamConstellation(M);
        }

        // --- FSK (векторное представление: каждый символ — отдельная частота) ---
        else if (modulationType.startsWith("FSK")) {
            int M = Integer.parseInt(modulationType.replace("FSK ", ""));
            return generateMfskConstellation(M);
        }

        else {
            throw new IllegalArgumentException("Unsupported modulation type: " + modulationType);
        }
    }

    private static Complex[] generateMfskConstellation(int M) {
        if (M < 2) {
            throw new IllegalArgumentException("M must be >= 2 for FSK");
        }
        Complex[] ref = new Complex[M];
        for (int i = 0; i < M; i++) {
            // Равномерно распределяем по углам (как PSK)
            double angle = 2.0 * Math.PI * i / M;
            ref[i] = new Complex(Math.cos(angle), Math.sin(angle));
        }
        return ref;
    }

    private static Complex[] generateMpamConstellation(int M) {
        Complex[] ref = new Complex[M];
        for (int m = 1; m <= M; m++) {
            double amplitude = 2.0 * m - 1.0 - M;
            ref[m - 1] = new Complex(amplitude, 0.0);
        }
        return ref;
    }

    private static Complex[] generateMpskConstellation(int M) {
        Complex[] ref = new Complex[M];
        for (int i = 0; i < M; i++) {
            double angle = 2.0 * Math.PI * i / M;
            ref[i] = new Complex(Math.cos(angle), Math.sin(angle));
        }
        return ref;
    }

    private static Complex[] generateSquareMqamConstellation(int M) {
        int N = (int) Math.sqrt(M);
        if (N * N != M) {
            throw new IllegalArgumentException("M must be a perfect square for QAM: " + M);
        }
        Complex[] ref = new Complex[M];
        for (int i = 0; i < M; i++) {
            int x = i / N;
            int y = i % N;
            double Ax = 2.0 * x + 1.0 - N;
            double Ay = 2.0 * y + 1.0 - N;
            ref[i] = new Complex(Ax, Ay);
        }
        return ref;
    }
}


