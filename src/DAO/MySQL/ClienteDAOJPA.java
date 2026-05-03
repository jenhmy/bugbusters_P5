package DAO.MySQL;

import DAO.Interfaces.ClienteDAO;
import Excepciones.DAOException;
import Modelo.Cliente;

// Ya no necesitamos importar ClienteEstandar y ClientePremium para instanciarlos a mano
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class ClienteDAOJPA implements ClienteDAO {

    // Sustituimos Connection por EntityManager
    private final EntityManager em;

    public ClienteDAOJPA(EntityManager em) {
        this.em = em;
    }

    // El método crearSegunTipo ha sido eliminado: JPA mapea la herencia automáticamente.

    @Override
    public List<Cliente> obtenerClientesEstandar() throws DAOException{
        try {
            // Al consultar directamente la entidad ClienteEstandar, JPA filtra por nosotros
            String jpql = "SELECT c FROM ClienteEstandar c";
            return em.createQuery(jpql, Cliente.class).getResultList();
        } catch (Exception e) {
            throw new DAOException("Error al obtener clientes estándar", e);
        }
    }

    @Override
    public List<Cliente> obtenerClientesPremium() throws DAOException{
        try {
            String jpql = "SELECT c FROM ClientePremium c";
            return em.createQuery(jpql, Cliente.class).getResultList();
        } catch (Exception e) {
            throw new DAOException("Error al obtener clientes premium", e);
        }
    }

    @Override
    public void insertar(Cliente cliente) throws DAOException {
        // Adiós al CallableStatement y a la comprobación de instancia manual
        try {
            em.persist(cliente);
        } catch (Exception e) {
            throw new DAOException("Error al insertar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> obtenerTodos() throws DAOException {
        try {
            String jpql = "SELECT c FROM Cliente c";
            return em.createQuery(jpql, Cliente.class).getResultList();
        } catch (Exception e) {
            throw new DAOException("Error al obtener la lista de clientes", e);
        }
    }

    @Override
    public Cliente obtenerPorId(Integer id) throws DAOException {
        try {
            return em.find(Cliente.class, id);
        } catch (Exception e) {
            throw new DAOException("Error al obtener cliente por ID: " + id, e);
        }
    }

    private boolean tienePedidosAsociados(Integer idCliente) {
        // Navegamos por la relación orientada a objetos (p.cliente.idCliente)
        String jpql = "SELECT COUNT(p) FROM Pedido p WHERE p.cliente.idCliente = :id";
        try {
            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("id", idCliente)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void eliminar(Integer idCliente) throws DAOException {
        if (tienePedidosAsociados(idCliente)) {
            throw new DAOException("No se puede eliminar el cliente porque tiene pedidos asociados.");
        }

        try {
            Cliente cliente = em.find(Cliente.class, idCliente);
            if (cliente != null) {
                em.remove(cliente);
            } else {
                throw new DAOException("El cliente con ID " + idCliente + " no existe.");
            }
        } catch (Exception e) {
            throw new DAOException("Error al eliminar cliente (ID: " + idCliente + ") en la base de datos.", e);
        }
    }

    @Override
    public boolean existePorEmail(String email) throws DAOException {
        String jpql = "SELECT COUNT(c) FROM Cliente c WHERE c.email = :email";
        try {
            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new DAOException("Error al comprobar email", e);
        }
    }

    @Override
    public Cliente buscarPorEmail(String email) throws DAOException {
        String jpql = "SELECT c FROM Cliente c WHERE c.email = :email";
        try {
            return em.createQuery(jpql, Cliente.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Comportamiento esperado si no hay resultados
        } catch (Exception e) {
            throw new DAOException("Error al buscar cliente por email", e);
        }
    }
    @Override
    public void actualizar(Cliente cliente) throws DAOException {
        try {
            em.merge(cliente);
        } catch (Exception e) {
            throw new DAOException("Error al actualizar el cliente: " + e.getMessage(), e);
        }
    }
}