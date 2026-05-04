package Factory;

import Excepciones.DAOException;
import DAO.Interfaces.PedidoDAO;
import DAO.Interfaces.ArticuloDAO;
import DAO.Interfaces.ClienteDAO;
import jakarta.persistence.EntityManager;

public abstract class DAOFactory {

    private static DAOFactory instancia;
    public static final int MYSQL = 1;
    public static final int JPA = 2; // Añadido para la nueva implementación JPA

    //Método para obtener la fábrica
    public static DAOFactory getDAOFactory(int tipo) {
        if (instancia == null) {
            //Indicamos que base de datos usamos
            instancia = new MySQLDAOFactory();
        }
        return instancia;
    }
    // --- MÉTODOS PARA OBTENER LOS DAOs ---
    public abstract PedidoDAO getPedidoDAO() throws DAOException;
    public abstract ArticuloDAO getArticuloDAO() throws DAOException;
    public abstract ClienteDAO getClienteDAO() throws DAOException;

    // Sustituimos la Connection de JDBC por el EntityManager de JPA
    public abstract EntityManager getEntityManager() throws DAOException;

    // Gestión de transacciones
    public abstract void iniciarTransaccion() throws DAOException;
    public abstract void confirmarTransaccion() throws DAOException;
    public abstract void cancelarTransaccion() throws DAOException;
}