package Vista.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import Vista.fx.SoundFX;
import Vista.services.NavigationService;

/**
 * Controlador genérico base que proporciona funcionalidades comunes
 * para las vistas con tablas, búsqueda, filtrado y gestión de formularios.
 *
 * @param <T> tipo de elemento que manejará la tabla
 */
public abstract class GenericoController<T> {

    @FXML protected TableView<T> tvTabla;
    @FXML protected ComboBox<String> comboFiltro;
    @FXML protected Label lblMensaje;
    @FXML protected TextField txtBuscador;
    @FXML protected Button btnLupa;

    /**
     * Inicializa los eventos de búsqueda y filtrado de la vista.
     */
    @FXML
    public void initialize() {
        if (txtBuscador != null) {
            txtBuscador.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    ejecutarBusqueda(txtBuscador.getText());
                }
            });
        }

        if (comboFiltro != null) {
            comboFiltro.setOnAction(e -> filtrar());
        }

        configurarVista();
    }

    /**
     * Activa o ejecuta la búsqueda del buscador.
     */
    @FXML
    void activarBuscador() {
        if (!txtBuscador.isVisible()) {
            txtBuscador.setVisible(true);
            txtBuscador.setManaged(true);
            txtBuscador.requestFocus();
        } else {
            ejecutarBusqueda(txtBuscador.getText());
        }
    }

    /**
     * Ejecuta la búsqueda y cierra el buscador.
     *
     * @param texto texto introducido por el usuario
     */
    protected void ejecutarBusqueda(String texto) {
        if (texto != null && !texto.trim().isEmpty()) {
            realizarBusquedaEspecifica(texto.trim());
        }
        cerrarBuscador();
    }

    /**
     * Oculta y limpia el campo de búsqueda.
     */
    protected void cerrarBuscador() {
        txtBuscador.setText("");
        txtBuscador.setVisible(false);
        txtBuscador.setManaged(false);
    }

    /**
     * Muestra un mensaje temporal en la interfaz.
     *
     * @param texto mensaje a mostrar
     */
    protected void mostrarMensaje(String texto) {
        lblMensaje.setText(texto);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lblMensaje.setText(""));
        pause.play();
    }

    /**
     * Configura la vista inicial de la tabla.
     */
    protected abstract void configurarVista();

    /**
     * Aplica el filtro seleccionado.
     */
    protected abstract void filtrar();

    /**
     * Realiza la búsqueda específica según la lógica del controlador.
     *
     * @param texto texto de búsqueda
     */
    protected abstract void realizarBusquedaEspecifica(String texto);

    /**
     * Elimina un elemento seleccionado.
     */
    @FXML protected abstract void eliminarElemento();

    /**
     * Muestra el formulario correspondiente.
     */
    @FXML protected abstract void mostrarFormulario();

    /**
     * Vuelve al dashboard principal.
     */
    @FXML
    protected void volverInicio() {
        SoundFX.navigate();
        NavigationService.irADashboard();
    }
    /**
     * Abre un formulario FXML en una ventana modal.
     *
     * @param fxmlPath ruta del archivo FXML
     * @param titulo título de la ventana
     */
    protected void abrirFormulario(String fxmlPath, String titulo) {
        try {
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                String pathLimpio = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
                fxmlUrl = getClass().getClassLoader().getResource(pathLimpio);
            }

            if (fxmlUrl == null) {
                throw new RuntimeException("No se encontró el archivo FXML en: " + fxmlPath);
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle(titulo);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            filtrar();
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO AL CARGAR FXML: " + fxmlPath);
            e.printStackTrace();
            mostrarMensaje("Error al abrir el formulario.");
        }
    }
}