package Vista.controllers;

import Controlador.Controlador;
import Modelo.Cliente;
import Modelo.ClientePremium;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador encargado de la gestión de clientes.
 *
 * Además de la tabla principal, incorpora un panel CRM con indicadores reales:
 * clientes totales, premium, estándar, porcentaje premium, cuota total estimada,
 * descuento medio aplicado y cliente con mayor cuota.
 */
public class GestionClientesController extends GenericoController<Cliente> {

    @FXML private TableColumn<Cliente, String> colTipo;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colNif;
    @FXML private TableColumn<Cliente, String> colDomicilio;
    @FXML private TableColumn<Cliente, String> colCuota;
    @FXML private TableColumn<Cliente, String> colDesc;

    @FXML private Label lblTotalClientes;
    @FXML private Label lblClientesPremium;
    @FXML private Label lblClientesEstandar;
    @FXML private Label lblPorcentajePremium;
    @FXML private Label lblCuotaTotal;
    @FXML private Label lblDescuentoMedio;
    @FXML private Label lblMayorCuota;

    private final Controlador controladorLogico = new Controlador();

    @Override
    protected void configurarVista() {
        configurarColumnas();
        configurarFiltros();
        cargarClientesInicial();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNif.setCellValueFactory(new PropertyValueFactory<>("nif"));
        colDomicilio.setCellValueFactory(new PropertyValueFactory<>("domicilio"));

        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(obtenerTipoCliente(cellData.getValue()))
        );

        colTipo.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);

                if (empty || tipo == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(tipo);

                if ("Premium".equalsIgnoreCase(tipo)) {
                    setStyle("-fx-text-fill: #4dffd2; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #67f6ff; -fx-font-weight: bold;");
                }
            }
        });

        colCuota.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatoMoneda(cuotaSegura(cellData.getValue())))
        );

        colDesc.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatoPorcentaje(descuentoSeguro(cellData.getValue())))
        );

        tvTabla.setPlaceholder(new Label("Selecciona un filtro para visualizar los clientes."));
    }

    private void configurarFiltros() {
        comboFiltro.getItems().clear();
        comboFiltro.getItems().addAll("Todos", "Premium", "Estándar");
    }

    private void cargarClientesInicial() {
        try {
            List<Cliente> todos = controladorLogico.obtenerTodosClientes();
            tvTabla.setItems(FXCollections.observableArrayList(todos));
            actualizarPanelClientes(todos);
            comboFiltro.setValue("Todos");
        } catch (Exception e) {
            System.err.println("Error al cargar clientes iniciales: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo cargar el listado de clientes.");
            actualizarPanelClientes(List.of());
        }
    }

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

            tvTabla.setItems(FXCollections.observableArrayList(listaFiltrada));
            actualizarPanelClientes(todos);

            if (listaFiltrada.isEmpty()) {
                mostrarMensaje("No hay clientes de tipo: " + seleccion);
            } else {
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
            actualizarPanelClientes(todos);

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

    private void actualizarPanelClientes(List<Cliente> clientes) {
        if (clientes == null) {
            clientes = List.of();
        }

        int totalClientes = clientes.size();
        int premium = 0;
        int estandar = 0;

        BigDecimal cuotaTotal = BigDecimal.ZERO;
        BigDecimal descuentoTotal = BigDecimal.ZERO;

        for (Cliente cliente : clientes) {
            if (cliente instanceof ClientePremium) {
                premium++;
            } else {
                estandar++;
            }

            cuotaTotal = cuotaTotal.add(cuotaSegura(cliente));
            descuentoTotal = descuentoTotal.add(descuentoSeguro(cliente));
        }

        BigDecimal porcentajePremium = totalClientes == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(premium)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalClientes), 1, RoundingMode.HALF_UP);

        BigDecimal descuentoMedio = totalClientes == 0
                ? BigDecimal.ZERO
                : descuentoTotal
                .divide(BigDecimal.valueOf(totalClientes), 4, RoundingMode.HALF_UP);

        Optional<Cliente> clienteMayorCuota = clientes.stream()
                .max(Comparator.comparing(this::cuotaSegura));

        setTexto(lblTotalClientes, String.valueOf(totalClientes));
        setTexto(lblClientesPremium, premium + " premium");
        setTexto(lblClientesEstandar, estandar + " estándar");
        setTexto(lblPorcentajePremium, porcentajePremium.toPlainString() + " %");
        setTexto(lblCuotaTotal, formatoMoneda(cuotaTotal));
        setTexto(lblDescuentoMedio, formatoPorcentaje(descuentoMedio));

        if (clienteMayorCuota.isPresent()) {
            Cliente cliente = clienteMayorCuota.get();
            setTexto(
                    lblMayorCuota,
                    nombreSeguro(cliente) + " · " + formatoMoneda(cuotaSegura(cliente))
            );
        } else {
            setTexto(lblMayorCuota, "Sin clientes registrados");
        }
    }

    private String obtenerTipoCliente(Cliente cliente) {
        return (cliente instanceof ClientePremium) ? "Premium" : "Estándar";
    }

    private String nombreSeguro(Cliente cliente) {
        if (cliente == null || cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            return "Cliente sin nombre";
        }

        String nombre = cliente.getNombre();

        if (nombre.length() > 28) {
            return nombre.substring(0, 28) + "...";
        }

        return nombre;
    }

    private BigDecimal cuotaSegura(Cliente cliente) {
        if (cliente == null) {
            return BigDecimal.ZERO;
        }

        try {
            Object cuota = cliente.calcularCuota();

            if (cuota instanceof BigDecimal) {
                return (BigDecimal) cuota;
            }

            if (cuota instanceof Number) {
                return BigDecimal.valueOf(((Number) cuota).doubleValue());
            }

            return new BigDecimal(String.valueOf(cuota));

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal descuentoSeguro(Cliente cliente) {
        if (cliente == null) {
            return BigDecimal.ZERO;
        }

        try {
            Object descuento = cliente.descuentoEnvio();

            if (descuento instanceof BigDecimal) {
                return (BigDecimal) descuento;
            }

            if (descuento instanceof Number) {
                return BigDecimal.valueOf(((Number) descuento).doubleValue());
            }

            return new BigDecimal(String.valueOf(descuento));

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String formatoMoneda(BigDecimal valor) {
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }

        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €";
    }

    private String formatoPorcentaje(BigDecimal valor) {
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }

        BigDecimal porcentaje = valor.multiply(BigDecimal.valueOf(100));

        return porcentaje.setScale(0, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private void setTexto(Label label, String texto) {
        if (label != null) {
            label.setText(texto);
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    @FXML
    protected void eliminarElemento() {
        // Funcionalidad reservada para la fase correspondiente del equipo.
    }

    /**
     * Abre el formulario para añadir un nuevo cliente.
     */
    @Override
    @FXML
    protected void mostrarFormulario() {
        abrirFormulario("/Vista/fxml/formularios/FormularioCliente.fxml", "Añadir Nuevo Cliente");
        cargarClientesInicial();
    }
}