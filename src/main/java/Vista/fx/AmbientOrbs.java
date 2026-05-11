package Vista.fx;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Random;

/**
 * Fondo global tipo "matrix rain".
 *
 * Actúa como capa de fondo de toda la aplicación y no interfiere con la interacción.
 * Sustituye el fondo anterior para conseguir una ambientación más tecnológica y estable.
 */
public class AmbientOrbs extends Pane {

    private static final String SYMBOLS = "01アカサタナハマヤラワZXCVBNMQWERTYUIOPLKJHGFDS";
    private static final double FONT_SIZE = 18;
    private static final double COLUMN_STEP = 20;

    private final Canvas canvas = new Canvas();
    private final Random random = new Random();

    private double[] drops;
    private int columns;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            draw();
        }
    };

    public AmbientOrbs() {
        setMouseTransparent(true);
        setPickOnBounds(false);

        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);

        widthProperty().addListener((obs, oldVal, newVal) -> rebuildDrops());
        heightProperty().addListener((obs, oldVal, newVal) -> rebuildDrops());

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                timer.stop();
            } else {
                rebuildDrops();
                timer.start();
            }
        });
    }

    private void rebuildDrops() {
        double width = getWidth();
        if (width <= 0) return;

        columns = Math.max(1, (int) Math.ceil(width / COLUMN_STEP));
        drops = new double[columns];

        for (int i = 0; i < columns; i++) {
            drops[i] = random.nextDouble() * Math.max(1, getHeight());
        }
    }

    private void draw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        if (width <= 0 || height <= 0) return;
        if (drops == null || drops.length == 0) rebuildDrops();

        GraphicsContext g = canvas.getGraphicsContext2D();

        // Fondo oscuro con desvanecimiento parcial para dejar rastro
        g.setFill(Color.rgb(2, 6, 12, 0.16));
        g.fillRect(0, 0, width, height);

        g.setFont(Font.font("Consolas", FontWeight.BOLD, FONT_SIZE));

        for (int i = 0; i < columns; i++) {
            double x = i * COLUMN_STEP;
            double y = drops[i];

            char ch = SYMBOLS.charAt(random.nextInt(SYMBOLS.length()));

            // cabeza brillante
            g.setFill(Color.rgb(140, 255, 245, 0.95));
            g.fillText(String.valueOf(ch), x, y);

            // cola
            for (int t = 1; t <= 10; t++) {
                double tailY = y - t * FONT_SIZE;
                if (tailY < 0) break;

                double alpha = Math.max(0.04, 0.32 - (t * 0.025));
                g.setFill(Color.rgb(0, 255, 200, alpha));
                char tailChar = SYMBOLS.charAt(random.nextInt(SYMBOLS.length()));
                g.fillText(String.valueOf(tailChar), x, tailY);
            }

            // velocidad variable
            drops[i] += 3 + random.nextDouble() * 4.2;

            if (drops[i] > height + random.nextDouble() * 150) {
                drops[i] = -random.nextDouble() * 300;
            }
        }

        // velo azulado ligero por encima para integrar con la UI
        g.setFill(Color.rgb(0, 180, 255, 0.035));
        g.fillRect(0, 0, width, height);
    }
}