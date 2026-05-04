package Modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private int idPedido;

    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_articulo", referencedColumnName = "id_articulo", nullable = false)
    private Articulo articulo;

    private int cantidad;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    private String estado;

    public Pedido(int idPedido, Cliente cliente, Articulo articulo, int cantidad, LocalDateTime fechaHora, String estado) {
        this.idPedido = idPedido;
        this.cliente = cliente;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    public Pedido(Cliente cliente, Articulo articulo, int cantidad, LocalDateTime fechaHora, String estado) {
        this.cliente = cliente;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    public Pedido() {
    }

    public boolean puedeCancelar() {
        if (!"PENDIENTE".equalsIgnoreCase(this.estado)) {
            return false;
        }

        LocalDateTime limite = this.fechaHora.plusMinutes(this.articulo.getTiempoPreparacionMin());
        return LocalDateTime.now().isBefore(limite);
    }

    public boolean debeMarcarseComoEnviadoAutomaticamente() {
        return "PENDIENTE".equalsIgnoreCase(this.estado) && !puedeCancelar();
    }

    public BigDecimal calcularTotal() {
        BigDecimal descuento = cliente.descuentoEnvio();
        // Multiplicamos el precio del artículo por la cantidad
        BigDecimal precioTotalArticulos = articulo.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad));
        // Calculamos (1 - descuento). Usamos BigDecimal.ONE que representa un "1" exacto.
        BigDecimal multiplicadorEnvio = BigDecimal.ONE.subtract(descuento);
        // Multiplicamos los gastos de envío base por el multiplicador
        BigDecimal gastosEnvioFinal = articulo.getGastosEnvio().multiply(multiplicadorEnvio);

        // Sumamos el precio de los artículos más los gastos de envío finales
        return precioTotalArticulos.add(gastosEnvioFinal);
    }

    public int getNumeroPedido() {
        return idPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}