package Vista.controllers;

import Controlador.Controlador;
import Modelo.Pedido;
import Vista.fx.SoundFX;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador encargado de la gestión de pedidos.
 *
 * Además de la tabla principal, incorpora un panel operativo/financiero
 * con indicadores reales: pedidos totales, pendientes, enviados,
 * facturación total, facturación mensual, ticket medio y pedido de mayor importe.
 */
public class GestionPedidosController extends GenericoController<Pedido> {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableColumn<Pedido, Integer> colId;
    @FXML private TableColumn<Pedido, String> colCliente;
    @FXML private TableColumn<Pedido, String> colArticulo;
    @FXML private TableColumn<Pedido, String> colCant;
    @FXML private TableColumn<Pedido, String> colFecha;
    @FXML private TableColumn<Pedido, String> colTotal;
    @FXML private TableColumn<Pedido, String> colEstado;

    @FXML private Label lblTotalPedidos;
    @FXML private Label lblPedidosPendientes;
    @FXML private Label lblPedidosEnviados;
    @FXML private Label lblFacturacionTotal;
    @FXML private Label lblFacturacionMes;
    @FXML private Label lblTicketMedio;
    @FXML private Label lblMayorImporte;

    private final Controlador controladorLogico = new Controlador();

    @Override
    protected void configurarVista() {
        configurarColumnas();
        configurarFiltros();
        cargarPedidosInicial();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("numeroPedido"));

        colCliente.setCellValueFactory(cellData ->
                new SimpleStringProperty(nombreCliente(cellData.getValue()))
        );

        colArticulo.setCellValueFactory(cellData ->
                new SimpleStringProperty(descripcionArticulo(cellData.getValue()))
        );

