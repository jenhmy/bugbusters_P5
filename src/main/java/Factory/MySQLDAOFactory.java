package Factory;

import DAO.Interfaces.ArticuloDAO;
import DAO.MySQL.ArticuloDAOJPA;
import Excepciones.DAOException;
import DAO.Interfaces.PedidoDAO;
import DAO.MySQL.PedidoDAOJPA;
import DAO.Interfaces.ClienteDAO;
import DAO.MySQL.ClienteDAOJPA;
import Util.JPAUtil;

import jakarta.persistence.EntityManager;

public class MySQLDAOFactory extends DAOFactory {

    private EntityManager em; // Guardamos el EntityManager activo

    @Override
    public EntityManager getEntityManager() throws DAOException {
        // Si no hay un EntityManager o se cerró, creamos uno nuevo de JPAUtil
        if (em == null || !em.isOpen()) {
            em = JPAUtil.getEntityManager();
        }
        return em;
    }

    @Override
    public ArticuloDAO getArticuloDAO() throws DAOException {
        EntityManager emActual = getEntityManager();
        // Construimos el DAO y le inyectamos el EntityManager
        return new ArticuloDAOJPA(emActual);
    }

    @Override
    public PedidoDAO getPedidoDAO() throws DAOException{
        EntityManager emActual = getEntityManager();
        return new PedidoDAOJPA(emActual);
    }

    @Override
    public ClienteDAO getClienteDAO() throws DAOException {
        // Pedimos la instancia única de EntityManager
        EntityManager emActual = getEntityManager();
        return new ClienteDAOJPA(emActual);
    }

    @Override
    public void iniciarTransaccion() throws DAOException {
        try {
            getEntityManager().getTransaction().begin();
        } catch (Exception e) {
            throw new DAOException("Error al iniciar transacción JPA", e);
        }
    }

    @Override
    public void confirmarTransaccion() throws DAOException {
        try {
            getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            throw new DAOException("Error al confirmar transacción JPA", e);
        }
    }

    @Override
    public void cancelarTransaccion() throws DAOException {
        try {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Limpiar el EM en caso de error para forzar a que getEntityManager() cree uno nuevo y limpio la próxima vez
            if (em != null) {
                em.clear();
            }
        } catch (Exception e) {
            throw new DAOException("Error al cancelar transacción JPA", e);
        }
    }
}
