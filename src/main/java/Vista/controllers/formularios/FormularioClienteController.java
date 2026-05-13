package Vista.controllers.formularios;

import Controlador.Controlador;
import Vista.fx.SoundFX;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class FormularioClienteController {

    @FXML private TextField txtEmail;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDomicilio;
    @FXML private TextField txtNif;
    @FXML private Button btnEstandar;
    @FXML private Button btnPremium;

    private int tipoClienteSeleccionado = 1; // 1 = Estándar por defecto
    private boolean exito = false;

    @FXML
    public void initialize() {
        // Al abrir la ventana, como el tipo es 1 por defecto, iluminamos el Estándar
        btnEstandar.getStyleClass().setAll("button", "boton-resaltado");
        btnPremium.getStyleClass().setAll("button", "boton-primario");
    }

    private final Controlador controladorLogico = new Controlador();

    @FXML
    private void seleccionarEstandar() {
        SoundFX.click();
        this.tipoClienteSeleccionado = 1;

        btnEstandar.getStyleClass().setAll("button", "boton-resaltado");
        btnPremium.getStyleClass().setAll("button", "boton-primario");
    }

    @FXML
    private void seleccionarPremium() {
        SoundFX.click();
        this.tipoClienteSeleccionado = 2;

        btnEstandar.getStyleClass().setAll("button", "boton-primario");
        btnPremium.getStyleClass().setAll("button", "boton-resaltado");
    }

    @FXML
    private void guardar() {
        try {
            controladorLogico.anadirCliente(
                    txtEmail.getText(),
                    txtNombre.getText(),
                    txtDomicilio.getText(),
                    txtNif.getText(),
                    tipoClienteSeleccionado
            );

            this.exito = true;
            SoundFX.success();
            cerrarVentana();
        } catch (Exception e) {
            SoundFX.alert();
            System.err.println("Error al guardar cliente: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        SoundFX.click();
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.close();
    }

    public boolean isExito() {
        return exito;
    }
}