package Modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Clase que representa un cliente premium de la tienda.
 *
 * Los clientes premium pagan una cuota fija de 30€ y tienen un 20% de descuento
 * en los gastos de envío. Estos valores son fijos y no modificables por el usuario.
 *
 * @author BugBusters
 * @version 1.0
 * @since 1.0
 */
@Entity
@DiscriminatorValue("Premium")
public class ClientePremium extends Cliente {

    public ClientePremium(String email, String nombre, String domicilio, String nif) {
        super(email, nombre, domicilio, nif);
    }

    public ClientePremium() {
    }

    @Override
    public BigDecimal calcularCuota() {
        return BigDecimal.valueOf(30.0);
    }

    @Override
    public BigDecimal descuentoEnvio() {
        return BigDecimal.valueOf(0.20);
    }

    @Override
    public String toString() {
        // Multiplicamos usando BigDecimal y lo pasamos a entero para que salga "20%" sin decimales
        BigDecimal porcentaje = descuentoEnvio().multiply(BigDecimal.valueOf(100));
        return "[Cliente Premium] " +
                "Email: " + getEmail() +
                " | Nombre: " + getNombre() +
                " | Domicilio: " + getDomicilio() +
                " | NIF: " + getNif() +
                " | Cuota: " + String.format("%.2f €", calcularCuota()) +
                " | Descuento: " + porcentaje.intValue() + "%";
    }
}