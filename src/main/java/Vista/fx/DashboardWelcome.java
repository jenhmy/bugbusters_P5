package Vista.fx;

import Vista.services.DashboardStats;
import Vista.services.DashboardStats.Stats;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

/**
 * Dashboard empresarial principal de BugBusters Store.
 *
 * - Cabecera principal a ancho completo
 * - Sin panel redundante de accesos rápidos
 * - KPIs reales de negocio
 * - Navegación clicable a módulos
 * - Distribución más limpia y estable
 */
public class DashboardWelcome extends VBox {

    private static final DateTimeFormatter FECHA_FMT =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    private final Runnable abrirArticulos;
    private final Runnable abrirClientes;
    private final Runnable abrirPedidos;

    private Label lblFecha;

    private Label lblArticulosTotal;
    private Label lblStockTotal;
    private Label lblStockCritico;

    private Label lblClientesTotal;
    private Label lblClientesPremium;
    private Label lblPorcentajePremium;

    private Label lblPedidosTotal;
    private Label lblPedidosMes;
    private Label lblPedidosPendientes;

    private Label lblFacturacionTotal;
    private Label lblFacturacionMes;
    private Label lblTicketMedio;

    private Label lblArticuloTop;
    private Label lblUnidadesTop;
    private Label lblSugerenciaStock;
    private Label lblSugerenciaPedidos;
    private Label lblSugerenciaClientes;

    private Timeline fechaTimeline;

    public DashboardWelcome() {
        this(null, null, null);
    }

    public DashboardWelcome(Runnable abrirArticulos, Runnable abrirClientes, Runnable abrirPedidos) {
        this.abrirArticulos = abrirArticulos;
        this.abrirClientes = abrirClientes;
        this.abrirPedidos = abrirPedidos;

        setSpacing(16);
        setPadding(new Insets(18));
        setFillWidth(true);
        setMinWidth(0);
        setMaxWidth(Double.MAX_VALUE);
        setStyle("-fx-background-color: transparent;");

        getChildren().addAll(
                buildHeader(),
                buildKpiRow(),
                buildBusinessRow()
        );

        iniciarActualizacionFecha();
        loadRealStats();
    }

    private Region buildHeader() {
        return buildBrandPanel();
    }

    private StackPane buildBrandPanel() {
        VBox content = contentBox(12, new Insets(26, 34, 26, 34));
        content.setAlignment(Pos.CENTER);

        Label overline = label("CENTRO DE CONTROL EMPRESARIAL", "#67f6ff", 15, true);

        Label title = label("BUGBUSTERS STORE", "#9afbff", 54, true);
        title.setStyle(
                "-fx-text-fill: #9afbff;" +
                        "-fx-font-size: 54px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,240,255,0.42), 24, 0.24, 0, 0);"
        );

        Label subtitle = label(
                "Gestión integral de inventario, clientes, pedidos y facturación",
                "#dceff8",
                18,
                true
        );

        lblFecha = label("", "#8eb6ca", 13, true);
        actualizarFecha();

        HBox domain = new HBox(10);
        domain.setAlignment(Pos.CENTER);
        domain.getChildren().addAll(
                chip("ARTÍCULOS", "#00f0ff"),
                chip("CLIENTES", "#4dffd2"),
                chip("PEDIDOS", "#9d4edd"),
                chip("FACTURACIÓN", "#ff2e88")
        );

        content.getChildren().addAll(overline, title, subtitle, lblFecha, domain);

        StackPane panel = depthPanel(content, "#00f0ff", 26);
        panel.setPrefHeight(235);
        panel.setMinHeight(235);
        panel.setMaxHeight(235);
        panel.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(panel, new Insets(0, 0, 10, 0));

        return panel;
    }

