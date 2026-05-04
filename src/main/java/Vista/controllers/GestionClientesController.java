package Vista.controllers;

import Modelo.Cliente;
import Modelo.ClientePremium;
import Controlador.Controlador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador encargado de la gestión de la vista de clientes.
 * Permite mostrar, filtrar y gestionar la tabla de clientes estándar y premium.
 */
public class GestionClientesController extends GenericoController<Cliente> {

    @FXML private TableColumn<Cliente, String> colTipo;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colNif;
    @FXML private TableColumn<Cliente, String> colDomicilio;
    @FXML private TableColumn<Cliente, String> colCuota;
    @FXML private TableColumn<Cliente, String> colDesc;

    private Controlador controladorLogico = new Controlador();

    /**
     * Configura las columnas de la tabla y define cómo se muestran los datos.
     */
    @Override
    protected void configurarVista() {

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNif.setCellValueFactory(new PropertyValueFactory<>("nif"));
        colDomicilio.setCellValueFactory(new PropertyValueFactory<>("domicilio"));

        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        (cellData.getValue() instanceof ClientePremium) ? "Premium" : "Estándar"
                )
        );

        colCuota.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.format("%.2f €", cellData.getValue().calcularCuota())
                )
        );

        colDesc.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.format("%.0f%%", cellData.getValue().descuentoEnvio().doubleValue() * 100)
                )
        );

        comboFiltro.getItems().clear();
        comboFiltro.getItems().addAll("Todos", "Premium", "Estándar");
        tvTabla.setPlaceholder(new Label("Selecciona un filtro para visualizar los clientes."));
    }

    /**
     * Filtra la lista de clientes según el tipo seleccionado.
     */
    @Override
    protected void filtrar() {
        String seleccion = comboFiltro.getValue();
        if (seleccion == null) return;

        try {
            List<Cliente> todos = controladorLogico.obtenerTodosClientes();
            List<Cliente> listaFiltrada;

            if ("Estándar".equals(seleccion)) {
                listaFiltrada = todos.stream()
                        .filter(c -> !(c instanceof ClientePremium))
                        .collect(Collectors.toList());
            } else if ("Premium".equals(seleccion)) {
                listaFiltrada = todos.stream()
                        .filter(c -> c instanceof ClientePremium)
                        .collect(Collectors.toList());
            } else {
                listaFiltrada = todos;
            }

            if (listaFiltrada.isEmpty()) {
                tvTabla.setItems(FXCollections.observableArrayList());
                mostrarMensaje("No hay clientes de tipo: " + seleccion);
            } else {
                tvTabla.setItems(FXCollections.observableArrayList(listaFiltrada));
                mostrarMensaje("Mostrando clientes correctamente.");
            }

        } catch (Exception e) {
            System.err.println("Error al filtrar clientes: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo acceder a los datos de clientes.");
        }
    }

    @Override
    protected void realizarBusquedaEspecifica(String texto) {}

    @Override
    @FXML
    protected void eliminarElemento() {}

    /**
     * Abre el formulario para añadir un nuevo cliente.
     */
    @Override
    @FXML
    protected void mostrarFormulario() {
        abrirFormulario("/Vista/fxml/formularios/FormularioCliente.fxml", "Añadir Nuevo Cliente");
    }
}