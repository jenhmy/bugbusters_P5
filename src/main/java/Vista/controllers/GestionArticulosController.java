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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador encargado de la gestión de artículos en la vista.
 *
 * Permite mostrar, filtrar, buscar, abrir el formulario de creación y añadir
 * stock a artículos existentes, manteniendo la lógica de negocio delegada en
 * el controlador principal del proyecto.
 */
public class GestionArticulosController extends GenericoController<Articulo> {

    @FXML private TableColumn<Articulo, String> colCodigo;
    @FXML private TableColumn<Articulo, String> colDesc;
    @FXML private TableColumn<Articulo, BigDecimal> colPrecio;
    @FXML private TableColumn<Articulo, BigDecimal> colEnvio;
    @FXML private TableColumn<Articulo, Integer> colPrep;
    @FXML private TableColumn<Articulo, Integer> colStock;

    private final Controlador controladorLogico = new Controlador();

    @Override
    protected void configurarVista() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.2f €", item));
            }
        });

        colEnvio.setCellValueFactory(new PropertyValueFactory<>("gastosEnvio"));
        colEnvio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.2f €", item));
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

        comboFiltro.getItems().clear();
        comboFiltro.getItems().addAll("Todos", "Con Stock", "Sin Stock");

        tvTabla.setPlaceholder(new Label("Selecciona un filtro para visualizar los artículos."));
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
                            .filter(a -> a.getCantidadDisponible() > 0)
                            .collect(Collectors.toList());
                    break;

                case "Sin Stock":
                    listaFiltrada = todos.stream()
                            .filter(a -> a.getCantidadDisponible() <= 0)
                            .collect(Collectors.toList());
                    break;

                default:
                    listaFiltrada = todos;
                    break;
            }

            tvTabla.setItems(FXCollections.observableArrayList(listaFiltrada));

            if (listaFiltrada.isEmpty()) {
                mostrarMensaje("No se encontraron artículos: " + seleccion);
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
            filtrar();

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