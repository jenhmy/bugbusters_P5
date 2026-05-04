package Vista;

/**
 * Clase lanzadora que delega la ejecución al {@code main} de {@code Main}
 * para evitar restricciones de JavaFX.
 */
public class Launcher {

    /**
     * Punto de entrada que llama al {@code main} de {@code Main}.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}