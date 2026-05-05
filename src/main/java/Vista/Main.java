package Vista;

import Util.JPAUtil;
import Vista.fx.AmbientOrbs;
import Vista.fx.Animations;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;

/**
 * Clase principal de la aplicación JavaFX.
 *
 * Carga el menú principal, aplica la hoja de estilos global y añade una capa
 * visual ambiental no interactiva. El arranque evita realizar operaciones de
 * base de datos bloqueantes antes de mostrar la ventana, ya que la conexión se
 * utiliza bajo demanda desde los controladores y servicios correspondientes.
 */
public class Main extends Application {

    private static final String RUTA_MENU_PRINCIPAL = "/Vista/fxml/MenuPrincipal.fxml";
    private static final String RUTA_CSS = "/Vista/css/estilos.css";

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlLocation = getClass().getResource(RUTA_MENU_PRINCIPAL);
            if (fxmlLocation == null) {
                System.err.println("ERROR: No se encuentra el archivo FXML: " + RUTA_MENU_PRINCIPAL);
                return;
            }

            Parent root = FXMLLoader.load(fxmlLocation);

            AmbientOrbs fondoAmbiental = new AmbientOrbs();
            StackPane stackRoot = new StackPane(fondoAmbiental, root);

            Scene scene = new Scene(stackRoot, 1280, 780);
            scene.setFill(Color.web("#02040a"));

            URL cssUrl = getClass().getResource(RUTA_CSS);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("ADVERTENCIA: No se pudo localizar la hoja de estilos: " + RUTA_CSS);
            }

            primaryStage.setTitle("BugBusters Store - Gestión de Inventario");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1280);
            primaryStage.setMinHeight(780);
            primaryStage.setOnCloseRequest(event -> cerrarAplicacion());

            primaryStage.show();
            Animations.fadeIn(root, Duration.millis(650));

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al arrancar la aplicación JavaFX:");
            e.printStackTrace();
        }
    }

    private void cerrarAplicacion() {
        try {
            JPAUtil.cerrarEntityManagerFactory();
        } catch (Exception e) {
            System.err.println("ADVERTENCIA: No se pudo cerrar correctamente JPA: " + e.getMessage());
        } finally {
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}