package Vista.controllers;

import Controlador.Controlador;
import Modelo.Articulo;
import Modelo.Excepciones.RecursoNoEncontradoException;
import Vista.fx.SoundFX;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

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
        Articulo seleccionado = tvTabla.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Por favor, selecciona un artículo de la tabla.");
            SoundFX.alert();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/fxml/ConfirmacionDialog.fxml"));
            Parent root = loader.load();

            ConfirmacionController confController = loader.getController();
            confController.setMensaje("¿Estás seguro de que deseas eliminar el artículo: "
                    + seleccionado.getDescripcion() + "?");

            Stage stage = new Stage();
            stage.setTitle("Confirmar Eliminación");
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (confController.isConfirmado()) {
                controladorLogico.eliminarArticulo(seleccionado.getCodigo()); // Llamada al controlador JPA
                SoundFX.success();
                mostrarMensaje("Artículo eliminado correctamente.");

                cargarCatalogoInicial();
            }

        } catch (Exception e) {
            System.err.println("Error al procesar la eliminación: " + e.getMessage());
            mostrarMensaje("ERROR: No se pudo eliminar el artículo.");
            SoundFX.alert();
        }
    }

    @Override
    @FXML
    protected void mostrarFormulario() {
        SoundFX.click();
        abrirFormulario("/Vista/fxml/formularios/FormularioArticulos.fxml", "Añadir Nuevo Artículo");
        cargarCatalogoInicial();
    }

    @FXML
    protected void anadirStock() {
        SoundFX.click();

        Articulo seleccionado = tvTabla.getSelectionModel().getSelectedItem();
        Optional<StockInput> result = mostrarDialogoStockPersonalizado(seleccionado);

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

    private Optional<StockInput> mostrarDialogoStockPersonalizado(Articulo seleccionado) {
        final StockInput[] resultado = {null};

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        if (tvTabla != null && tvTabla.getScene() != null) {
            stage.initOwner(tvTabla.getScene().getWindow());
        }

        VBox panel = new VBox(18);
        panel.setPrefWidth(460);
        panel.setPadding(new Insets(22, 24, 22, 24));
        panel.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #071321, #030813);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-color: rgba(0,240,255,0.65);" +
                        "-fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,240,255,0.35), 34, 0.22, 0, 0);"
        );

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("AÑADIR STOCK");
        titulo.setStyle(
                "-fx-text-fill: #00f0ff;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,240,255,0.55), 12, 0.30, 0, 0);"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane cerrar = crearBotonModalVisual("×", false, 42, 38);
        cerrar.setOnMouseClicked(e -> {
            SoundFX.click();
            stage.close();
        });

        header.getChildren().addAll(titulo, spacer, cerrar);

        Label subtitulo = new Label("Suma unidades al stock de un artículo existente");
        subtitulo.setWrapText(true);
        subtitulo.setStyle(
                "-fx-text-fill: #dceff8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 700;"
        );

        TextField txtCodigo = crearCampoModal("Código del artículo");
        if (seleccionado != null) {
            txtCodigo.setText(seleccionado.getCodigo());
        }

        TextField txtCantidad = crearCampoModal("Cantidad a añadir");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(8, 0, 4, 0));

        grid.add(crearLabelModal("Código:"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(crearLabelModal("Cantidad:"), 0, 1);
        grid.add(txtCantidad, 1, 1);

        HBox botones = new HBox(20);
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(8, 0, 0, 0));

        StackPane confirmar = crearBotonModalVisual("CONFIRMAR", true, 150, 44);
        StackPane cancelar = crearBotonModalVisual("CANCELAR", false, 150, 44);

        confirmar.setOnMouseClicked(e -> {
            String error = validarEntradaStock(txtCodigo.getText(), txtCantidad.getText());

            if (error != null) {
                mostrarMensaje(error);
                SoundFX.alert();
                return;
            }

            String codigo = txtCodigo.getText().trim();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());

            resultado[0] = new StockInput(codigo, cantidad);
            SoundFX.success();
            stage.close();
        });

        cancelar.setOnMouseClicked(e -> {
            SoundFX.click();
            stage.close();
        });

        botones.getChildren().addAll(confirmar, cancelar);
        panel.getChildren().addAll(header, subtitulo, grid, botones);

        StackPane shell = new StackPane(panel);
        shell.setPadding(new Insets(26));
        shell.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(shell);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.setOnShown(e -> centrarVentanaModal(stage));
        stage.showAndWait();

        return Optional.ofNullable(resultado[0]);
    }

    private StackPane crearBotonModalVisual(String texto, boolean principal, double ancho, double alto) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setStyle(
                principal
                        ? "-fx-text-fill: #031018; -fx-font-size: 12px; -fx-font-weight: 900;"
                        : "-fx-text-fill: #ff6686; -fx-font-size: 12px; -fx-font-weight: 900;"
        );

        StackPane boton = new StackPane(label);
        boton.setMinSize(ancho, alto);
        boton.setPrefSize(ancho, alto);
        boton.setMaxSize(ancho, alto);
        boton.setFocusTraversable(false);

        String estiloNormal = crearEstiloBotonVisual(principal, false);
        String estiloHover = crearEstiloBotonVisual(principal, true);

        boton.setStyle(estiloNormal);
        boton.setOnMouseEntered(e -> boton.setStyle(estiloHover));
        boton.setOnMouseExited(e -> boton.setStyle(estiloNormal));

        return boton;
    }

    private String crearEstiloBotonVisual(boolean principal, boolean hover) {
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

    private TextField crearCampoModal(String prompt) {
        TextField campo = new TextField();
        campo.setPromptText(prompt);
        campo.setPrefWidth(245);
        campo.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(8,15,27,0.96), rgba(5,10,19,0.96));" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 650;" +
                        "-fx-prompt-text-fill: rgba(201,216,230,0.42);" +
                        "-fx-border-color: rgba(0,240,255,0.34);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 11;" +
                        "-fx-background-radius: 11;" +
                        "-fx-padding: 10 14 10 14;"
        );
        return campo;
    }

    private Label crearLabelModal(String texto) {
        Label label = new Label(texto);
        label.setStyle(
                "-fx-text-fill: #dceff8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 800;"
        );
        return label;
    }

    private void centrarVentanaModal(Stage stage) {
        Window owner = stage.getOwner();

        if (owner != null) {
            stage.setX(owner.getX() + (owner.getWidth() / 2) - (stage.getWidth() / 2));
            stage.setY(owner.getY() + (owner.getHeight() / 2) - (stage.getHeight() / 2));
        }
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