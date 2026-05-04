package Vista.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import javafx.scene.Parent;
import java.net.URL;

/**
 * Controlador del menú principal de la aplicación.
 * Gestiona la navegación entre las distintas vistas del sistema.
 */
public class MenuPrincipalController {

    @FXML private AnchorPane areaContenido;

    @FXML private Button btnArticulos;
    @FXML private Button btnClientes;
    @FXML private Button btnPedidos;
    @FXML private Button btnSalir;

    /**
     * Inicializa los efectos visuales de los botones del menú.
     */
    @FXML
    public void initialize() {
        configurarEfectoBoton(btnArticulos);
        configurarEfectoBoton(btnClientes);
        configurarEfectoBoton(btnPedidos);
    }

    @FXML void abrirArticulos() {
        cargarEscena("GestionArticulos.fxml");
    }

    @FXML void abrirClientes() {
        cargarEscena("GestionClientes.fxml");
    }

    @FXML void abrirPedidos() {
        cargarEscena("GestionPedidos.fxml");
    }

    @FXML void salirApp() {
        System.exit(0);
    }

    /**
     * Carga una vista FXML dentro del área de contenido del menú principal.
     *
     * @param archivoFxml nombre del archivo FXML a cargar
     */
    private void cargarEscena(String archivoFxml) {
        try {
            String rutaFxml = "/Vista/fxml/" + archivoFxml;
            URL fxmlUrl = getClass().getResource(rutaFxml);

            if (fxmlUrl == null) {
                System.err.println("ERROR: No se encuentra el archivo FXML en: " + rutaFxml);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent vista = loader.load();

            String rutaCss = "/Vista/css/estilos.css";
            URL cssUrl = getClass().getResource(rutaCss);
            if (cssUrl != null) {
                vista.getStylesheets().add(cssUrl.toExternalForm());
            }

            areaContenido.getChildren().setAll(vista);

            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);

            System.out.println("Escena cargada con éxito: " + archivoFxml);

        } catch (IOException e) {
            System.err.println("ERROR de entrada/salida al cargar " + archivoFxml);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al cargar la escena:");
            e.printStackTrace();
        }
    }

    /**
     * Aplica efectos visuales de hover a los botones del menú.
     *
     * @param btn botón al que se le aplica el efecto
     */
    private void configurarEfectoBoton(Button btn) {
        if (btn != null) {
            btn.setOnMouseEntered(e ->
                    btn.setStyle("-fx-background-color: #3d4450; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;")
            );

            btn.setOnMouseExited(e ->
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #c1d1db; -fx-font-weight: bold;")
            );
        }
    }
}