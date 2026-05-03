package DAO.Interfaces;

import Excepciones.DAOException;
import Modelo.Articulo;

public interface ArticuloDAO extends GenericoDAO<Articulo, String> {

    void sumarStock(String codigo, int cantidad) throws DAOException;
    boolean existePorCodigo(String codigo) throws DAOException;
}