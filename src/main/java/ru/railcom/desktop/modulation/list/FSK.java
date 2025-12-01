package ru.railcom.desktop.modulation.list;

import lombok.AllArgsConstructor;
import ru.railcom.desktop.modulation.Complex;

import java.util.Random;

@AllArgsConstructor
public class FSK {

    private static final Random RAND = new Random();

    /**
     * @param signal          [numSymbols][M] — каждый символ: вектор длины M
     * @param referencePhases фазы для каждой частоты (длина M)
     */
    public record MfskResult(Complex[][] signal, Complex[] referencePhases) { }

    /**
     * MFSK-модуляция.
     *
     * @param M          порядок модуляции (число частот)
     * @param d          символы (1-based, 1..M)
     * @param isCoherent  "COHERENT" или "NONCOHERENT"
     * @return результат с сигналом и фазами
     */
    public static MfskResult modulate(int M, int[] d, boolean isCoherent) {
        Complex[] phi = new Complex[M];

        if (isCoherent) {
            // Фаза = 0 → exp(0) = 1
            for (int i = 0; i < M; i++) {
                phi[i] = new Complex(1.0, 0.0);
            }
        } else {
            // Случайные фазы
            for (int i = 0; i < M; i++) {
                double angle = 2.0 * Math.PI * RAND.nextDouble();
                phi[i] = new Complex(Math.cos(angle), Math.sin(angle));
            }
        }

        // Формируем сигнал: для каждого символа — вектор длины M с 1 в позиции символа
        Complex[][] s = new Complex[d.length][M];
        for (int i = 0; i < d.length; i++) {
            int symbol = d[i];
            if (symbol < 1 || symbol > M) {
                throw new IllegalArgumentException("Symbol out of range [1, " + M + "]: " + symbol);
            }
            // Заполняем нулями
            for (int j = 0; j < M; j++) {
                s[i][j] = new Complex(0.0, 0.0);
            }
            // Устанавливаем амплитуду на своей частоте
            s[i][symbol - 1] = phi[symbol - 1]; // 1-based → 0-based
        }

        return new MfskResult(s, phi);
    }

    /**
     * Демодуляция MFSK.
     *
     * @param M          число частот
     * @param r          принятый сигнал: массив векторов длины M (Complex[M] для каждого символа)
     * @param isCoherent  "COHERENT" или "NONCOHERENT"
     * @param refPhases  опорные фазы (только для когерентной; может быть null)
     * @return массив обнаруженных символов (1-based)
     */
    public static int[] detect(int M, Complex[][] r, boolean isCoherent, Complex[] refPhases) {
        int[] dCap = new int[r.length];

        if (isCoherent) {
            if (refPhases == null || refPhases.length != M) {
                throw new IllegalArgumentException("Reference phases required for coherent detection");
            }

            // Преобразуем refPhases в матрицу y: M x M (но на самом деле — только диагональ)
            // Но для совместимости с minEuclideanDistance — сделаем y как M векторов длины M
            double[][] y = new double[M][M];
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < M; j++) {
                    y[i][j] = (i == j) ? refPhases[i].real() : 0.0;
                    // Предполагаем, что imag = 0 при когерентной (phi=0), но для общности можно и real+imag
                    // Однако MATLAB использует только real(r) → мы делаем так же
                }
            }

            // x — real часть принятого сигнала
            double[][] x = new double[r.length][M];
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < M; j++) {
                    x[i][j] = r[i][j].real();
                }
            }

            int[] indices = findNearest(x, y);
            for (int i = 0; i < indices.length; i++) {
                dCap[i] = indices[i] + 1; // 0-based → 1-based
            }

        } else {
            // Некогерентная: выбираем частоту с максимальной огибающей
            for (int i = 0; i < r.length; i++) {
                int bestFreq = 0;
                double maxAmp = r[i][0].abs();
                for (int j = 1; j < M; j++) {
                    double amp = r[i][j].abs();
                    if (amp > maxAmp) {
                        maxAmp = amp;
                        bestFreq = j;
                    }
                }
                dCap[i] = bestFreq + 1; // 0-based → 1-based
            }
        }

        return dCap;
    }
    /**
     * Находит индексы ближайших точек из y для каждой точки в x (по евклидову расстоянию).
     *
     * @param x матрица m x p (в Java: double[][] или Complex[][]; здесь double[][] для real-части)
     * @param y матрица n x p
     * @return массив индексов (0-based) длины m
     */
    public static int[] findNearest(double[][] x, double[][] y) {
        int m = x.length;
        int n = y.length;
        if (m == 0 || n == 0) return new int[0];
        int p = x[0].length;
        if (p != y[0].length) {
            throw new IllegalArgumentException("Dimension mismatch");
        }

        int[] indices = new int[m];
        for (int i = 0; i < m; i++) {
            double minDist = Double.MAX_VALUE;
            int best = 0;
            for (int j = 0; j < n; j++) {
                double dist = 0.0;
                for (int k = 0; k < p; k++) {
                    double diff = x[i][k] - y[j][k];
                    dist += diff * diff;
                }
                if (dist < minDist) {
                    minDist = dist;
                    best = j;
                }
            }
            indices[i] = best;
        }
        return indices;
    }
}
