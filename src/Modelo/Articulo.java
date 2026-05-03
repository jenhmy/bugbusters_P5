package Modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.math.BigDecimal;

/**
 * Clase que representa un artículo de la tienda.
 *
 * Contiene la información básica necesaria para gestionar la venta y el envío de un artículo,
 * incluyendo su precio, gastos de envío, tiempo de preparación y cantidad disponible.
 *
 * @author BugBusters
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "Articulos")
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_articulo")
    private int idArticulo;

    @Column(name = "codigo", unique = true, nullable = false)
    private String codigo;

    private String descripcion;

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "gastos_envio", precision = 10, scale = 2)
    private BigDecimal gastosEnvio;

    @Column(name = "tiempo_preparacion")
    private int tiempoPreparacionMin; // minutos

    @Column(name = "cantidad_disponible")
    private int cantidadDisponible;

    public Articulo(String codigo, String descripcion, BigDecimal precioVenta, BigDecimal gastosEnvio, int tiempoPreparacionMin) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioVenta = precioVenta;
        this.gastosEnvio = gastosEnvio;
        this.tiempoPreparacionMin = tiempoPreparacionMin;
        this.cantidadDisponible = 0;
    }

    public Articulo() {
    }

    public Articulo(String codigo, String descripcion, BigDecimal precioVenta, BigDecimal gastosEnvio,
                    int tiempoPreparacionMin, int cantidadDisponible) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioVenta = precioVenta;
        this.gastosEnvio = gastosEnvio;
        this.tiempoPreparacionMin = tiempoPreparacionMin;
        this.cantidadDisponible = cantidadDisponible;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public BigDecimal getGastosEnvio() {
        return gastosEnvio;
    }

    public void setGastosEnvio(BigDecimal gastosEnvio) {
        this.gastosEnvio = gastosEnvio;
    }

    public int getTiempoPreparacionMin() {
        return tiempoPreparacionMin;
    }

    public void setTiempoPreparacionMin(int tiempoPreparacionMin) {
        this.tiempoPreparacionMin = tiempoPreparacionMin;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public int getIdArticulo() { return idArticulo; }

    public void setIdArticulo(int idArticulo) { this.idArticulo = idArticulo; }

    @Override
    public String toString() {
        return "Artículo: " +
                "Código: " + codigo +
                " | Descripción: " + descripcion +
                " | Precio: " + String.format("%.2f €", precioVenta) +
                " | Gastos envío: " + String.format("%.2f €", gastosEnvio) +
                " | Tiempo preparación: " + tiempoPreparacionMin + " min" +
                " | Stock: " + cantidadDisponible;
    }
}