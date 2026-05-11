package Vista.controllers.formularios;

import Controlador.Controlador;
import Vista.fx.SoundFX;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.math.BigDecimal;

public class FormularioArticuloController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtEnvio;
    @FXML private TextField txtPreparacion;
    @FXML private TextField txtStock;

    private final Controlador controladorLogico = new Controlador();

    @FXML
    private void guardar() {
        try {
            // Llamamos al método de tu clase Controlador
            controladorLogico.anadirArticulo(
                    txtCodigo.getText(),
                    txtDescripcion.getText(),
                    new BigDecimal(txtPrecio.getText()),
                    new BigDecimal(txtEnvio.getText()),
                    Integer.parseInt(txtPreparacion.getText()),
                    Integer.parseInt(txtStock.getText())
            );

            SoundFX.success();
            cerrarVentana();
        } catch (Exception e) {
            SoundFX.alert();
            System.err.println("Error al guardar artículo: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        SoundFX.click();
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtCodigo.getScene().getWindow();
        stage.close();
    }
}