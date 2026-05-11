package Vista.fx;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Componente 3D decorativo del dashboard principal.
 *
 * Representa un núcleo de datos holográfico inspirado en un servidor / sistema
 * de persistencia, alineado con el contexto del proyecto de gestión BugBusters.
 */
public class Hologram3D extends StackPane {

    private final Group root3D = new Group();
    private final Group coreGroup = new Group();
    private final Group orbitGroup = new Group();
    private final Group panelGroup = new Group();

    public Hologram3D(double width, double height) {
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        setMouseTransparent(true);
        setPickOnBounds(false);

        crearEscena(width, height);
        construirNucleo();
        aplicarAnimacion();
    }

    private void crearEscena(double width, double height) {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(3000);
        camera.setTranslateZ(-540);

        AmbientLight ambientLight = new AmbientLight(Color.web("#6bd8ff", 0.30));

        PointLight cyanLight = new PointLight(Color.web("#00f0ff"));
        cyanLight.setTranslateX(-140);
        cyanLight.setTranslateY(-120);
        cyanLight.setTranslateZ(-220);

        PointLight mintLight = new PointLight(Color.web("#4dffd2"));
        mintLight.setTranslateX(150);
        mintLight.setTranslateY(80);
        mintLight.setTranslateZ(-180);

        root3D.getChildren().addAll(ambientLight, cyanLight, mintLight, coreGroup, orbitGroup, panelGroup);
        root3D.setDepthTest(DepthTest.ENABLE);
        root3D.setTranslateX(width / 2.0);
        root3D.setTranslateY(height / 2.0);

        SubScene subScene = new SubScene(root3D, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);
        subScene.setCamera(camera);

        getChildren().add(subScene);
    }

    private void construirNucleo() {
        coreGroup.getChildren().clear();
        orbitGroup.getChildren().clear();
        panelGroup.getChildren().clear();

        PhongMaterial materialCyan = material("#00f0ff", 0.95);
        PhongMaterial materialMint = material("#4dffd2", 0.95);
        PhongMaterial materialBlue = material("#7de3ff", 0.95);
        PhongMaterial materialViolet = material("#9d4edd", 0.92);

        // Columna central holográfica
        Cylinder energyBeam = new Cylinder(2.4, 108);
        energyBeam.setMaterial(materialCyan);
        energyBeam.setEffect(new DropShadow(18, Color.web("#00f0ff")));
        coreGroup.getChildren().add(energyBeam);

        // Plataforma base
        Cylinder base = new Cylinder(34, 5);
        base.setMaterial(materialBlue);
        base.setTranslateY(34);
        base.setEffect(new DropShadow(14, Color.web("#7de3ff", 0.35)));
        coreGroup.getChildren().add(base);

        // Apilado tipo servidor / base de datos
        for (int i = 0; i < 4; i++) {
            Cylinder stack = new Cylinder(20 + (i % 2), 6);
            stack.setMaterial(i % 2 == 0 ? materialMint : materialBlue);
            stack.setTranslateY(18 - i * 12);
            stack.setEffect(new DropShadow(14, Color.web(i % 2 == 0 ? "#4dffd2" : "#7de3ff", 0.45)));
            coreGroup.getChildren().add(stack);
        }

        // Núcleo central
        Sphere core = new Sphere(9);
        core.setMaterial(materialMint);
        core.setTranslateY(-10);
        core.setEffect(new DropShadow(24, Color.web("#4dffd2")));
        coreGroup.getChildren().add(core);

        // Halo superior
        Sphere halo = new Sphere(15);
        halo.setMaterial(material("#00f0ff", 0.14));
        halo.setDrawMode(DrawMode.LINE);
        halo.setTranslateY(-10);
        halo.setEffect(new DropShadow(16, Color.web("#00f0ff", 0.40)));
        coreGroup.getChildren().add(halo);

        // Anillos orbitales
        orbitGroup.getChildren().add(crearAnillo(54, materialCyan, Rotate.X_AXIS, 90));
        orbitGroup.getChildren().add(crearAnillo(44, materialMint, Rotate.Y_AXIS, 70));
        orbitGroup.getChildren().add(crearAnillo(34, materialViolet, Rotate.Z_AXIS, 0));

        // Satélites
        orbitGroup.getChildren().add(crearSatelite(64, 0, 0, 4.0, materialViolet, "#9d4edd"));
        orbitGroup.getChildren().add(crearSatelite(-64, 0, 0, 3.8, materialCyan, "#00f0ff"));
        orbitGroup.getChildren().add(crearSatelite(0, -58, 16, 3.7, materialMint, "#4dffd2"));
        orbitGroup.getChildren().add(crearSatelite(0, 58, -16, 3.7, materialBlue, "#7de3ff"));

        // Paneles flotantes tipo HUD
        panelGroup.getChildren().add(crearPanelFlotante(-74, -18, 36, -22, materialCyan, "#00f0ff"));
        panelGroup.getChildren().add(crearPanelFlotante(74, -4, -34, 24, materialMint, "#4dffd2"));
        panelGroup.getChildren().add(crearPanelFlotante(-54, 42, -26, -12, materialBlue, "#7de3ff"));
        panelGroup.getChildren().add(crearPanelFlotante(56, 36, 28, 14, materialViolet, "#9d4edd"));

        // Conectores a paneles
        crearConector(new Point3D(0, -10, 0), new Point3D(-74, -18, 36), materialCyan, 0.65);
        crearConector(new Point3D(0, -10, 0), new Point3D(74, -4, -34), materialMint, 0.65);
        crearConector(new Point3D(0, -10, 0), new Point3D(-54, 42, -26), materialBlue, 0.55);
        crearConector(new Point3D(0, -10, 0), new Point3D(56, 36, 28), materialViolet, 0.55);
    }

