package Controlador;

import DAO.Interfaces.ArticuloDAO;
import DAO.Interfaces.ClienteDAO;
import DAO.Interfaces.PedidoDAO;
import Factory.DAOFactory;
import Modelo.Articulo;
import Modelo.Cliente;
import Modelo.ClienteEstandar;
import Modelo.ClientePremium;
import Modelo.Pedido;
import Excepciones.DAOException;
import Modelo.Excepciones.*;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

public class Controlador {

    private DAOFactory factory;
    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO;
    private ArticuloDAO articuloDAO;

    public Controlador() {
        try {
            // Aquí en el futuro podríamos cambiar DAOFactory.MYSQL por DAOFactory.JPA o similar
            this.factory = DAOFactory.getDAOFactory(DAOFactory.MYSQL);
            this.clienteDAO = factory.getClienteDAO();
            this.articuloDAO = factory.getArticuloDAO();
            this.pedidoDAO = factory.getPedidoDAO();
        } catch (DAOException e) {
            throw new RuntimeException("Error fatal: No se pudo conectar con la base de datos.", e);
        }
    }

    public Pedido anadirPedido(String email, String codigoArticulo, int cantidad)
            throws DAOException, RecursoNoEncontradoException, EmailInvalidoException {

        emailValido(email);

        if (cantidad <= 0) {
            throw new DAOException("La cantidad del pedido debe ser mayor que 0.");
        }

        // 1. Buscamos los objetos (Ya validados previamente en la Vista)
        Cliente cliente = clienteDAO.buscarPorEmail(email);
        if (cliente == null) throw new RecursoNoEncontradoException("Cliente", email);

        Articulo articulo = articuloDAO.obtenerPorId(codigoArticulo);
        if (articulo == null) throw new RecursoNoEncontradoException("Artículo", codigoArticulo);

        // 2. Verificamos stock una última vez antes de entrar en transacción
        if (articulo.getCantidadDisponible() < cantidad) {
            throw new DAOException("Stock insuficiente. Disponible: " + articulo.getCantidadDisponible());
        }

        try {
            // INICIO DE TRANSACCIÓN ÚNICA
            factory.iniciarTransaccion();

            // 3. RESTAR EL STOCK en el objeto Java
            int nuevoStock = articulo.getCantidadDisponible() - cantidad;
            articulo.setCantidadDisponible(nuevoStock);

            // 4. ACTUALIZAR el artículo en la base de datos
            articuloDAO.actualizar(articulo);

            // 5. CREAR E INSERTAR el pedido
            Pedido nuevoPedido = new Pedido(0, cliente, articulo, cantidad, LocalDateTime.now(), "PENDIENTE");
            pedidoDAO.insertar(nuevoPedido);

            // FIN DE TRANSACCIÓN
            factory.confirmarTransaccion();
            return nuevoPedido;

        } catch (Exception e) {
            // Si algo falla (la resta o la inserción), se deshace todo
            factory.cancelarTransaccion();
            if (e instanceof DAOException) throw (DAOException) e;
            throw new DAOException("Error crítico al procesar el pedido y actualizar stock: " + e.getMessage());
        }
    }

    public void eliminarPedido(int idPedido) throws DAOException, RecursoNoEncontradoException, PedidoNoCancelableException {

        sincronizarEstadosAutomaticos();

        Pedido pedido = pedidoDAO.obtenerPorId(idPedido);
        if (pedido == null) {
            throw new RecursoNoEncontradoException("Pedido", String.valueOf(idPedido));
        }

        if (!pedido.puedeCancelar()) {
            throw new PedidoNoCancelableException(idPedido);
        }

        try {
            factory.iniciarTransaccion();
            pedidoDAO.eliminar(idPedido);
            factory.confirmarTransaccion();

        } catch (DAOException e) {
            factory.cancelarTransaccion();
            throw e;
        } catch (Exception e) {
            factory.cancelarTransaccion();
            throw new DAOException("Error crítico de base de datos: " + e.getMessage());
        }
    }

    public List<Pedido> obtenerPedidosPendientes(String email)
            throws DAOException, RecursoNoEncontradoException, EmailInvalidoException {

        sincronizarEstadosAutomaticos();

        int idCliente = 0;
        if (email != null && !email.trim().isEmpty()) {
            emailValido(email);
            Cliente c = buscarCliente(email);
            idCliente = c.getIdCliente();
        }

        return pedidoDAO.obtenerPedidosPendientes(idCliente);
    }

