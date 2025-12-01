package ru.railcom.desktop.modulation.list;

import lombok.AllArgsConstructor;
import ru.railcom.desktop.modulation.Complex;

import static ru.railcom.desktop.modulation.Modulate.detector;
import static ru.railcom.desktop.modulation.Modulate.modulator;

@AllArgsConstructor
public class QAM {

    public static Complex[] modulate(int M, int[] d) {
        Complex[] ref = constructQAM(M);

        return modulator(M,d,ref);
    }

    public static int[] detect(int M, Complex[] r) {
        Complex[] ref = constructQAM(M);

        int[] indices0Based = detector(r, ref);
        int[] dCap = new int[indices0Based.length];

        for (int i = 0; i < indices0Based.length; i++) {
            dCap[i] = indices0Based[i] + 1;
        }

        return dCap;
    }

    public static Complex[] constructQAM(int M) {
        if (!isSquareQamSupported(M)) {
            throw new IllegalArgumentException("Only square MQAM supported (M must be 4, 16, 64, 256, ...)");
        }

        if (M == 1) {
            return new Complex[]{new Complex(0, 0)};
        }

        int N = (int) Math.sqrt(M); // N x N grid

        // Шаг 1: создать последовательность индексов 0..M-1
        int[] indices = new int[M];
        for (int i = 0; i < M; i++) {
            indices[i] = i;
        }

        // Шаг 2: преобразовать в Gray code
        int[] gray = new int[M];
        for (int i = 0; i < M; i++) {
            gray[i] = decToGray(indices[i]);
        }

        // Шаг 3: reshape в матрицу N x N и транспонировать (как в MATLAB: reshape(...).')
        int[][] matrix = new int[N][N];
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                // MATLAB: reshape(a, N, N).' → в Java: col по строкам, row по столбцам
                int idx = row * N + col;
                matrix[col][row] = gray[idx]; // транспонирование
            }
        }

        // Шаг 4: отразить чётные строки (нумерация с 1 → в Java: нечётные индексы)
        for (int row = 1; row < N; row += 2) { // row = 1, 3, 5... → 2nd, 4th... in MATLAB
            reverseRow(matrix[row]);
        }

        // Шаг 5: обратно в вектор (reshape(matrix.', 1, M))
        int[] nGray = new int[M];
        int k = 0;
        for (int col = 0; col < N; col++) {
            for (int row = 0; row < N; row++) {
                nGray[k++] = matrix[row][col];
            }
        }

        // Шаг 6: построить координаты
        double[] Ax = new double[M];
        double[] Ay = new double[M];
        for (int i = 0; i < M; i++) {
            int x = nGray[i] / N; // floor(nGray / N)
            int y = nGray[i] % N; // mod(nGray, N)
            // Амплитуда: 2*(index+1) - 1 - N = 2*index + 1 - N
            Ax[i] = 2.0 * x + 1.0 - N;
            Ay[i] = 2.0 * y + 1.0 - N;
        }

        // Шаг 7: создать комплексные точки
        Complex[] ref = new Complex[M];
        for (int i = 0; i < M; i++) {
            ref[i] = new Complex(Ax[i], Ay[i]);
        }

        return ref;
    }

    // Проверка: M — степень двойки и чётная степень (т.е. M = 4, 16, 64, 256, ...)
    public static boolean isSquareQamSupported(int M) {
        if (M == 1) return true;
        if (M < 4) return false;
        // Проверка: M — степень двойки
        if ((M & (M - 1)) != 0) return false;
        // Проверка: log2(M) — чётное число → sqrt(M) — целое
        int log2M = Integer.numberOfTrailingZeros(M);
        return (log2M % 2 == 0);
    }

    // Преобразование десятичного числа в Gray code
    public static int decToGray(int n) {
        return n ^ (n >>> 1);
    }
    private static void reverseRow(int[] row) {
        int n = row.length;
        for (int i = 0; i < n / 2; i++) {
            int temp = row[i];
            row[i] = row[n - 1 - i];
            row[n - 1 - i] = temp;
        }
    }
}
