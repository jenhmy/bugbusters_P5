package Vista.controllers;

import Modelo.Cliente;
import Modelo.ClientePremium;
import Controlador.Controlador;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Controlador encargado de la gestión de la vista de clientes.
 * Permite mostrar, filtrar, buscar y gestionar la tabla de clientes estándar y premium.
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
                new SimpleStringProperty(obtenerTipoCliente(cellData.getValue()))
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

    /**
     * Realiza una búsqueda optimizada de clientes por nombre, email, NIF,
     * domicilio, tipo de cliente o identificador interno.
     *
     * La búsqueda ignora mayúsculas y minúsculas para facilitar el uso de la interfaz.
     *
     * @param texto texto introducido por el usuario en el buscador
     */
    @Override
    protected void realizarBusquedaEspecifica(String texto) {
        try {
            String criterio = normalizarTexto(texto);

            List<Cliente> todos = controladorLogico.obtenerTodosClientes();

            List<Cliente> resultados = todos.stream()
                    .filter(c ->
                            normalizarTexto(c.getNombre()).contains(criterio)
                                    || normalizarTexto(c.getEmail()).contains(criterio)
                                    || normalizarTexto(c.getNif()).contains(criterio)
                                    || normalizarTexto(c.getDomicilio()).contains(criterio)
                                    || normalizarTexto(obtenerTipoCliente(c)).contains(criterio)
                                    || String.valueOf(c.getIdCliente()).contains(criterio)
                    )
                    .collect(Collectors.toList());

            tvTabla.setItems(FXCollections.observableArrayList(resultados));

            if (resultados.isEmpty()) {
                mostrarMensaje("No se encontraron clientes para: " + texto);
            } else {
                mostrarMensaje("Clientes encontrados: " + resultados.size());
            }

        } catch (Exception e) {
            System.err.println("Error al buscar clientes: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo realizar la búsqueda de clientes.");
        }
    }

    /**
     * Devuelve el tipo textual del cliente para mostrarlo en la tabla y poder buscarlo.
     *
     * @param cliente cliente evaluado
     * @return Premium o Estándar
     */
    private String obtenerTipoCliente(Cliente cliente) {
        return (cliente instanceof ClientePremium) ? "Premium" : "Estándar";
    }

    /**
     * Normaliza un texto para realizar comparaciones seguras durante la búsqueda.
     *
     * @param valor texto original
     * @return texto normalizado en minúsculas y sin espacios externos
     */
    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

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