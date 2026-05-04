package DAO.MySQL;

import DAO.Interfaces.PedidoDAO;
import Excepciones.DAOException;
import Modelo.Pedido;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class PedidoDAOJPA implements PedidoDAO {

    // Sustituimos Connection por EntityManager
    private final EntityManager em;

    public PedidoDAOJPA(EntityManager em) {
        this.em = em;
    }

    @Override
    public void insertar(Pedido pedido) throws DAOException {
        // Adiós al CallableStatement. JPA se encarga de guardar las relaciones automáticamente.
        try {
            em.persist(pedido);
        } catch (Exception e) {
            throw new DAOException("Error al insertar pedido: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer idPedido) throws DAOException {
        try {
            Pedido pedido = em.find(Pedido.class, idPedido);
            if (pedido != null) {
                em.remove(pedido);
            } else {
                throw new DAOException("El pedido con ID " + idPedido + " no existe.");
            }
        } catch (Exception e) {
            throw new DAOException("Error al eliminar el pedido con ID: " + idPedido, e);
        }
    }

    @Override
    public List<Pedido> obtenerTodos() throws DAOException {
        try {
            String jpql = "SELECT p FROM Pedido p ORDER BY p.idPedido";
            return em.createQuery(jpql, Pedido.class).getResultList();
        } catch (Exception e) {
            throw new DAOException("Error al obtener todos los pedidos", e);
        }
    }

    @Override
    public List<Pedido> obtenerPedidosPendientes(int idCliente) throws DAOException {
        return cargarPedidos(idCliente, "PENDIENTE");
    }

    @Override
    public List<Pedido> obtenerPedidosEnviados(int idCliente) throws DAOException {
        return cargarPedidos(idCliente, "ENVIADO");
    }

    private List<Pedido> cargarPedidos(int idCliente, String estado) throws DAOException {
        try {
            StringBuilder jpql = new StringBuilder("SELECT p FROM Pedido p WHERE p.estado = :estado");

            if (idCliente > 0) {
                // Navegamos directamente al idCliente del objeto Cliente asociado
                jpql.append(" AND p.cliente.idCliente = :idCliente");
            }
            jpql.append(" ORDER BY p.idPedido");

            TypedQuery<Pedido> query = em.createQuery(jpql.toString(), Pedido.class);
            query.setParameter("estado", estado);

            if (idCliente > 0) {
                query.setParameter("idCliente", idCliente);
            }

            return query.getResultList();
        } catch (Exception e) {
            throw new DAOException("Error al obtener pedidos filtrados", e);
        }
    }

    @Override
    public Pedido obtenerPorId(Integer id) throws DAOException {
        try {
            return em.find(Pedido.class, id);
        } catch (Exception e) {
            throw new DAOException("Error al obtener el pedido con id: " + id, e);
        }
    }

    @Override
    public void actualizar(Pedido pedido) throws DAOException {
        try {
            em.merge(pedido);
        } catch (Exception e) {
            throw new DAOException("Error al actualizar el pedido", e);
        }
    }

    @Override
    public void actualizarEstado(int idPedido, String nuevoEstado) throws DAOException {
        try {
            Pedido pedido = em.find(Pedido.class, idPedido);
            if (pedido != null) {
                pedido.setEstado(nuevoEstado);
                em.merge(pedido);
            } else {
                throw new DAOException("No se encontró el pedido con id: " + idPedido);
            }
        } catch (Exception e) {
            throw new DAOException("Error al actualizar el estado del pedido", e);
        }
    }
}