        colCant.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cantidadSegura(cellData.getValue())))
        );

        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatearFecha(cellData.getValue()))
        );

        colTotal.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatoMoneda(calcularTotalSeguro(cellData.getValue())))
        );

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);

                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                String estadoNormalizado = estado.trim().toUpperCase(Locale.ROOT);
                setText(estadoNormalizado);

                if ("PENDIENTE".equals(estadoNormalizado)) {
                    setStyle("-fx-text-fill: #ffd166; -fx-font-weight: bold;");
                } else if ("ENVIADO".equals(estadoNormalizado)) {
                    setStyle("-fx-text-fill: #4dffd2; -fx-font-weight: bold;");
                } else if ("CANCELADO".equals(estadoNormalizado)) {
                    setStyle("-fx-text-fill: #ff5c7a; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #dceff8; -fx-font-weight: bold;");
                }
            }
        });

        tvTabla.setPlaceholder(new Label("Selecciona un filtro para visualizar los pedidos."));
    }

    private void configurarFiltros() {
        comboFiltro.getItems().clear();
        comboFiltro.getItems().addAll("Todos los pedidos", "Pendientes", "Enviados");
    }

    private void cargarPedidosInicial() {
        try {
            List<Pedido> todos = controladorLogico.getListaPedidos();
            tvTabla.setItems(FXCollections.observableArrayList(todos));
            actualizarPanelPedidos(todos);
            comboFiltro.setValue("Todos los pedidos");

        } catch (Exception e) {
            System.err.println("Error al cargar pedidos iniciales: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo cargar el listado de pedidos.");
            actualizarPanelPedidos(List.of());
        }
    }

    @Override
    protected void filtrar() {
        String seleccion = comboFiltro.getValue();
        if (seleccion == null) return;

        try {
            List<Pedido> todos = controladorLogico.getListaPedidos();
            List<Pedido> listaFiltrada;

            if ("Pendientes".equals(seleccion)) {
                listaFiltrada = todos.stream()
                        .filter(p -> "PENDIENTE".equalsIgnoreCase(estadoSeguro(p)))
                        .collect(Collectors.toList());

            } else if ("Enviados".equals(seleccion)) {
                listaFiltrada = todos.stream()
                        .filter(p -> "ENVIADO".equalsIgnoreCase(estadoSeguro(p)))
                        .collect(Collectors.toList());

            } else {
                listaFiltrada = todos;
            }

            tvTabla.setItems(FXCollections.observableArrayList(listaFiltrada));
            actualizarPanelPedidos(todos);
            tvTabla.refresh();

            if (listaFiltrada.isEmpty()) {
                mostrarMensaje("No hay pedidos con el estado: " + seleccion);
            } else {
                mostrarMensaje("Mostrando pedidos correctamente.");
            }

        } catch (Exception e) {
            System.err.println("Error al filtrar pedidos: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo conectar con la base de datos.");
        }
    }

    @Override
    protected void realizarBusquedaEspecifica(String texto) {
        try {
            String criterio = normalizarTexto(texto);
            List<Pedido> todos = controladorLogico.getListaPedidos();

            List<Pedido> resultados = todos.stream()
                    .filter(p ->
                            String.valueOf(p.getNumeroPedido()).contains(criterio)
                                    || normalizarTexto(nombreCliente(p)).contains(criterio)
                    )
                    .collect(Collectors.toList());

            tvTabla.setItems(FXCollections.observableArrayList(resultados));
            actualizarPanelPedidos(todos);

            if (resultados.isEmpty()) {
                mostrarMensaje("No se encontraron pedidos para: " + texto);
            } else {
                mostrarMensaje("Pedidos encontrados: " + resultados.size());
            }

        } catch (Exception e) {
            System.err.println("Error al buscar pedidos: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo realizar la búsqueda de pedidos.");
        }
    }

    private void actualizarPanelPedidos(List<Pedido> pedidos) {
        if (pedidos == null) {
            pedidos = List.of();
        }

        int totalPedidos = pedidos.size();
        int pendientes = 0;
        int enviados = 0;
        int pedidosFacturables = 0;

        BigDecimal facturacionTotal = BigDecimal.ZERO;
        BigDecimal facturacionMes = BigDecimal.ZERO;

        LocalDateTime inicioMes = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        for (Pedido pedido : pedidos) {
            String estado = estadoSeguro(pedido);

            if ("PENDIENTE".equalsIgnoreCase(estado)) {
                pendientes++;
            }

            if ("ENVIADO".equalsIgnoreCase(estado)) {
                enviados++;
            }

            if (!"CANCELADO".equalsIgnoreCase(estado)) {
                BigDecimal totalPedido = calcularTotalSeguro(pedido);
                facturacionTotal = facturacionTotal.add(totalPedido);
                pedidosFacturables++;

                if (pedido.getFechaHora() != null && !pedido.getFechaHora().isBefore(inicioMes)) {
                    facturacionMes = facturacionMes.add(totalPedido);
                }
            }
        }

        BigDecimal ticketMedio = pedidosFacturables == 0
                ? BigDecimal.ZERO
                : facturacionTotal.divide(BigDecimal.valueOf(pedidosFacturables), 2, RoundingMode.HALF_UP);

        Optional<Pedido> mayorImporte = pedidos.stream()
                .filter(p -> !"CANCELADO".equalsIgnoreCase(estadoSeguro(p)))
                .max(Comparator.comparing(this::calcularTotalSeguro));

        setTexto(lblTotalPedidos, String.valueOf(totalPedidos));
        setTexto(lblPedidosPendientes, pendientes + " pendientes");
        setTexto(lblPedidosEnviados, enviados + " enviados");
        setTexto(lblFacturacionTotal, formatoMoneda(facturacionTotal));
        setTexto(lblFacturacionMes, formatoMoneda(facturacionMes));
        setTexto(lblTicketMedio, formatoMoneda(ticketMedio));

        if (mayorImporte.isPresent()) {
            Pedido pedido = mayorImporte.get();
            setTexto(
                    lblMayorImporte,
                    "#" + pedido.getNumeroPedido()
                            + " · " + nombreCliente(pedido)
                            + " · " + formatoMoneda(calcularTotalSeguro(pedido))
            );
        } else {
            setTexto(lblMayorImporte, "Sin pedidos facturables");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private String nombreCliente(Pedido pedido) {
        try {
            if (pedido == null || pedido.getCliente() == null || pedido.getCliente().getNombre() == null) {
                return "";
            }

            return pedido.getCliente().getNombre();

        } catch (Exception e) {
            return "";
        }
    }

    private String descripcionArticulo(Pedido pedido) {
        try {
            if (pedido == null || pedido.getArticulo() == null || pedido.getArticulo().getDescripcion() == null) {
                return "";
            }

            return pedido.getArticulo().getDescripcion();

        } catch (Exception e) {
            return "";
        }
    }

    private int cantidadSegura(Pedido pedido) {
        if (pedido == null) return 0;
        return Math.max(0, pedido.getCantidad());
    }

    private String estadoSeguro(Pedido pedido) {
        if (pedido == null || pedido.getEstado() == null) {
            return "";
        }

        return pedido.getEstado();
    }

    private String formatearFecha(Pedido pedido) {
        if (pedido == null || pedido.getFechaHora() == null) {
            return "";
        }

        return pedido.getFechaHora().format(FECHA_FMT);
    }

    private BigDecimal calcularTotalSeguro(Pedido pedido) {
        try {
            BigDecimal total = pedido.calcularTotal();
            return total == null ? BigDecimal.ZERO : total;

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

    private void setTexto(Label label, String texto) {
        if (label != null) {
            label.setText(texto);
        }
    }

    @Override
    @FXML
    protected void eliminarElemento() {
        Pedido seleccionado = tvTabla.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Por favor, selecciona un pedido de la tabla.");
            SoundFX.alert();
            return;
        }

        if ("ENVIADO".equalsIgnoreCase(estadoSeguro(seleccionado))) {
            mostrarMensaje("No se puede eliminar un pedido que ya consta como ENVIADO.");
            SoundFX.alert();
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/Vista/fxml/ConfirmacionDialog.fxml"));
            javafx.scene.Parent root = loader.load();

            ConfirmacionController confController = loader.getController();
            confController.setMensaje("¿Estás seguro de cancelar el pedido nº " + seleccionado.getNumeroPedido() + "?\n" +
                    "Se restaurará el stock de: " + descripcionArticulo(seleccionado));

            Stage stage = new Stage();
            stage.setTitle("Confirmar Cancelación");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (confController.isConfirmado()) {
                controladorLogico.eliminarPedido(seleccionado.getNumeroPedido());

                SoundFX.success();
                cargarPedidosInicial();
                filtrar();
                mostrarMensaje("Pedido eliminado y stock restaurado.");


            }

        } catch (Exception e) {
            System.err.println("Error al eliminar pedido: " + e.getMessage());
            mostrarMensaje("ERROR: " + e.getMessage());
            SoundFX.alert();
        }
    }

    @Override
    @FXML
    protected void mostrarFormulario() {
        SoundFX.click();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/fxml/formularios/FormularioPedido.fxml"));
            Parent root = loader.load();

            Vista.controllers.formularios.FormularioPedidoController controller = loader.getController();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            stage.showAndWait();

            if (controller.isExito()) {
                cargarPedidosInicial();
                filtrar();
                mostrarMensaje("Pedido registrado correctamente.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("ERROR: No se pudo abrir el formulario.");
        }
    }

    @FXML
    public void cambiarAEnviado() {
        Pedido seleccionado = tvTabla.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Por favor, selecciona un pedido de la tabla.");
            SoundFX.alert();
            return;
        }

        if ("ENVIADO".equalsIgnoreCase(estadoSeguro(seleccionado))) {
            mostrarMensaje("Este pedido ya ha sido enviado.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/fxml/ConfirmacionDialog.fxml"));
            Parent root = loader.load();

            ConfirmacionController confController = loader.getController();
            confController.setMensaje("¿Deseas marcar el pedido #" + seleccionado.getNumeroPedido() +
                    " como ENVIADO?\nEsta acción es irreversible.");

            Stage stage = new Stage();
            stage.setTitle("Confirmar Envío");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (confController.isConfirmado()) {
                controladorLogico.marcarPedidoComoEnviado(seleccionado.getNumeroPedido());

                SoundFX.success();
                cargarPedidosInicial();
                filtrar();
                mostrarMensaje("Pedido marcado como ENVIADO.");


            }

        } catch (Exception e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
            mostrarMensaje("ERROR: " + e.getMessage());
            SoundFX.alert();
        }
    }

}