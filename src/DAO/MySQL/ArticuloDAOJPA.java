package DAO.MySQL;

import DAO.Interfaces.ArticuloDAO;
import Excepciones.DAOException;
import Modelo.Articulo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.NoResultException;
import java.util.List;


public class ArticuloDAOJPA implements ArticuloDAO {

    // Sustituimos Connection por EntityManager
    private final EntityManager em;

    public ArticuloDAOJPA(EntityManager em) {
        this.em = em;
    }

    @Override
    public void insertar(Articulo articulo) throws DAOException {
        // Adiós al CallableStatement y al procedimiento almacenado
        try {
            em.persist(articulo);
        } catch (Exception e) {
            throw new DAOException("Error al insertar artículo: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existePorCodigo(String codigo) throws DAOException {
        String jpql = "SELECT COUNT(a) FROM Articulo a WHERE a.codigo = :codigo";
        try {
            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("codigo", codigo)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new DAOException("Error al verificar existencia del artículo", e);
        }
    }

    @Override
    public List<Articulo> obtenerTodos() throws DAOException {
        // Usamos JPQL para consultar a la Entidad Articulo, no a la tabla
        try {
            String jpql = "SELECT a FROM Articulo a";
            TypedQuery<Articulo> query = em.createQuery(jpql, Articulo.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new DAOException("Error de JPA al obtener la lista de artículos: " + e.getMessage(), e);
        }
    }

    @Override
    public Articulo obtenerPorId(String codigo) throws DAOException {
        try {
            String jpql = "SELECT a FROM Articulo a WHERE a.codigo = :cod";
            return em.createQuery(jpql, Articulo.class)
                    .setParameter("cod", codigo)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DAOException("Error crítico en la base de datos", e);
        }
    }

    @Override
    public void sumarStock(String codigo, int cantidad) throws DAOException {
        try {
            Articulo articulo = obtenerPorId(codigo);

            if (articulo != null) {
                articulo.setCantidadDisponible(articulo.getCantidadDisponible() + cantidad);
                em.merge(articulo); // Esto guarda el cambio en la base de datos
            } else {
                throw new DAOException("No se puede sumar stock: el artículo '" + codigo + "' no existe.");
            }
        } catch (DAOException e) {
            throw e;
        } catch (Exception e) {
            throw new DAOException("Error al sumar stock del artículo: " + e.getMessage(), e);
        }
    }

    private boolean tienePedidosAsociados(String codigoArticulo) {
        String jpql = "SELECT COUNT(p) FROM Pedido p WHERE p.articulo.codigo = :codigo";

        try {
            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("codigo", codigoArticulo)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void eliminar(String codigo) throws DAOException {
        if (tienePedidosAsociados(codigo)) {
            throw new DAOException("No se puede eliminar el artículo porque tiene pedidos asociados.");
        }

        try {
            Articulo articulo = obtenerPorId(codigo);

            if (articulo == null) {
                throw new DAOException("El artículo con código '" + codigo + "' no existe.");
            }

            em.remove(articulo);
        } catch (DAOException e) {
            throw e;
        } catch (Exception e) {
            throw new DAOException("Error al ejecutar eliminar artículo: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Articulo articulo) throws DAOException {
        try {
            em.merge(articulo);
        } catch (Exception e) {
            throw new DAOException("Error al actualizar el artículo: " + e.getMessage(), e);
        }
    }
}