    public List<Pedido> obtenerPedidosEnviados(String email)
            throws DAOException, RecursoNoEncontradoException, EmailInvalidoException {
        sincronizarEstadosAutomaticos();
        int idFiltro = 0;

        if (email != null && !email.trim().isEmpty()) {
            emailValido(email);
            Cliente c = buscarCliente(email);
            idFiltro = c.getIdCliente();

            if (idFiltro <= 0) {
                // Eliminado new SQLException()
                throw new DAOException("Error de integridad: El ID del cliente no es válido.");
            }
        }

        return pedidoDAO.obtenerPedidosEnviados(idFiltro);
    }

    public void marcarPedidoComoEnviado(int idPedido)
            throws DAOException, RecursoNoEncontradoException, CambioEstadoPedidoNoPermitidoException {

        // 1. Sincronizamos estados por si otros pedidos han caducado
        sincronizarEstadosAutomaticos();

        // 2. Buscamos el pedido
        Pedido pedido = pedidoDAO.obtenerPorId(idPedido);
        if (pedido == null) {
            throw new RecursoNoEncontradoException("Pedido", String.valueOf(idPedido));
        }

        // 3. Validación de lógica de negocio: Si ya está enviado, lanzamos excepción
        if ("ENVIADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new CambioEstadoPedidoNoPermitidoException(
                    "El pedido nº " + idPedido + " ya consta como ENVIADO en el sistema."
            );
        }

