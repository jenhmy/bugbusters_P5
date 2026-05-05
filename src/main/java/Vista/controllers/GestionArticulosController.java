package Vista.controllers;

import Controlador.Controlador;
import Modelo.Articulo;
import Modelo.Excepciones.RecursoNoEncontradoException;
import Vista.fx.SoundFX;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador encargado de la gestión de artículos.
 *
 * Además de la tabla principal, incorpora un panel superior de inventario
 * con indicadores empresariales reales: total de artículos, stock total,
 * artículos sin stock, stock crítico, valor estimado del inventario y artículo
 * con mayor disponibilidad.
 */
public class GestionArticulosController extends GenericoController<Articulo> {

    private static final int UMBRAL_STOCK_CRITICO = 5;

    @FXML private TableColumn<Articulo, String> colCodigo;
    @FXML private TableColumn<Articulo, String> colDesc;
    @FXML private TableColumn<Articulo, BigDecimal> colPrecio;
    @FXML private TableColumn<Articulo, BigDecimal> colEnvio;
    @FXML private TableColumn<Articulo, Integer> colPrep;
    @FXML private TableColumn<Articulo, Integer> colStock;

    @FXML private Label lblTotalArticulos;
    @FXML private Label lblStockTotal;
    @FXML private Label lblSinStock;
    @FXML private Label lblStockCritico;
    @FXML private Label lblValorInventario;
    @FXML private Label lblMayorStock;

    private final Controlador controladorLogico = new Controlador();

