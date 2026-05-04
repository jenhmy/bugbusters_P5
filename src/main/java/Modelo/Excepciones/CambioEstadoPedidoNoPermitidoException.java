package Modelo.Excepciones;

public class CambioEstadoPedidoNoPermitidoException extends Exception {

    public CambioEstadoPedidoNoPermitidoException(String mensaje) {
        super(mensaje);
    }

    public CambioEstadoPedidoNoPermitidoException(int numeroPedido, String estadoActual, String nuevoEstado) {
        super("No se puede cambiar el pedido con número " + numeroPedido +
                " de '" + estadoActual + "' a '" + nuevoEstado +
                "'. Un pedido enviado no puede volver a pendiente.");
    }
}