    private Cylinder crearAnillo(double radio, PhongMaterial material, Point3D axis, double angle) {
        Cylinder ring = new Cylinder(radio, 0.9);
        ring.setMaterial(material);
        ring.setDrawMode(DrawMode.LINE);
        ring.getTransforms().add(new Rotate(angle, axis));
        ring.setEffect(new DropShadow(14, Color.web("#00f0ff", 0.25)));
        return ring;
    }

    private Sphere crearSatelite(double x, double y, double z, double radius, PhongMaterial material, String glow) {
        Sphere sphere = new Sphere(radius);
        sphere.setMaterial(material);
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);
        sphere.setEffect(new DropShadow(14, Color.web(glow)));
        return sphere;
    }

    private Box crearPanelFlotante(double x, double y, double z, double rotateY, PhongMaterial material, String glow) {
        Box panel = new Box(34, 18, 2.4);
        panel.setMaterial(material);
        panel.setDrawMode(DrawMode.LINE);
        panel.setTranslateX(x);
        panel.setTranslateY(y);
        panel.setTranslateZ(z);
        panel.setRotationAxis(Rotate.Y_AXIS);
        panel.setRotate(rotateY);
        panel.setEffect(new DropShadow(14, Color.web(glow, 0.42)));
        return panel;
    }

    private void crearConector(Point3D from, Point3D to, PhongMaterial material, double radius) {
        Point3D diff = to.subtract(from);
        double height = diff.magnitude();

        if (height <= 0.0001) {
            return;
        }

        Point3D mid = from.midpoint(to);

        Cylinder line = new Cylinder(radius, height);
        line.setMaterial(material);
        line.setTranslateX(mid.getX());
        line.setTranslateY(mid.getY());
        line.setTranslateZ(mid.getZ());

        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D direction = diff.normalize();
        Point3D rotationAxis = yAxis.crossProduct(direction);

        double dot = yAxis.dotProduct(direction);
        dot = Math.max(-1.0, Math.min(1.0, dot));
        double angle = Math.toDegrees(Math.acos(dot));

        if (rotationAxis.magnitude() > 0.0001) {
            line.getTransforms().add(new Rotate(angle, rotationAxis));
        } else if (dot < 0) {
            line.getTransforms().add(new Rotate(180, Rotate.X_AXIS));
        }

        coreGroup.getChildren().add(line);
    }

    private PhongMaterial material(String colorHex, double opacity) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.web(colorHex, opacity));
        material.setSpecularColor(Color.web("#ffffff", 0.55));
        material.setSpecularPower(34);
        return material;
    }

    private void aplicarAnimacion() {
        RotateTransition coreRotation = new RotateTransition(Duration.seconds(18), coreGroup);
        coreRotation.setAxis(Rotate.Y_AXIS);
        coreRotation.setByAngle(360);
        coreRotation.setCycleCount(Animation.INDEFINITE);
        coreRotation.setInterpolator(Interpolator.LINEAR);
        coreRotation.play();

        RotateTransition coreTilt = new RotateTransition(Duration.seconds(7), coreGroup);
        coreTilt.setAxis(Rotate.X_AXIS);
        coreTilt.setFromAngle(-9);
        coreTilt.setToAngle(9);
        coreTilt.setAutoReverse(true);
        coreTilt.setCycleCount(Animation.INDEFINITE);
        coreTilt.setInterpolator(Interpolator.EASE_BOTH);
        coreTilt.play();

        RotateTransition orbitRotation = new RotateTransition(Duration.seconds(9), orbitGroup);
        orbitRotation.setAxis(Rotate.Z_AXIS);
        orbitRotation.setByAngle(-360);
        orbitRotation.setCycleCount(Animation.INDEFINITE);
        orbitRotation.setInterpolator(Interpolator.LINEAR);
        orbitRotation.play();

        RotateTransition orbitYRotation = new RotateTransition(Duration.seconds(12), orbitGroup);
        orbitYRotation.setAxis(Rotate.Y_AXIS);
        orbitYRotation.setByAngle(360);
        orbitYRotation.setCycleCount(Animation.INDEFINITE);
        orbitYRotation.setInterpolator(Interpolator.LINEAR);
        orbitYRotation.play();

        RotateTransition panelsRotation = new RotateTransition(Duration.seconds(14), panelGroup);
        panelsRotation.setAxis(Rotate.Y_AXIS);
        panelsRotation.setByAngle(-360);
        panelsRotation.setCycleCount(Animation.INDEFINITE);
        panelsRotation.setInterpolator(Interpolator.LINEAR);
        panelsRotation.play();
    }
}