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

    @FXML
    private void guardar() {
        try {
            // El método anadirPedido ya gestiona internamente la existencia
            // del cliente, el stock del artículo y la transacción.
            controladorLogico.anadirPedido(
                    txtEmailCliente.getText(),
                    txtCodigoArticulo.getText(),
                    Integer.parseInt(txtCantidad.getText())
            );

            SoundFX.success();
            cerrarVentana();
        } catch (Exception e) {
            SoundFX.alert();
            System.err.println("Error al procesar pedido: " + e.getMessage());
        }
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