    private Region buildKpiRow() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(25);
            c.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
        }

        lblArticulosTotal = metricValue("#00f0ff");
        lblStockTotal = smallValue("—", "#dceff8");
        lblStockCritico = smallValue("—", "#ffcf5f");

        lblClientesTotal = metricValue("#4dffd2");
        lblClientesPremium = smallValue("—", "#dceff8");
        lblPorcentajePremium = smallValue("—", "#4dffd2");

        lblPedidosTotal = metricValue("#9d4edd");
        lblPedidosMes = smallValue("—", "#dceff8");
        lblPedidosPendientes = smallValue("—", "#ffcf5f");

        lblFacturacionTotal = metricValue("#ff2e88");
        lblFacturacionMes = smallValue("—", "#dceff8");
        lblTicketMedio = smallValue("—", "#ff9bc4");

        grid.add(kpiCard(
                "INVENTARIO",
                lblArticulosTotal,
                "Artículos registrados",
                "#00f0ff",
                new String[]{"Stock total", "Stock crítico"},
                new Label[]{lblStockTotal, lblStockCritico},
                abrirArticulos
        ), 0, 0);

        grid.add(kpiCard(
                "CLIENTES",
                lblClientesTotal,
                "Clientes activos",
                "#4dffd2",
                new String[]{"Premium", "% Premium"},
                new Label[]{lblClientesPremium, lblPorcentajePremium},
                abrirClientes
        ), 1, 0);

        grid.add(kpiCard(
                "PEDIDOS",
                lblPedidosTotal,
                "Pedidos totales",
                "#9d4edd",
                new String[]{"Este mes", "Pendientes"},
                new Label[]{lblPedidosMes, lblPedidosPendientes},
                abrirPedidos
        ), 2, 0);

        grid.add(kpiCard(
                "FACTURACIÓN",
                lblFacturacionTotal,
                "Facturación acumulada",
                "#ff2e88",
                new String[]{"Este mes", "Ticket medio"},
                new Label[]{lblFacturacionMes, lblTicketMedio},
                abrirPedidos
        ), 3, 0);

        return grid;
    }

    private StackPane kpiCard(
            String title,
            Label mainValue,
            String subtitle,
            String accentHex,
            String[] detailTitles,
            Label[] detailValues,
            Runnable action
    ) {
        VBox content = contentBox(9, new Insets(16));

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = label(title, "#67f6ff", 15, true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region dot = new Region();
        dot.setPrefSize(12, 12);
        dot.setStyle(
                "-fx-background-color: " + accentHex + ";" +
                        "-fx-background-radius: 6;" +
                        "-fx-effect: dropshadow(gaussian, " + accentHex + ", 12, 0.55, 0, 0);"
        );

        top.getChildren().addAll(titleLabel, spacer, dot);

        Label sub = label(subtitle, "#a9c5d5", 12, false);

        Canvas spark = buildSparkline(accentHex, 220, 34);

        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(5);

        ColumnConstraints dc1 = new ColumnConstraints();
        dc1.setPercentWidth(50);
        ColumnConstraints dc2 = new ColumnConstraints();
        dc2.setPercentWidth(50);
        details.getColumnConstraints().addAll(dc1, dc2);

        for (int i = 0; i < detailTitles.length && i < detailValues.length; i++) {
            VBox box = new VBox(2);
            box.getChildren().addAll(
                    label(detailTitles[i], "#7f9fb4", 11, true),
                    detailValues[i]
            );
            details.add(box, i, 0);
        }

        content.getChildren().addAll(top, mainValue, sub, spark, details);

        StackPane panel = depthPanel(content, accentHex, 20);
        panel.setPrefHeight(190);
        panel.setMinHeight(190);
        panel.setMaxHeight(190);

        makeClickable(panel, action);

        return panel;
    }

    private Region buildBusinessRow() {
        GridPane row = new GridPane();
        row.setHgap(16);
        row.setVgap(16);
        row.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(34);
        c1.setHgrow(Priority.ALWAYS);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(33);
        c2.setHgrow(Priority.ALWAYS);

        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(33);
        c3.setHgrow(Priority.ALWAYS);

        row.getColumnConstraints().addAll(c1, c2, c3);

        row.add(buildTopProductPanel(), 0, 0);
        row.add(buildOperationalPanel(), 1, 0);
        row.add(buildRecommendationsPanel(), 2, 0);

        return row;
    }

    private StackPane buildTopProductPanel() {
        VBox content = contentBox(14, new Insets(16));
        content.getChildren().add(panelTitle("RENDIMIENTO COMERCIAL", "#00f0ff"));

        lblArticuloTop = label("—", "#ffffff", 16, true);
        lblArticuloTop.setWrapText(true);

        lblUnidadesTop = metricValue("#00f0ff");
        lblUnidadesTop.setText("—");

        content.getChildren().addAll(
                label("Artículo con mayor volumen de venta", "#a9c5d5", 12, false),
                lblArticuloTop,
                label("Unidades vendidas", "#a9c5d5", 12, false),
                lblUnidadesTop
        );

        StackPane panel = depthPanel(content, "#00f0ff", 20);
        panel.setPrefHeight(260);
        panel.setMinHeight(260);
        panel.setMaxHeight(260);

        makeClickable(panel, abrirArticulos);

        return panel;
    }

    private StackPane buildOperationalPanel() {
        VBox content = contentBox(12, new Insets(16));
        content.getChildren().add(panelTitle("ESTADO OPERATIVO", "#4dffd2"));

        lblSugerenciaStock = analysisLine("Inventario pendiente de analizar...");
        lblSugerenciaPedidos = analysisLine("Pedidos pendientes de analizar...");
        lblSugerenciaClientes = analysisLine("Cartera de clientes pendiente de analizar...");

        content.getChildren().addAll(
                lblSugerenciaStock,
                lblSugerenciaPedidos,
                lblSugerenciaClientes
        );

        StackPane panel = depthPanel(content, "#4dffd2", 20);
        panel.setPrefHeight(260);
        panel.setMinHeight(260);
        panel.setMaxHeight(260);

        return panel;
    }

    private StackPane buildRecommendationsPanel() {
        VBox content = contentBox(12, new Insets(16));
        content.getChildren().add(panelTitle("ACCIONES RECOMENDADAS", "#9d4edd"));

        content.getChildren().addAll(
                recommendationButton("Revisar stock crítico", "Ir al módulo de artículos", "#00f0ff", abrirArticulos),
                recommendationButton("Gestionar pedidos pendientes", "Ir al módulo de pedidos", "#9d4edd", abrirPedidos),
                recommendationButton("Analizar cartera premium", "Ir al módulo de clientes", "#4dffd2", abrirClientes)
        );

        StackPane panel = depthPanel(content, "#9d4edd", 20);
        panel.setPrefHeight(260);
        panel.setMinHeight(260);
        panel.setMaxHeight(260);

        return panel;
    }

    private Label analysisLine(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(
                "-fx-text-fill: #dceff8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: normal;" +
                        "-fx-background-color: rgba(255,255,255,0.045);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 12 10 12;" +
                        "-fx-border-color: rgba(0,240,255,0.10);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;"
        );
        return label;
    }

    private Node recommendationButton(String title, String subtitle, String accentHex, Runnable action) {
        VBox box = new VBox(3);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.045);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + rgba(accentHex, 0.18) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;"
        );

        Label titleLabel = label(title, "#ffffff", 13, true);
        Label subLabel = label(subtitle, "#9fc1d4", 12, false);

        box.getChildren().addAll(titleLabel, subLabel);
        makeClickable(box, action);
        return box;
    }

    private void makeClickable(Node node, Runnable action) {
        if (node == null || action == null) return;

        node.setCursor(Cursor.HAND);

        node.setOnMouseEntered(e -> {
            node.setScaleX(1.01);
            node.setScaleY(1.01);
            SoundFX.hover();
        });

        node.setOnMouseExited(e -> {
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });

        node.setOnMouseClicked(e -> {
            SoundFX.navigate();
            action.run();
        });
    }

    private StackPane depthPanel(Region content, String accentHex, int radius) {
        StackPane shell = new StackPane();
        shell.setMaxWidth(Double.MAX_VALUE);

        Region back = new Region();
        back.setTranslateX(4);
        back.setTranslateY(4);
        back.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, rgba(2,8,16,0.90), rgba(7,18,30,0.72));" +
                        "-fx-background-radius: " + radius + ";" +
                        "-fx-border-color: rgba(255,255,255,0.04);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: " + radius + ";"
        );

        StackPane front = new StackPane(content);
        front.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, rgba(10,20,35,0.94), rgba(12,28,46,0.86));" +
                        "-fx-background-radius: " + radius + ";" +
                        "-fx-border-color: " + rgba(accentHex, 0.16) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: " + radius + ";"
        );

        DropShadow outer = new DropShadow();
        outer.setRadius(16);
        outer.setOffsetY(4);
        outer.setColor(Color.rgb(0, 0, 0, 0.34));

        InnerShadow inner = new InnerShadow();
        inner.setRadius(11);
        inner.setColor(Color.web(accentHex, 0.10));
        inner.setInput(outer);

        front.setEffect(inner);

        shell.getChildren().addAll(back, front);
        return shell;
    }

    private VBox contentBox(double spacing, Insets padding) {
        VBox box = new VBox(spacing);
        box.setPadding(padding);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private HBox panelTitle(String text, String accentHex) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Region line = new Region();
        line.setPrefSize(26, 3);
        line.setStyle(
                "-fx-background-color: " + accentHex + ";" +
                        "-fx-background-radius: 3;" +
                        "-fx-effect: dropshadow(gaussian, " + accentHex + ", 8, 0.35, 0, 0);"
        );

        row.getChildren().addAll(line, label(text, "#67f6ff", 15, true));
        return row;
    }

    private Label chip(String text, String accentHex) {
        Label chip = new Label(text);
        chip.setStyle(
                "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-border-color: " + rgba(accentHex, 0.30) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;"
        );
        return chip;
    }

    private Label smallValue(String text, String color) {
        return label(text, color, 13, true);
    }

    private Label metricValue(String accentHex) {
        Label label = new Label("—");
        label.setStyle(
                "-fx-text-fill: #ffffff;" +
                        "-fx-font-size: 31px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, " + accentHex + ", 14, 0.28, 0, 0);"
        );
        return label;
    }

    private Label label(String text, String color, int size, boolean bold) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: " + size + "px;" +
                        "-fx-font-weight: " + (bold ? "bold" : "normal") + ";"
        );
        return label;
    }

    private Canvas buildSparkline(String hex, double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext g = canvas.getGraphicsContext2D();

        Random rng = new Random(hex.hashCode());
        int points = 24;
        double[] values = new double[points];
        double current = 0.45 + rng.nextDouble() * 0.10;

        for (int i = 0; i < points; i++) {
            current += (rng.nextDouble() - 0.45) * 0.12;
            current = Math.max(0.12, Math.min(0.92, current));
            values[i] = current;
        }

        drawSpark(g, width, height, values, hex);
        return canvas;
    }

    private void drawSpark(GraphicsContext g, double width, double height, double[] values, String hex) {
        g.clearRect(0, 0, width, height);

        Color line = Color.web(hex);
        Color fill = Color.color(line.getRed(), line.getGreen(), line.getBlue(), 0.15);

        g.beginPath();
        g.moveTo(0, height);

        for (int i = 0; i < values.length; i++) {
            double x = (i / (double) (values.length - 1)) * width;
            double y = height - values[i] * (height - 6) - 3;
            g.lineTo(x, y);
        }

        g.lineTo(width, height);
        g.closePath();
        g.setFill(fill);
        g.fill();

        g.setStroke(line);
        g.setLineWidth(2);

        g.beginPath();
        for (int i = 0; i < values.length; i++) {
            double x = (i / (double) (values.length - 1)) * width;
            double y = height - values[i] * (height - 6) - 3;
            if (i == 0) g.moveTo(x, y);
            else g.lineTo(x, y);
        }
        g.stroke();
    }

    private void iniciarActualizacionFecha() {
        actualizarFecha();

        fechaTimeline = new Timeline(
                new KeyFrame(Duration.minutes(1), e -> actualizarFecha())
        );
        fechaTimeline.setCycleCount(Timeline.INDEFINITE);
        fechaTimeline.play();
    }

    private void actualizarFecha() {
        if (lblFecha != null) {
            lblFecha.setText(LocalDate.now().format(FECHA_FMT).toUpperCase(Locale.ROOT));
        }
    }

    private void loadRealStats() {
        Task<Stats> task = DashboardStats.loadAsync();

        task.setOnSucceeded(event -> {
            Stats stats = task.getValue() == null ? Stats.empty() : task.getValue();
            Platform.runLater(() -> applyStats(stats));
        });

        task.setOnFailed(event -> Platform.runLater(() -> applyStats(Stats.empty())));

        Thread thread = new Thread(task, "dashboard-stats-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void applyStats(Stats stats) {
        if (stats == null) stats = Stats.empty();

        animateCounter(lblArticulosTotal, stats.articulosTotal, false);
        animateCounter(lblClientesTotal, stats.clientesTotal, false);
        animateCounter(lblPedidosTotal, stats.pedidosTotal, false);

        animateMoney(lblFacturacionTotal, stats.facturacionTotal);

        lblStockTotal.setText(stats.stockTotal + " uds.");
        lblStockCritico.setText(stats.articulosStockCritico + " críticos");

        lblClientesPremium.setText(stats.clientesPremium + " premium");
        lblPorcentajePremium.setText(formatPercent(stats.porcentajeClientesPremium));

        lblPedidosMes.setText(stats.pedidosMes + " este mes");
        lblPedidosPendientes.setText(stats.pedidosPendientes + " pendientes");

        lblFacturacionMes.setText(formatMoney(stats.facturacionMes));
        lblTicketMedio.setText(formatMoney(stats.ticketMedio));

        lblArticuloTop.setText(stats.articuloMasVendido);
        lblUnidadesTop.setText(stats.unidadesArticuloMasVendido + " uds.");

        actualizarSugerencias(stats);
    }

    private void actualizarSugerencias(Stats stats) {
        if (stats.articulosSinStock > 0 || stats.articulosStockCritico > 0) {
            lblSugerenciaStock.setText(
                    "Inventario: " + stats.articulosSinStock + " artículos sin stock y "
                            + stats.articulosStockCritico + " con stock crítico. Conviene revisar reposición."
            );
        } else {
            lblSugerenciaStock.setText("Inventario: no se detectan artículos sin stock ni niveles críticos.");
        }

        if (stats.pedidosPendientes > 0) {
            lblSugerenciaPedidos.setText(
                    "Pedidos: existen " + stats.pedidosPendientes
                            + " pedidos pendientes. Priorizar preparación y actualización de estado."
            );
        } else {
            lblSugerenciaPedidos.setText("Pedidos: no hay pedidos pendientes registrados actualmente.");
        }

        if (stats.clientesTotal > 0) {
            lblSugerenciaClientes.setText(
                    "Clientes: el " + formatPercent(stats.porcentajeClientesPremium)
                            + " de la cartera corresponde a clientes premium."
            );
        } else {
            lblSugerenciaClientes.setText("Clientes: todavía no hay suficientes datos para analizar la cartera.");
        }
    }

    private void animateCounter(Label label, int target, boolean currency) {
        if (label == null) return;

        DoubleProperty value = new SimpleDoubleProperty(0);
        value.addListener((obs, oldVal, newVal) -> {
            int current = newVal.intValue();
            label.setText(currency ? current + " €" : String.valueOf(current));
        });

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(value, 0)),
                new KeyFrame(Duration.millis(900), new KeyValue(value, Math.max(0, target), Interpolator.EASE_OUT))
        );
        timeline.play();
    }

    private void animateMoney(Label label, BigDecimal target) {
        if (label == null) return;

        int rounded = target == null
                ? 0
                : target.setScale(0, RoundingMode.HALF_UP).intValue();

        animateCounter(label, rounded, true);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return value.setScale(2, RoundingMode.HALF_UP) + " €";
    }

    private String formatPercent(double value) {
        return String.format(Locale.GERMANY, "%.1f %%", value);
    }

    private String rgba(String hex, double alpha) {
        Color c = Color.web(hex);
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
    }
}