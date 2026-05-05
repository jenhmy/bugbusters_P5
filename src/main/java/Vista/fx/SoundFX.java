package Vista.fx;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Motor de sonido sintético para la interfaz JavaFX.
 *
 * Esta versión utiliza una paleta sonora más grave, suave y profesional,
 * evitando sonidos excesivamente agudos o arcaicos. No requiere archivos
 * externos, ya que genera las ondas en tiempo real mediante javax.sound.sampled.
 */
public final class SoundFX {

    private static final float SAMPLE_RATE = 44100f;

    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r, "SoundFX");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile boolean enabled = true;

    private SoundFX() {
    }

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Sonido de hover: muy corto, suave y poco invasivo.
     */
    public static void hover() {
        if (!enabled) return;

        POOL.submit(() -> {
            playTone(420, 520, 32, 0.018, 0.06);
        });
    }

    /**
     * Sonido de click: más grave, tipo pulsación física digital.
     */
    public static void click() {
        if (!enabled) return;

        POOL.submit(() -> {
            playTone(190, 260, 45, 0.045, 0.04);
            sleep(8);
            playTone(360, 420, 38, 0.030, 0.03);
        });
    }

    /**
     * Sonido de navegación: transición suave y profesional entre pantallas.
     */
    public static void navigate() {
        if (!enabled) return;

        POOL.submit(() -> {
            playTone(240, 310, 52, 0.045, 0.04);
            sleep(10);
            playTone(330, 420, 58, 0.040, 0.04);
            sleep(10);
            playTone(460, 560, 72, 0.032, 0.03);
        });
    }

    /**
     * Sonido de éxito: confirmación limpia, cálida y no estridente.
     */
    public static void success() {
        if (!enabled) return;

        POOL.submit(() -> {
            playTone(330, 360, 70, 0.045, 0.04);
            sleep(12);
            playTone(440, 500, 75, 0.043, 0.04);
            sleep(12);
            playTone(590, 660, 95, 0.035, 0.03);
        });
    }

    /**
     * Sonido de alerta: grave, serio y corto.
     */
    public static void alert() {
        if (!enabled) return;

        POOL.submit(() -> {
            playTone(260, 220, 105, 0.055, 0.035);
            sleep(18);
            playTone(210, 180, 120, 0.050, 0.025);
        });
    }

    /**
     * Sonido de notificación: breve, elegante y moderado.
     */
    public static void notify_() {
        if (!enabled) return;

        POOL.submit(() -> {
            playTone(380, 470, 55, 0.032, 0.035);
            sleep(10);
            playTone(520, 610, 65, 0.026, 0.025);
        });
    }

    /**
     * Mantiene compatibilidad con llamadas anteriores a sequence().
     */
    public static void sequence(double[] frequencies, int[] durationsMs, double volume) {
        if (!enabled || frequencies == null || durationsMs == null) return;

        POOL.submit(() -> {
            int length = Math.min(frequencies.length, durationsMs.length);

            for (int i = 0; i < length; i++) {
                playTone(frequencies[i], frequencies[i], durationsMs[i], volume, 0.03);
                sleep(10);
            }
        });
    }

    /**
     * Genera un tono con transición de frecuencia, envolvente suave y armónicos
     * muy controlados para evitar sonidos agudos o metálicos.
     *
     * @param startFrequency frecuencia inicial
     * @param endFrequency frecuencia final
     * @param durationMs duración en milisegundos
     * @param volume volumen general
     * @param harmonicLevel cantidad de armónico añadido
     */
    private static void playTone(
            double startFrequency,
            double endFrequency,
            int durationMs,
            double volume,
            double harmonicLevel
    ) {
        try {
            int samples = (int) (SAMPLE_RATE * durationMs / 1000.0);
            byte[] buffer = new byte[samples * 2];

            double phase = 0.0;
            double harmonicPhase = 0.0;

            for (int i = 0; i < samples; i++) {
                double progress = i / (double) Math.max(1, samples - 1);

                double frequency = startFrequency + ((endFrequency - startFrequency) * easeOut(progress));

                phase += 2.0 * Math.PI * frequency / SAMPLE_RATE;
                harmonicPhase += 2.0 * Math.PI * frequency * 1.5 / SAMPLE_RATE;

                double envelope = envelope(progress);

                double base = Math.sin(phase);
                double harmonic = Math.sin(harmonicPhase) * harmonicLevel;

                double sample = (base * 0.88 + harmonic) * envelope;

                // Saturación suave para evitar aspereza digital.
                sample = Math.tanh(sample * 1.15);

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
            // Si el sistema no tiene salida de audio disponible, la app sigue funcionando.
        }
    }

    /**
     * Envolvente ADSR simplificada.
     * Ataque corto y salida suave para eliminar clics y pitidos secos.
     */
    private static double envelope(double progress) {
        double attackEnd = 0.18;
        double releaseStart = 0.58;

        if (progress < attackEnd) {
            return smoothStep(progress / attackEnd);
        }

        if (progress > releaseStart) {
            double releaseProgress = (progress - releaseStart) / (1.0 - releaseStart);
            return 1.0 - smoothStep(releaseProgress);
        }

        return 1.0;
    }

    private static double smoothStep(double value) {
        value = clamp(value);
        return value * value * (3.0 - 2.0 * value);
    }

    private static double easeOut(double value) {
        value = clamp(value);
        return 1.0 - Math.pow(1.0 - value, 2.0);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}