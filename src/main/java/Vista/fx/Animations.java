package Vista.fx;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Clase utilitaria para centralizar animaciones reutilizables en la interfaz JavaFX.
 *
 * Permite aplicar efectos visuales homogéneos a botones, vistas cargadas,
 * elementos destacados y nodos interactivos, evitando duplicidad de código
 * en los controladores.
 */
public final class Animations {

    private Animations() {
        // Clase de utilidad: no debe instanciarse.
    }

    /**
     * Aplica una entrada progresiva por opacidad.
     *
     * @param node nodo sobre el que se aplica la animación
     * @param duration duración de la animación
     */
    public static void fadeIn(Node node, Duration duration) {
        if (node == null) return;

        node.setOpacity(0);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);
        fade.play();
    }

    /**
     * Aplica una transición de entrada combinando desplazamiento vertical y opacidad.
     *
     * @param node nodo sobre el que se aplica la animación
     * @param duration duración de la animación
     */
    public static void slideUpFadeIn(Node node, Duration duration) {
        if (node == null) return;

        node.setOpacity(0);
        node.setTranslateY(18);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromY(18);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition transition = new ParallelTransition(fade, slide);
        transition.play();
    }

    /**
     * Aplica un pequeño efecto de pulsación al hacer clic.
     *
     * @param node nodo interactivo
     */
    public static void clickBounce(Node node) {
        if (node == null) return;

        ScaleTransition down = new ScaleTransition(Duration.millis(80), node);
        down.setToX(0.96);
        down.setToY(0.96);
        down.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition up = new ScaleTransition(Duration.millis(110), node);
        up.setToX(1.0);
        up.setToY(1.0);
        up.setInterpolator(Interpolator.EASE_OUT);

        down.setOnFinished(e -> up.play());
        down.play();
    }

    /**
     * Añade un efecto hover con brillo tipo neón y escala moderada.
     *
     * @param node nodo interactivo
     * @param colorHex color del resplandor
     * @param scale escala máxima en hover
     */
    public static void neonHover(Node node, String colorHex, double scale) {
        if (node == null) return;

        DropShadow glow = new DropShadow();
        glow.setRadius(18);
        glow.setSpread(0.22);
        glow.setColor(Color.web(colorHex, 0.55));

        node.setOnMouseEntered(e -> {
            node.setEffect(glow);

            ScaleTransition st = new ScaleTransition(Duration.millis(160), node);
            st.setToX(scale);
            st.setToY(scale);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });

        node.setOnMouseExited(e -> {
            node.setEffect(null);

            ScaleTransition st = new ScaleTransition(Duration.millis(160), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
    }

    /**
     * Aplica un resplandor permanente a un nodo.
     *
     * @param node nodo afectado
     * @param colorHex color del resplandor
     * @param radius radio del brillo
     */
    public static void glow(Node node, String colorHex, double radius) {
        if (node == null) return;

        DropShadow glow = new DropShadow();
        glow.setRadius(radius);
        glow.setSpread(0.18);
        glow.setColor(Color.web(colorHex, 0.45));

        node.setEffect(glow);
    }

    /**
     * Aplica un efecto de pulso luminoso.
     *
     * @param node nodo afectado
     * @param colorHex color del brillo
     * @param minRadius radio mínimo
     * @param maxRadius radio máximo
     */
    public static void neonPulse(Node node, String colorHex, double minRadius, double maxRadius) {
        if (node == null) return;

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(colorHex, 0.65));
        glow.setSpread(0.24);
        glow.setRadius(minRadius);
        node.setEffect(glow);

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.ZERO,
                        new javafx.animation.KeyValue(glow.radiusProperty(), minRadius, Interpolator.EASE_BOTH)),
                new javafx.animation.KeyFrame(Duration.seconds(1.35),
                        new javafx.animation.KeyValue(glow.radiusProperty(), maxRadius, Interpolator.EASE_BOTH))
        );

        timeline.setAutoReverse(true);
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }
}