    @Override
    protected void configurarVista() {
        configurarColumnas();
        configurarFiltros();
        cargarCatalogoInicial();
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatoMoneda(item));
            }
        });

        colEnvio.setCellValueFactory(new PropertyValueFactory<>("gastosEnvio"));
        colEnvio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatoMoneda(item));
            }
        });

        colPrep.setCellValueFactory(new PropertyValueFactory<>("tiempoPreparacionMin"));
        colPrep.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item + " min");
            }
        });

        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));
        colStock.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(String.valueOf(item));

                if (item <= 0) {
                    setStyle("-fx-text-fill: #ff5c7a; -fx-font-weight: bold;");
                } else if (item <= UMBRAL_STOCK_CRITICO) {
                    setStyle("-fx-text-fill: #ffd166; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #4dffd2; -fx-font-weight: bold;");
                }
            }
        });

        tvTabla.setPlaceholder(new Label("Selecciona un filtro para visualizar los artículos."));
    }

    private void configurarFiltros() {
        comboFiltro.getItems().clear();
        comboFiltro.getItems().addAll("Todos", "Con Stock", "Sin Stock", "Stock Crítico");
    }

    private void cargarCatalogoInicial() {
        try {
            List<Articulo> todos = controladorLogico.obtenerTodosArticulos();
            tvTabla.setItems(FXCollections.observableArrayList(todos));
            actualizarPanelInventario(todos);
            comboFiltro.setValue("Todos");
        } catch (Exception e) {
            System.err.println("Error al cargar catálogo inicial: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo cargar el catálogo de artículos.");
            actualizarPanelInventario(List.of());
        }
    }

    @Override
    protected void filtrar() {
        String seleccion = comboFiltro.getValue();
        if (seleccion == null) return;

        try {
            List<Articulo> todos = controladorLogico.obtenerTodosArticulos();
            List<Articulo> listaFiltrada;

            switch (seleccion) {
                case "Con Stock":
                    listaFiltrada = todos.stream()
                            .filter(a -> stockSeguro(a) > 0)
                            .collect(Collectors.toList());
                    break;

                case "Sin Stock":
                    listaFiltrada = todos.stream()
                            .filter(a -> stockSeguro(a) <= 0)
                            .collect(Collectors.toList());
                    break;

                case "Stock Crítico":
                    listaFiltrada = todos.stream()
                            .filter(a -> stockSeguro(a) > 0 && stockSeguro(a) <= UMBRAL_STOCK_CRITICO)
                            .collect(Collectors.toList());
                    break;

                default:
                    listaFiltrada = todos;
                    break;
            }

            tvTabla.setItems(FXCollections.observableArrayList(listaFiltrada));
            actualizarPanelInventario(todos);

            if (listaFiltrada.isEmpty()) {
                mostrarMensaje("No se encontraron artículos para el filtro: " + seleccion);
            } else {
                mostrarMensaje("Mostrando artículos correctamente.");
            }

        } catch (Exception e) {
            System.err.println("Error al filtrar artículos: " + e.getMessage());
            mostrarMensaje("ERROR: Fallo al cargar el catálogo de artículos.");
        }
    }

    @Override
    protected void realizarBusquedaEspecifica(String texto) {
        try {
            String criterio = normalizarTexto(texto);
            List<Articulo> todos = controladorLogico.obtenerTodosArticulos();

            List<Articulo> resultados = todos.stream()
                    .filter(a -> normalizarTexto(a.getCodigo()).contains(criterio)
                            || normalizarTexto(a.getDescripcion()).contains(criterio))
                    .collect(Collectors.toList());

            tvTabla.setItems(FXCollections.observableArrayList(resultados));
            actualizarPanelInventario(todos);

            if (resultados.isEmpty()) {
                mostrarMensaje("No se encontraron artículos para: " + texto);
            } else {
                mostrarMensaje("Artículos encontrados: " + resultados.size());
            }

        } catch (Exception e) {
            System.err.println("Error al buscar artículos: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo realizar la búsqueda de artículos.");
        }
    }

    private void actualizarPanelInventario(List<Articulo> articulos) {
        if (articulos == null) {
            articulos = List.of();
        }

        int totalArticulos = articulos.size();
        int stockTotal = 0;
        int sinStock = 0;
        int stockCritico = 0;
        BigDecimal valorInventario = BigDecimal.ZERO;

        for (Articulo articulo : articulos) {
            int stock = stockSeguro(articulo);

            stockTotal += stock;

            if (stock <= 0) {
                sinStock++;
            }

            if (stock > 0 && stock <= UMBRAL_STOCK_CRITICO) {
                stockCritico++;
            }

            valorInventario = valorInventario.add(
                    precioSeguro(articulo).multiply(BigDecimal.valueOf(stock))
            );
        }

        Optional<Articulo> articuloMayorStock = articulos.stream()
                .filter(a -> stockSeguro(a) > 0)
                .max(Comparator.comparingInt(this::stockSeguro));

        setTexto(lblTotalArticulos, String.valueOf(totalArticulos));
        setTexto(lblStockTotal, stockTotal + " uds.");
        setTexto(lblSinStock, sinStock + " artículos");
        setTexto(lblStockCritico, stockCritico + " críticos");
        setTexto(lblValorInventario, formatoMoneda(valorInventario));

        if (articuloMayorStock.isPresent()) {
            Articulo articulo = articuloMayorStock.get();
            setTexto(
                    lblMayorStock,
                    descripcionCorta(articulo) + " · " + stockSeguro(articulo) + " uds."
            );
        } else {
            setTexto(lblMayorStock, "Sin stock disponible");
        }
    }

    private int stockSeguro(Articulo articulo) {
        if (articulo == null) return 0;
        return Math.max(0, articulo.getCantidadDisponible());
    }

    private BigDecimal precioSeguro(Articulo articulo) {
        if (articulo == null || articulo.getPrecioVenta() == null) {
            return BigDecimal.ZERO;
        }
        return articulo.getPrecioVenta();
    }

    private String descripcionCorta(Articulo articulo) {
        if (articulo == null) return "Artículo no disponible";

        String descripcion = articulo.getDescripcion();
        String codigo = articulo.getCodigo();

        if (descripcion == null || descripcion.isBlank()) {
            return codigo == null ? "Artículo sin descripción" : codigo;
        }

        if (descripcion.length() > 26) {
            return descripcion.substring(0, 26) + "...";
        }

        return descripcion;
    }

    private void setTexto(Label label, String texto) {
        if (label != null) {
            label.setText(texto);
        }
    }

    private String formatoMoneda(BigDecimal valor) {
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }

        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString() + " €";
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    @FXML
    protected void eliminarElemento() {
        // Funcionalidad reservada para la fase correspondiente del equipo.
    }

    @Override
    @FXML
    protected void mostrarFormulario() {
        SoundFX.click();
        abrirFormulario("/Vista/fxml/formularios/FormularioArticulos.fxml", "Añadir Nuevo Artículo");
        cargarCatalogoInicial();
    }

    /**
     * Abre un diálogo modal para añadir stock a un artículo existente.
     *
     * La operación se delega en Controlador.sumarStockArticulo(), evitando que
     * la vista acceda directamente a DAOs o gestione transacciones JPA.
     */
    @FXML
    protected void anadirStock() {
        SoundFX.click();

        Articulo seleccionado = tvTabla.getSelectionModel().getSelectedItem();
        Dialog<StockInput> dialog = crearDialogoStock(seleccionado);

        Optional<StockInput> result = dialog.showAndWait();
        if (result.isEmpty()) {
            mostrarMensaje("Operación cancelada.");
            return;
        }

        StockInput input = result.get();

        try {
            controladorLogico.sumarStockArticulo(input.codigo(), input.cantidad());
            SoundFX.success();
            mostrarMensaje("Stock añadido correctamente: +" + input.cantidad() + " uds. (" + input.codigo() + ")");
            cargarCatalogoInicial();

        } catch (RecursoNoEncontradoException e) {
            SoundFX.alert();
            mostrarMensaje("No existe ningún artículo con código: " + input.codigo());

        } catch (Exception e) {
            SoundFX.alert();
            System.err.println("Error al añadir stock: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo añadir el stock.");
        }
    }

    private Dialog<StockInput> crearDialogoStock(Articulo seleccionado) {
        Dialog<StockInput> dialog = new Dialog<>();
        dialog.setTitle("Añadir stock");
        dialog.setHeaderText("Suma unidades al stock de un artículo existente");

        ButtonType confirmar = new ButtonType("CONFIRMAR", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmar, ButtonType.CANCEL);

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Código del artículo");
        if (seleccionado != null) {
            txtCodigo.setText(seleccionado.getCodigo());
        }

        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad a añadir");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 24, 10, 24));
        grid.add(new Label("Código:"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(txtCantidad, 1, 1);

        dialog.getDialogPane().setContent(grid);

        try {
            String css = getClass().getResource("/Vista/css/estilos.css").toExternalForm();
            dialog.getDialogPane().getStylesheets().add(css);
        } catch (Exception ignored) {
            // El diálogo sigue funcionando aunque el CSS no pueda cargarse.
        }

        Button botonConfirmar = (Button) dialog.getDialogPane().lookupButton(confirmar);
        botonConfirmar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String error = validarEntradaStock(txtCodigo.getText(), txtCantidad.getText());
            if (error != null) {
                event.consume();
                mostrarMensaje(error);
                SoundFX.alert();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != confirmar) return null;
            String codigo = txtCodigo.getText().trim();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            return new StockInput(codigo, cantidad);
        });

        return dialog;
    }

    private String validarEntradaStock(String codigo, String cantidadTexto) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return "Introduce el código del artículo.";
        }

        if (cantidadTexto == null || cantidadTexto.trim().isEmpty()) {
            return "Introduce la cantidad a añadir.";
        }

        try {
            int cantidad = Integer.parseInt(cantidadTexto.trim());
            if (cantidad <= 0) {
                return "La cantidad debe ser mayor que cero.";
            }
        } catch (NumberFormatException e) {
            return "La cantidad debe ser un número entero válido.";
        }

        return null;
    }

    private record StockInput(String codigo, int cantidad) {}
}