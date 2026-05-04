package Modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Clase que representa un cliente estándar de la tienda.
 *
 * Los clientes estándar no pagan cuota ni tienen descuento en los gastos de envío.
 * Hereda de la clase abstracta Cliente e implementa los métodos abstractos.
 *
 * @author BugBusters
 * @version 1.0
 * @since 1.0
 */
@Entity
@DiscriminatorValue("Estandar")
public class ClienteEstandar extends Cliente {

    public ClienteEstandar(String email, String nombre, String domicilio, String nif) {
        super(email, nombre, domicilio, nif);
    }

    public ClienteEstandar() {
    }

    @Override
    public BigDecimal calcularCuota() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal descuentoEnvio() {
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "[Cliente Estandar] " +
                "Email: " + getEmail() +
                " | Nombre: " + getNombre() +
                " | Domicilio: " + getDomicilio() +
                " | NIF: " + getNif();
    }
}