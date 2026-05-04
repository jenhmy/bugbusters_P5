package Vista;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Clase principal de la aplicación JavaFX que inicializa la interfaz
 * gráfica cargando el FXML y los estilos, y muestra la ventana principal.
 */
public class Main extends Application {

    /**
     * Método que inicia la interfaz gráfica y configura la ventana principal.
     *
     * @param primaryStage escenario principal de la aplicación
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("DEBUG: Iniciando arranque de la aplicación...");

            // Carga del FXML principal
            URL fxmlLocation = getClass().getResource("/Vista/fxml/MenuPrincipal.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: No se encuentra MenuPrincipal.fxml en /Vista/fxml/");
                return;
            }

            Parent root = FXMLLoader.load(fxmlLocation);
            Scene scene = new Scene(root);

            // Carga del CSS
            String cssPath = "/Vista/css/estilos.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS cargado correctamente.");
            } else {
                System.err.println("No se pudo localizar el CSS en: " + cssPath);
            }

            // Configuración de la ventana
            primaryStage.setTitle("BugBusters Store - Gestión de Inventario");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1127);
            primaryStage.setMinHeight(650);

            System.out.println("DEBUG: Intentando mostrar la ventana principal...");
            primaryStage.show();
            System.out.println("¡Ventana mostrada con éxito!");

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al arrancar Main:");
            e.printStackTrace();
        }
    }

    /**
     * Punto de entrada que prueba la conexión a la base de datos
     * y lanza la aplicación JavaFX.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        try {
            System.out.println("DEBUG: Intentando conectar con la base de datos...");
            jakarta.persistence.EntityManager em = Util.JPAUtil.getEntityManager();
            System.out.println("CONEXIÓN EXITOSA: " + em.isOpen());
            em.close();
        } catch (Exception e) {
            System.err.println("ERROR DE CONEXIÓN: " + e.getMessage());
            e.printStackTrace();
        }

        launch(args);
        launch(args);
    }
}