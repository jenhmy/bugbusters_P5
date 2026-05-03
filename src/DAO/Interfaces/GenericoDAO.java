package DAO.Interfaces;

import java.util.List;
import Excepciones.DAOException;

// T es la Entidad, K es la Clave Primaria (Primary Key)
public interface GenericoDAO<T, K> {

    void insertar(T entidad) throws DAOException;
    List<T> obtenerTodos() throws DAOException;
    T obtenerPorId(K id) throws DAOException;
    void eliminar(K clave) throws DAOException;
    void actualizar(T entidad) throws DAOException;
}