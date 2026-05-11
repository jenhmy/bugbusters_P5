package Vista.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controlador encargado de la gestión del modal de confirmación tanto de eliminar pedidos,
 * articulos y clientes como confirmar el envío de un pedido.
 */

public class ConfirmacionController {

    @FXML private Label lblMensaje;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancelar;

    private boolean confirmado = false;

    public void setMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    @FXML
    private void aceptar() {
        confirmado = true;
        cerrar();
    }

    @FXML
    private void cancelar() {
        confirmado = false;
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) lblMensaje.getScene().getWindow();
        stage.close();
    }
}
