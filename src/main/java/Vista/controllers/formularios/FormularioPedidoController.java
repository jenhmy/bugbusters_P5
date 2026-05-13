package Vista.controllers.formularios;

import Controlador.Controlador;
import Vista.fx.SoundFX;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FormularioPedidoController {

    @FXML private TextField txtEmailCliente;
    @FXML private TextField txtCodigoArticulo;
    @FXML private TextField txtCantidad;

    private final Controlador controladorLogico = new Controlador();
    private boolean exito = false;

    @FXML
    private void guardar() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());

            controladorLogico.anadirPedido(
                    txtEmailCliente.getText().trim(),
                    txtCodigoArticulo.getText().trim(),
                    cantidad
            );

            this.exito = true;
            SoundFX.success();
            cerrarVentana();
        } catch (NumberFormatException e) {
            SoundFX.alert();
            System.err.println("Error: La cantidad debe ser un número entero.");
        } catch (Exception e) {
            SoundFX.alert();
            System.err.println("Error al procesar pedido: " + e.getMessage());
        }
    }

    public boolean isExito() {
        return exito;
    }

    @FXML
    private void cancelar() {
        SoundFX.click();
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtEmailCliente.getScene().getWindow();
        stage.close();
    }
}