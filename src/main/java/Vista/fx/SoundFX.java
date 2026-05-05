package Vista.fx;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Motor de sonido sintético para la interfaz.
 *
 * Genera sonidos ligeros y tecnológicos sin depender de archivos externos.
 * Los sonidos se ejecutan en segundo plano para no bloquear JavaFX.
 */
public final class SoundFX {

    private static final float SAMPLE_RATE = 44100f;

    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r, "SoundFX");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile boolean enabled = true;

    private SoundFX() {}

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void hover() {
        sequence(new double[]{1420, 1680}, new int[]{22, 28}, 0.025);
    }

    public static void click() {
        sequence(new double[]{720, 1180}, new int[]{36, 42}, 0.055);
    }

    public static void navigate() {
        sequence(new double[]{520, 760, 1180}, new int[]{45, 45, 70}, 0.060);
    }

    public static void success() {
        sequence(new double[]{660, 880, 1320}, new int[]{55, 55, 90}, 0.070);
    }

    public static void alert() {
        sequence(new double[]{360, 260}, new int[]{95, 130}, 0.080);
    }

    public static void notify_() {
        sequence(new double[]{1200, 1600}, new int[]{35, 65}, 0.050);
    }

    public static void sequence(double[] frequencies, int[] durationsMs, double volume) {
        if (!enabled || frequencies == null || durationsMs == null) return;

        POOL.submit(() -> {
            int length = Math.min(frequencies.length, durationsMs.length);

            for (int i = 0; i < length; i++) {
                playBlocking(frequencies[i], durationsMs[i], volume);
                sleep(10);
            }
        });
    }

    private static void playBlocking(double frequency, int durationMs, double volume) {
        try {
            int samples = (int) (SAMPLE_RATE * durationMs / 1000);
            byte[] buffer = new byte[samples * 2];

            double durationSeconds = durationMs / 1000.0;

            for (int i = 0; i < samples; i++) {
                double t = i / SAMPLE_RATE;

                double attack = Math.min(1.0, t / Math.max(0.001, durationSeconds * 0.18));
                double release = Math.min(1.0, (durationSeconds - t) / Math.max(0.001, durationSeconds * 0.32));
                double envelope = Math.max(0.0, Math.min(attack, release));

                double base = Math.sin(2 * Math.PI * frequency * t);
                double harmonic = Math.sin(2 * Math.PI * frequency * 2.01 * t) * 0.20;
                double shimmer = Math.sin(2 * Math.PI * frequency * 3.02 * t) * 0.08;

                double sample = (base * 0.72 + harmonic + shimmer) * envelope;

                short value = (short) (sample * volume * Short.MAX_VALUE);

                buffer[i * 2] = (byte) (value & 0xFF);
                buffer[i * 2 + 1] = (byte) ((value >> 8) & 0xFF);
            }

            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
            }

        } catch (Exception ignored) {
            // Si el sistema no permite audio, la interfaz continúa funcionando.
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}