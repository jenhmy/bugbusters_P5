package Excepciones;

public class DAOException extends Exception {
    // Se usa para errores de BD
    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    // Se usa para poder lanzar errores personalizados (nuestras reglas)
    public DAOException(String mensaje) {
        super(mensaje);
    }
}