package Vista.controllers;

import Modelo.Articulo;
import Controlador.Controlador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador encargado de la gestión de artículos en la vista.
 * Permite mostrar, filtrar y formatear la tabla de productos.
 */
public class GestionArticulosController extends GenericoController<Articulo> {

    @FXML private TableColumn<Articulo, String> colCodigo;
    @FXML private TableColumn<Articulo, String> colDesc;
    @FXML private TableColumn<Articulo, BigDecimal> colPrecio;
    @FXML private TableColumn<Articulo, BigDecimal> colEnvio;
    @FXML private TableColumn<Articulo, Integer> colPrep;
    @FXML private TableColumn<Articulo, Integer> colStock;

    private Controlador controladorLogico = new Controlador();

    /**
     * Configura la vista de la tabla de artículos y el formato de sus columnas.
     */
    @Override
    protected void configurarVista() {

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.2f €", item));
            }
        });
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));

        colEnvio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%.2f €", item));
            }
        });
        colEnvio.setCellValueFactory(new PropertyValueFactory<>("gastosEnvio"));

        colPrep.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item + " min");
            }
        });
        colPrep.setCellValueFactory(new PropertyValueFactory<>("tiempoPreparacionMin"));

        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));

        comboFiltro.getItems().clear();
        comboFiltro.getItems().addAll("Todos", "Con Stock", "Sin Stock");

        tvTabla.setPlaceholder(new Label("Selecciona un filtro para visualizar los artículos."));
    }

    /**
     * Filtra los artículos según el estado de stock seleccionado.
     */
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

            if (listaFiltrada.isEmpty()) {
                tvTabla.setItems(FXCollections.observableArrayList());
                mostrarMensaje("No se encontraron artículos: " + seleccion);
            } else {
                tvTabla.setItems(FXCollections.observableArrayList(listaFiltrada));
                mostrarMensaje("Mostrando artículos correctamente.");
            }

        } catch (Exception e) {
            System.err.println("Error al filtrar artículos: " + e.getMessage());
            mostrarMensaje("ERROR: Fallo al cargar el catálogo de artículos.");
        }
    }

    @Override protected void realizarBusquedaEspecifica(String texto) {}

    @Override
    @FXML protected void eliminarElemento() {}

    /**
     * Abre el formulario para añadir un nuevo artículo.
     */
    @Override
    @FXML protected void mostrarFormulario() {
        abrirFormulario("/Vista/fxml/formularios/FormularioArticulos.fxml", "Añadir Nuevo Artículo");
    }
}