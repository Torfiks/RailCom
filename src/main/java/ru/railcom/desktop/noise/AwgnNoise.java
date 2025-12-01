package ru.railcom.desktop.noise;

import java.util.Random;
import ru.railcom.desktop.modulation.Complex;

public class AwgnNoise {

    private static final Random RAND = new Random();

    /**
     * Добавляет AWGN-шум к сигналу.
     *
     * @param s       сигнал (массив комплексных символов)
     * @param snrDb   SNR в дБ
     * @param L       oversampling ratio (обычно 1 для символьного моделирования)
     * @return массив с зашумлённым сигналом (s + n)
     */
    public static Complex[] addAwgnNoise(Complex[] s, double snrDb, int L) {
        if (s == null || s.length == 0) {
            return new Complex[0];
        }

        // 1. Переводим SNR из дБ в линейную шкалу
        double gamma = Math.pow(10.0, snrDb / 10.0);

        // 2. Вычисляем среднюю мощность сигнала
        double signalPower = 0.0;
        for (Complex c : s) {
            signalPower += c.squaredAbs(); // |s|^2
        }
        signalPower = L * signalPower / s.length;

        // 3. Плотность шума N0 = P / gamma
        double N0 = signalPower / gamma;

        // 4. Генерация комплексного AWGN
        Complex[] n = new Complex[s.length];
        double noiseVariance = N0 / 2.0; // дисперсия для каждой компоненты
        double noiseStd = Math.sqrt(noiseVariance);

        for (int i = 0; i < s.length; i++) {
            double realNoise = RAND.nextGaussian() * noiseStd;
            double imagNoise = RAND.nextGaussian() * noiseStd;
            n[i] = new Complex(realNoise, imagNoise);
        }

        // 5. Сложение сигнала и шума
        Complex[] r = new Complex[s.length];
        for (int i = 0; i < s.length; i++) {
            r[i] = new Complex(s[i].real() + n[i].real(), s[i].imag() + n[i].imag());
        }

        return r;
    }

    // Перегрузка без параметра L (по умолчанию L=1)
    public static Complex[] addAwgnNoise(Complex[] s, double snrDb) {
        return addAwgnNoise(s, snrDb, 1);
    }
}