        // 4. Ejecución de la transacción
        try {
            factory.iniciarTransaccion();
            // Forzamos el estado a "ENVIADO" internamente
            pedidoDAO.actualizarEstado(idPedido, "ENVIADO");
            factory.confirmarTransaccion();
        } catch (DAOException e) {
            factory.cancelarTransaccion();
            throw e;
        }
    }

    public void sincronizarEstadosAutomaticos() throws DAOException {
        try {
            List<Pedido> pendientes = pedidoDAO.obtenerPedidosPendientes(0);

            LocalDateTime ahora = LocalDateTime.now();

            factory.iniciarTransaccion();

            for (Pedido p : pendientes) {
                LocalDateTime fechaEnvioEstimada = p.getFechaHora().plusMinutes(
                        p.getArticulo().getTiempoPreparacionMin()
                );

                if (ahora.isAfter(fechaEnvioEstimada)) {
                    pedidoDAO.actualizarEstado(p.getNumeroPedido(), "ENVIADO");
                }
            }

            factory.confirmarTransaccion();

        } catch (DAOException e) {
            factory.cancelarTransaccion();
            throw e;
        } catch (Exception e) {
            factory.cancelarTransaccion();
            throw new DAOException("Error inesperado en sincronización: " + e.getMessage());
        }
    }

    private void validarEstadoPedido(String nuevoEstado) throws DAOException {
        if (!"PENDIENTE".equalsIgnoreCase(nuevoEstado) && !"ENVIADO".equalsIgnoreCase(nuevoEstado)) {
            // Eliminado new SQLException()
            throw new DAOException("Estado no válido. Solo se permite PENDIENTE o ENVIADO.");
        }
    }

    public Cliente anadirCliente(String email, String nombre, String domicilio, String nif, int tipoCliente)
            throws DAOException, EmailInvalidoException, TipoClienteInvalidoException {

        try {
            emailValido(email);
            factory.iniciarTransaccion();

            Cliente nuevoCliente;
            if (tipoCliente == 1) {
                nuevoCliente = new ClienteEstandar(email, nombre, domicilio, nif);
            } else if (tipoCliente == 2) {
                nuevoCliente = new ClientePremium(email, nombre, domicilio, nif);
            } else {
                throw new TipoClienteInvalidoException(tipoCliente);
            }

            clienteDAO.insertar(nuevoCliente);

            factory.confirmarTransaccion();

            return nuevoCliente;

        } catch (DAOException | EmailInvalidoException | TipoClienteInvalidoException e) {
            factory.cancelarTransaccion();
            throw e;
        } catch (Exception e) {
            factory.cancelarTransaccion();
            throw new DAOException("Error inesperado al añadir cliente: " + e.getMessage());
        }
    }

    public void existeCliente(String email) throws DAOException {
        if (clienteDAO.existePorEmail(email)) {
            throw new DAOException("El email '" + email + "' ya está registrado.");
        }
    }

    public List<Cliente> obtenerTodosClientes() throws DAOException {
        return clienteDAO.obtenerTodos();
    }

    public List<Cliente> obtenerClientesEstandar() throws DAOException {
        return clienteDAO.obtenerClientesEstandar();
    }

    public List<Cliente> obtenerClientesPremium() throws DAOException {
        return clienteDAO.obtenerClientesPremium();
    }

    public void emailValido(String email) throws EmailInvalidoException {
        if (!email.contains("@") && !email.contains(".")) {
            throw new EmailInvalidoException(email);
        }
    }

    public Cliente buscarCliente(String email) throws EmailInvalidoException, RecursoNoEncontradoException, DAOException {
        emailValido(email);

        Cliente cliente = clienteDAO.buscarPorEmail(email);

        if (cliente == null) {
            throw new RecursoNoEncontradoException("cliente", email);
        }

        return cliente;
    }

    public void eliminarCliente(Cliente cliente) throws DAOException {
        if (cliente != null) {
            try {
                factory.iniciarTransaccion();
                clienteDAO.eliminar(cliente.getIdCliente());
                factory.confirmarTransaccion();
            } catch (DAOException e) {
                factory.cancelarTransaccion();
                throw e;
            }
        }
    }

    public Articulo buscarArticulo(String codigo) throws RecursoNoEncontradoException, DAOException {
        Articulo articulo = articuloDAO.obtenerPorId(codigo);
        if (articulo == null) {
            throw new RecursoNoEncontradoException("Articulo", codigo);
        }
        return articulo;
    }

    public Articulo anadirArticulo(String codigo, String descripcion, BigDecimal precio,
                                   BigDecimal envio, int tiempo, int stock) throws DAOException {

        if (articuloDAO.obtenerPorId(codigo) != null) {
            throw new DAOException("No se puede crear: El artículo con código '" + codigo + "' ya existe.");
        }

        try {
            factory.iniciarTransaccion();
            Articulo nuevo = new Articulo(codigo, descripcion, precio, envio, tiempo, stock);
            articuloDAO.insertar(nuevo);
            factory.confirmarTransaccion();
            return nuevo;
        } catch (DAOException e) {
            factory.cancelarTransaccion();
            throw e;
        }
    }

    public Articulo sumarStockArticulo(String codigo, int cantidad) throws DAOException, RecursoNoEncontradoException {
        try {
            factory.iniciarTransaccion();

            Articulo articulo = articuloDAO.obtenerPorId(codigo);
            if (articulo == null) throw new RecursoNoEncontradoException("Articulo", codigo);

            articuloDAO.sumarStock(codigo, cantidad);

            Articulo actualizado = articuloDAO.obtenerPorId(codigo);

            factory.confirmarTransaccion();
            return actualizado;
        } catch (DAOException e) {
            factory.cancelarTransaccion();
            throw e;
        }
    }

    public void eliminarArticulo(String codigo) throws RecursoNoEncontradoException, DAOException {
        Articulo articulo = articuloDAO.obtenerPorId(codigo);
        if (articulo == null) {
            throw new RecursoNoEncontradoException("Articulo", codigo);
        }

        try {
            factory.iniciarTransaccion();
            articuloDAO.eliminar(codigo);
            factory.confirmarTransaccion();
        } catch (DAOException e) {
            factory.cancelarTransaccion();
            throw e;
        }
    }

    public List<Articulo> obtenerTodosArticulos() throws DAOException {
        return articuloDAO.obtenerTodos();
    }
    public boolean existeArticulo(String codigo) throws DAOException {
        return articuloDAO.existePorCodigo(codigo);
    }
    public boolean hayStockSuficiente(String codigo, int cantidadSolicitada) throws DAOException {
        Articulo art = articuloDAO.obtenerPorId(codigo);
        if (art == null) return false;
        return art.getCantidadDisponible() >= cantidadSolicitada;
    }
}