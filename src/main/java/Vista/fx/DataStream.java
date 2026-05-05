package Vista.fx;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Componente gráfico decorativo para el dashboard.
 *
 * Dibuja partículas y líneas de datos sobre un Canvas para simular un flujo
 * de información en tiempo real. El AnimationTimer se detiene automáticamente
 * cuando el componente deja de estar asociado a una escena, evitando consumo
 * innecesario de recursos.
 */
public class DataStream extends Canvas {

    private static final int MAX_PARTICLES = 70;

    private final Random random = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private final AnimationTimer timer;

    private long lastSpawn;

    public DataStream(double width, double height) {
        super(width, height);

        setMouseTransparent(true);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
                draw();
            }
        };

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                timer.stop();
            } else {
                timer.start();
            }
        });

        widthProperty().addListener((obs, oldValue, newValue) -> draw());
        heightProperty().addListener((obs, oldValue, newValue) -> draw());
    }

    private void update(long now) {
        if (now - lastSpawn > 55_000_000L && particles.size() < MAX_PARTICLES) {
            spawnParticle();
            lastSpawn = now;
        }

        Iterator<Particle> iterator = particles.iterator();

        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.life -= 0.010;

            if (particle.life <= 0
                    || particle.x < -30
                    || particle.x > getWidth() + 30
                    || particle.y < -30
                    || particle.y > getHeight() + 30) {
                iterator.remove();
            }
        }
    }

    private void spawnParticle() {
        double width = getWidth();
        double height = getHeight();

        if (width <= 0 || height <= 0) return;

        double startFromLeft = random.nextBoolean() ? -10 : width + 10;
        double y = 10 + random.nextDouble() * Math.max(1, height - 20);

        double targetX = random.nextDouble() * width;
        double targetY = random.nextDouble() * height;

        double dx = targetX - startFromLeft;
        double dy = targetY - y;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) return;

        double speed = 0.45 + random.nextDouble() * 1.25;

        Particle particle = new Particle();
        particle.x = startFromLeft;
        particle.y = y;
        particle.vx = (dx / length) * speed;
        particle.vy = (dy / length) * speed;
        particle.life = 0.75 + random.nextDouble() * 0.55;
        particle.radius = 1.2 + random.nextDouble() * 2.4;

        int colorType = random.nextInt(4);
        particle.color = switch (colorType) {
            case 0 -> Color.web("#00f0ff");
            case 1 -> Color.web("#4dffd2");
            case 2 -> Color.web("#9d4edd");
            default -> Color.web("#ff2e88");
        };

        particles.add(particle);
    }

    private void draw() {
        GraphicsContext g = getGraphicsContext2D();
        double width = getWidth();
        double height = getHeight();

        if (width <= 0 || height <= 0) return;

        g.clearRect(0, 0, width, height);

        drawGrid(g, width, height);
        drawConnections(g);
        drawParticles(g);
    }

    private void drawGrid(GraphicsContext g, double width, double height) {
        g.setStroke(Color.web("#1d3652", 0.30));
        g.setLineWidth(0.7);

        double step = 22;

        for (double x = 0; x <= width; x += step) {
            g.strokeLine(x, 0, x, height);
        }

        for (double y = 0; y <= height; y += step) {
            g.strokeLine(0, y, width, y);
        }
    }

    private void drawConnections(GraphicsContext g) {
        for (int i = 0; i < particles.size(); i++) {
            Particle a = particles.get(i);

            for (int j = i + 1; j < particles.size(); j++) {
                Particle b = particles.get(j);

                double dx = a.x - b.x;
                double dy = a.y - b.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 80) {
                    double alpha = (1.0 - distance / 80.0) * 0.28 * Math.min(a.life, b.life);
                    g.setStroke(Color.color(0.49, 0.89, 1.0, alpha));
                    g.setLineWidth(0.8);
                    g.strokeLine(a.x, a.y, b.x, b.y);
                }
            }
        }
    }

    private void drawParticles(GraphicsContext g) {
        for (Particle particle : particles) {
            Color color = particle.color == null ? Color.web("#00f0ff") : particle.color;
            double alpha = Math.max(0, Math.min(1, particle.life));

            g.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha * 0.85));
            g.fillOval(
                    particle.x - particle.radius,
                    particle.y - particle.radius,
                    particle.radius * 2,
                    particle.radius * 2
            );

            g.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha * 0.35));
            g.setLineWidth(1.0);
            g.strokeOval(
                    particle.x - particle.radius * 2.2,
                    particle.y - particle.radius * 2.2,
                    particle.radius * 4.4,
                    particle.radius * 4.4
            );
        }
    }

    private static class Particle {
        private double x;
        private double y;
        private double vx;
        private double vy;
        private double life;
        private double radius;
        private Color color;
    }
}