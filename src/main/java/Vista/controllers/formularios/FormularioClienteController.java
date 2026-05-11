package Vista.controllers.formularios;

import Controlador.Controlador;
import Vista.fx.SoundFX;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FormularioClienteController {

    @FXML private TextField txtEmail;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDomicilio;
    @FXML private TextField txtNif;

    private int tipoClienteSeleccionado = 1; // 1 = Estándar por defecto
    private final Controlador controladorLogico = new Controlador();

    @FXML
    private void seleccionarEstandar() {
        SoundFX.click();
        this.tipoClienteSeleccionado = 1;
        // Aquí podrías añadir código para resaltar el botón seleccionado visualmente
    }

    @FXML
    private void seleccionarPremium() {
        SoundFX.click();
        this.tipoClienteSeleccionado = 2;
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
}