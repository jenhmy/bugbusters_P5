package Vista.controllers;

import Util.JPAUtil;
import Vista.fx.Animations;
import Vista.fx.DashboardWelcome;
import Vista.fx.SoundFX;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Reflection;
import javafx.scene.image.ImageView;
import Vista.services.NavigationService;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador principal de navegación de la aplicación.
 *
 * Gestiona el menú lateral, la carga dinámica de vistas dentro del área central,
 * el dashboard empresarial y los efectos visuales generales de la interfaz.
 */
public class MenuPrincipalController {

    @FXML private AnchorPane areaContenido;

    @FXML private Button btnArticulos;
    @FXML private Button btnClientes;
    @FXML private Button btnPedidos;
    @FXML private Button btnSalir;

    @FXML private ImageView imgLogo;
    @FXML private Label lblHora;
    @FXML private Label lblStatus;
    @FXML private Circle statusDot;

    private static final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Timeline relojTimeline;
    private ScrollPane dashboardView;

    @FXML
    public void initialize() {
        if (areaContenido != null) {
            areaContenido.setStyle("-fx-background-color: transparent;");
        }

        configurarBotonNavegacion(btnArticulos);
        configurarBotonNavegacion(btnClientes);
        configurarBotonNavegacion(btnPedidos);
        configurarBotonSalir();
        configurarLogo();
        configurarEstado();

        NavigationService.registrarDashboardAction(this::mostrarDashboard);

        iniciarReloj();
        mostrarDashboard();
    }

    private void configurarBotonNavegacion(Button boton) {
        if (boton == null) return;

        Animations.neonHover(boton, "#00f0ff", 1.035);
        boton.setOnMouseEntered(e -> SoundFX.hover());
        boton.setOnMousePressed(e -> {
            SoundFX.click();
            Animations.clickBounce(boton);
        });
    }

    private void configurarBotonSalir() {
        if (btnSalir == null) return;

        Animations.neonHover(btnSalir, "#ff5577", 1.025);
        btnSalir.setOnMouseEntered(e -> SoundFX.hover());
    }

    private void configurarLogo() {
        if (imgLogo == null) return;

        if (imgLogo.getImage() == null) {
            imgLogo.setManaged(false);
            imgLogo.setVisible(false);
            return;
        }

        Reflection reflection = new Reflection();
        reflection.setFraction(0.30);
        reflection.setTopOpacity(0.35);
        reflection.setBottomOpacity(0.0);

        imgLogo.setEffect(reflection);
        imgLogo.setStyle("-fx-cursor: hand;");
        imgLogo.setOnMouseClicked(e -> {
            SoundFX.navigate();
            mostrarDashboard();
        });
    }

    private void configurarEstado() {
        if (lblStatus != null) {
            lblStatus.setText("SISTEMA EN LÍNEA");
        }

        if (statusDot != null) {
            statusDot.setStyle("-fx-fill: #4dffd2;");
            Animations.neonPulse(statusDot, "#4dffd2", 4, 12);
        }
    }

    @FXML
    private void abrirArticulos() {
        SoundFX.navigate();
        cargarEscena("GestionArticulos.fxml");
    }

    @FXML
    private void abrirClientes() {
        SoundFX.navigate();
        cargarEscena("GestionClientes.fxml");
    }

    @FXML
    private void abrirPedidos() {
        SoundFX.navigate();
        cargarEscena("GestionPedidos.fxml");
    }

    @FXML
    private void salirApp() {
        SoundFX.alert();
        try {
            if (relojTimeline != null) {
                relojTimeline.stop();
            }
            JPAUtil.cerrarEntityManagerFactory();
        } catch (Exception e) {
            System.err.println("ADVERTENCIA: No se pudo cerrar JPA correctamente: " + e.getMessage());
        } finally {
            Platform.exit();
        }
    }

    private void mostrarDashboard() {
        if (areaContenido == null) return;

        DashboardWelcome dashboard = new DashboardWelcome(
                this::abrirArticulos,
                this::abrirClientes,
                this::abrirPedidos
        );

        dashboardView = crearScrollTransparente(dashboard);

        areaContenido.getChildren().setAll(dashboardView);
        AnchorPane.setTopAnchor(dashboardView, 0.0);
        AnchorPane.setBottomAnchor(dashboardView, 0.0);
        AnchorPane.setLeftAnchor(dashboardView, 0.0);
        AnchorPane.setRightAnchor(dashboardView, 0.0);

        Animations.slideUpFadeIn(dashboardView, Duration.millis(420));
    }

    private ScrollPane crearScrollTransparente(Region contenido) {
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setMinSize(0, 0);

        scroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );

        contenido.setStyle("-fx-background-color: transparent;");

        scroll.skinProperty().addListener((obs, oldSkin, newSkin) -> aplicarTransparenciaScroll(scroll));
        Platform.runLater(() -> aplicarTransparenciaScroll(scroll));

        return scroll;
    }

    private void aplicarTransparenciaScroll(ScrollPane scroll) {
        if (scroll == null) return;

        Node viewport = scroll.lookup(".viewport");
        if (viewport != null) {
            viewport.setStyle("-fx-background-color: transparent;");
        }

        Node content = scroll.getContent();
        if (content != null) {
            content.setStyle("-fx-background-color: transparent;");
        }
    }

    private void cargarEscena(String archivoFxml) {
        if (areaContenido == null) return;

        try {
            String rutaFxml = "/Vista/fxml/" + archivoFxml;
            URL fxmlUrl = getClass().getResource(rutaFxml);

            if (fxmlUrl == null) {
                System.err.println("ERROR: No se encuentra el archivo FXML en: " + rutaFxml);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent vista = loader.load();

            URL cssUrl = getClass().getResource("/Vista/css/estilos.css");
            if (cssUrl != null && !vista.getStylesheets().contains(cssUrl.toExternalForm())) {
                vista.getStylesheets().add(cssUrl.toExternalForm());
            }

            vista.setStyle("-fx-background-color: transparent;");

            areaContenido.getChildren().setAll(vista);
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);

            Animations.slideUpFadeIn(vista, Duration.millis(360));
            SoundFX.notify_();

        } catch (IOException e) {
            SoundFX.alert();
            System.err.println("ERROR de entrada/salida al cargar " + archivoFxml);
            e.printStackTrace();

        } catch (Exception e) {
            SoundFX.alert();
            System.err.println("ERROR CRÍTICO al cargar la escena " + archivoFxml + ":");
            e.printStackTrace();
        }
    }

    private void iniciarReloj() {
        if (lblHora == null) return;

        actualizarReloj();
        relojTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> actualizarReloj()));
        relojTimeline.setCycleCount(Animation.INDEFINITE);
        relojTimeline.play();
    }

    private void actualizarReloj() {
        if (lblHora != null) {
            lblHora.setText(LocalTime.now().format(HOUR_FMT));
        }
    }
}