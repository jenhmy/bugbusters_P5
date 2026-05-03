package DAO.Interfaces;

import Modelo.Pedido;
import Excepciones.DAOException;

import java.util.List;

public interface PedidoDAO extends GenericoDAO<Pedido, Integer> {

    List<Pedido> obtenerPedidosPendientes(int idCliente) throws DAOException;

    List<Pedido> obtenerPedidosEnviados(int idCliente) throws DAOException;

    void actualizarEstado(int idPedido, String nuevoEstado) throws DAOException;
}
