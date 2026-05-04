package Vista.controllers;

import Modelo.Pedido;
import Controlador.Controlador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador encargado de la gestión de pedidos.
 * Permite visualizar, filtrar, actualizar estado y gestionar pedidos desde la interfaz.
 */
public class GestionPedidosController extends GenericoController<Pedido> {

    @FXML private TableColumn<Pedido, Integer> colId;
    @FXML private TableColumn<Pedido, String> colCliente;
    @FXML private TableColumn<Pedido, String> colArticulo;
    @FXML private TableColumn<Pedido, String> colCant;
    @FXML private TableColumn<Pedido, String> colFecha;
    @FXML private TableColumn<Pedido, String> colTotal;
    @FXML private TableColumn<Pedido, String> colEstado;

    private Controlador controladorLogico = new Controlador();

    /**
     * Configura la tabla de pedidos y define cómo se muestran sus datos.
     */
    @Override
    protected void configurarVista() {

        colId.setCellValueFactory(new PropertyValueFactory<>("numeroPedido"));

        colCliente.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCliente().getNombre()));

        colArticulo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getArticulo().getDescripcion()));

        colCant.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getCantidad())));

        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaHora().toString()));

        colTotal.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f €", cellData.getValue().calcularTotal())));

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        comboFiltro.getItems().clear();
        comboFiltro.getItems().addAll("Todos los pedidos", "Pendientes", "Enviados");

        tvTabla.setPlaceholder(new Label("Selecciona un filtro para visualizar los pedidos."));
    }

    /**
     * Filtra los pedidos según su estado.
     */
    @Override
    protected void filtrar() {
        String seleccion = comboFiltro.getValue();
        if (seleccion == null) return;

        try {
            List<Pedido> todos = controladorLogico.getListaPedidos();
            List<Pedido> listaFiltrada;

            if ("Pendientes".equals(seleccion)) {
                listaFiltrada = todos.stream()
                        .filter(p -> "PENDIENTE".equalsIgnoreCase(p.getEstado()))
                        .collect(Collectors.toList());
            } else if ("Enviados".equals(seleccion)) {
                listaFiltrada = todos.stream()
                        .filter(p -> "ENVIADO".equalsIgnoreCase(p.getEstado()))
                        .collect(Collectors.toList());
            } else {
                listaFiltrada = todos;
            }

            if (listaFiltrada.isEmpty()) {
                tvTabla.setItems(FXCollections.observableArrayList());
                mostrarMensaje("No hay pedidos con el estado: " + seleccion);
            } else {
                tvTabla.setItems(FXCollections.observableArrayList(listaFiltrada));
                mostrarMensaje("Mostrando pedidos correctamente.");
            }

        } catch (Exception e) {
            System.err.println("Error al filtrar: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo conectar con la base de datos.");
        }
    }

    @Override protected void realizarBusquedaEspecifica(String texto) {}

    @Override @FXML protected void eliminarElemento() {}

    @Override @FXML protected void mostrarFormulario() {
        abrirFormulario("/Vista/fxml/formularios/FormularioPedido.fxml", "Nuevo Pedido");
    }

    /**
     * Marca un pedido seleccionado como enviado y actualiza la vista.
     */
    @FXML
    public void cambiarAEnviado() {
        Pedido seleccionado = tvTabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Por favor, selecciona un pedido de la tabla.");
            return;
        }

        boolean confirmar = mostrarVentanaConfirmacion(
                "¿Marcar pedido #" + seleccionado.getNumeroPedido() + " como ENVIADO?"
        );

        if (confirmar) {
            try {
                controladorLogico.marcarPedidoComoEnviado(seleccionado.getNumeroPedido());
                mostrarMensaje("Pedido enviado correctamente.");
                filtrar();
            } catch (Exception e) {
                mostrarMensaje("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * Muestra una ventana modal de confirmación personalizada.
     *
     * @param mensaje mensaje a mostrar en la ventana
     * @return true si el usuario confirma, false si cancela
     */
    private boolean mostrarVentanaConfirmacion(String mensaje) {
        final boolean[] resultado = {false};

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Vista/fxml/ConfirmacionDialog.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            Stage primaryStage = (Stage) tvTabla.getScene().getWindow();
            stage.initOwner(primaryStage);

            Scene scene = new Scene(root);
            stage.setScene(scene);

            root.setStyle("-fx-border-color: #66c0f4; -fx-border-width: 2; -fx-background-color: #1b2838;");

            Label lbl = (Label) root.lookup("#lblMensaje");
            if (lbl != null) {
                lbl.setText(mensaje);
                lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            }

            Button btnOk = (Button) root.lookup("#btnAceptar");
            Button btnCan = (Button) root.lookup("#btnCancelar");

            btnOk.setOnAction(e -> { resultado[0] = true; stage.close(); });
            btnCan.setOnAction(e -> { resultado[0] = false; stage.close(); });

            stage.setOnShowing(ev -> {
                stage.setX(primaryStage.getX() + (primaryStage.getWidth() / 2) - 200);
                stage.setY(primaryStage.getY() + (primaryStage.getHeight() / 2) - 100);
            });

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultado[0];
    }
}