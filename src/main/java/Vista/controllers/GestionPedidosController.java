package Vista.controllers;

import Controlador.Controlador;
import Modelo.Pedido;
import Vista.fx.SoundFX;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
        // Funcionalidad reservada para la fase correspondiente del equipo.
    }

    @Override
    @FXML
    protected void mostrarFormulario() {
        abrirFormulario("/Vista/fxml/formularios/FormularioPedido.fxml", "Nuevo Pedido");
        cargarPedidosInicial();
    }

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
                cargarPedidosInicial();

            } catch (Exception e) {
                mostrarMensaje("ERROR: " + e.getMessage());
            }
        }
    }

    private boolean mostrarVentanaConfirmacion(String mensaje) {
        final boolean[] resultado = {false};

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        if (tvTabla != null && tvTabla.getScene() != null) {
            stage.initOwner(tvTabla.getScene().getWindow());
        }

        VBox panel = new VBox(22);
        panel.setPrefWidth(500);
        panel.setPadding(new Insets(26, 28, 26, 28));
        panel.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #071321, #030813);" +
                        "-fx-background-radius: 26;" +
                        "-fx-border-radius: 26;" +
                        "-fx-border-color: rgba(0,240,255,0.65);" +
                        "-fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,240,255,0.35), 34, 0.22, 0, 0);"
        );

        Label titulo = new Label("CONFIRMACIÓN");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);
        titulo.setStyle(
                "-fx-text-fill: #00f0ff;" +
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,240,255,0.55), 12, 0.30, 0, 0);"
        );

        Label lblMensajeDialogo = new Label(mensaje);
        lblMensajeDialogo.setWrapText(true);
        lblMensajeDialogo.setMaxWidth(Double.MAX_VALUE);
        lblMensajeDialogo.setAlignment(Pos.CENTER);
        lblMensajeDialogo.setStyle(
                "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: 650;"
        );

        HBox botones = new HBox(22);
        botones.setAlignment(Pos.CENTER);

        StackPane aceptar = crearBotonConfirmacionVisual("ACEPTAR", true);
        StackPane cancelar = crearBotonConfirmacionVisual("CANCELAR", false);

        aceptar.setOnMouseClicked(e -> {
            resultado[0] = true;
            SoundFX.success();
            stage.close();
        });

        cancelar.setOnMouseClicked(e -> {
            resultado[0] = false;
            SoundFX.click();
            stage.close();
        });

        botones.getChildren().addAll(aceptar, cancelar);
        panel.getChildren().addAll(titulo, lblMensajeDialogo, botones);

        StackPane shell = new StackPane(panel);
        shell.setPadding(new Insets(28));
        shell.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(shell);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.setOnShown(e -> centrarVentanaConfirmacion(stage));
        stage.showAndWait();

        return resultado[0];
    }

    private StackPane crearBotonConfirmacionVisual(String texto, boolean principal) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setStyle(
                principal
                        ? "-fx-text-fill: #031018; -fx-font-size: 13px; -fx-font-weight: 900;"
                        : "-fx-text-fill: #ff6686; -fx-font-size: 13px; -fx-font-weight: 900;"
        );

        StackPane boton = new StackPane(label);
        boton.setMinSize(160, 46);
        boton.setPrefSize(160, 46);
        boton.setMaxSize(160, 46);
        boton.setFocusTraversable(false);

        String estiloNormal = crearEstiloBotonConfirmacion(principal, false);
        String estiloHover = crearEstiloBotonConfirmacion(principal, true);

        boton.setStyle(estiloNormal);
        boton.setOnMouseEntered(e -> boton.setStyle(estiloHover));
        boton.setOnMouseExited(e -> boton.setStyle(estiloNormal));

        return boton;
    }

    private String crearEstiloBotonConfirmacion(boolean principal, boolean hover) {
        if (principal) {
            return (hover
                    ? "-fx-background-color: linear-gradient(to bottom right, #9dfff2, #20f6ff);"
                    : "-fx-background-color: linear-gradient(to bottom right, #7ce8ff, #00f0ff);")
                    + "-fx-background-radius: 14;"
                    + "-fx-border-radius: 14;"
                    + "-fx-border-color: rgba(255,255,255,0.32);"
                    + "-fx-border-width: 1;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,240,255,0.36), 16, 0.25, 0, 0);"
                    + "-fx-cursor: hand;";
        }

        return (hover
                ? "-fx-background-color: rgba(255,85,119,0.15);"
                : "-fx-background-color: rgba(255,85,119,0.08);")
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: rgba(255,85,119,0.50);"
                + "-fx-border-width: 1;"
                + "-fx-cursor: hand;";
    }

    private void centrarVentanaConfirmacion(Stage stage) {
        Window owner = stage.getOwner();

        if (owner != null) {
            stage.setX(owner.getX() + (owner.getWidth() / 2) - (stage.getWidth() / 2));
            stage.setY(owner.getY() + (owner.getHeight() / 2) - (stage.getHeight() / 2));
        }
    }
}