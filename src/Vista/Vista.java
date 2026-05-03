package Vista;

import Controlador.Controlador;
import Excepciones.DAOException;
import Modelo.Articulo;
import Modelo.Cliente;
import Modelo.Pedido;
import Modelo.Excepciones.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Vista {

    private final Scanner teclado;
    private final Controlador controlador;

    public Vista() {
        teclado = new Scanner(System.in);
        controlador = new Controlador();
    }

    public void iniciar() {
        int opcion;

        TerminalUI.showWelcome();

        do {
            mostrarMenuPrincipal();
            opcion = leerEntero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    menuArticulos();
                    break;
                case 2:
                    menuClientes();
                    break;
                case 3:
                    menuPedidos();
                    break;
                case 0:
                    TerminalUI.info("Saliendo del programa...");
                    TerminalUI.showGoodbye();
                    break;
                default:
                    TerminalUI.error("Opción no válida.");
            }

        } while (opcion != 0);
    }

    private void mostrarMenuPrincipal() {
        TerminalUI.showMenu("MENÚ PRINCIPAL", new String[]{
                "1. Gestión de artículos",
                "2. Gestión de clientes",
                "3. Gestión de pedidos",
                "0. Salir"
        });
    }

    private void menuArticulos() {
        int opcion;

        do {
            TerminalUI.showMenu("GESTIÓN DE ARTÍCULOS", new String[]{
                    "1. Añadir artículo",
                    "2. Mostrar artículos",
                    "3. Añadir stock a artículo existente",
                    "4. Eliminar artículo",
                    "0. Volver"
            });

            opcion = leerEntero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    anadirArticulo();
                    break;
                case 2:
                    mostrarArticulos();
                    break;
                case 3:
                    anadirStockArticulo();
                    break;
                case 4:
                    eliminarArticulo();
                    break;
                case 0:
                    break;
                default:
                    TerminalUI.error("Opción no válida.");
            }

        } while (opcion != 0);
    }

    private void anadirArticulo() {
        TerminalUI.sectionTitle("AÑADIR ARTÍCULO");
        String codigo = leerTextoNoVacio("Código: ");

        try {
            // 1. Preguntamos al controlador si existe
            Articulo existente = controlador.buscarArticulo(codigo);

            // Si no saltó la excepción RecursoNoEncontrado, es que EXISTE
            TerminalUI.warning("El artículo ya existe.");
            TerminalUI.showArticleCard(existente);

            if (leerTextoNoVacio("¿Añadir stock? (S/N): ").equalsIgnoreCase("S")) {
                int cantidad = leerEntero("Cantidad a sumar: ");
                // El controlador hace el trabajo sucio, la vista solo muestra el resultado
                Articulo actualizado = controlador.sumarStockArticulo(codigo, cantidad);
                TerminalUI.success("Stock actualizado.");
                TerminalUI.showArticleCard(actualizado);
            }
            return;

        } catch (RecursoNoEncontradoException e) {
            // 2. Si NO existe, el controlador lanzó esta excepción y aquí procedemos al alta
            try {
                String desc = leerTextoNoVacio("Descripción: ");
                BigDecimal precio = leerBigDecimal("Precio: ");
                BigDecimal envio = leerBigDecimal("Envío: ");
                int tiempo = leerEntero("Minutos prep: ");
                int stock = leerEntero("Stock inicial: ");

                // El controlador crea e inserta
                Articulo nuevo = controlador.anadirArticulo(codigo, desc, precio, envio, tiempo, stock);

                TerminalUI.success("Artículo creado.");
                TerminalUI.showArticleCard(nuevo);

            } catch (DAOException ex) {
                TerminalUI.exception(ex.getMessage());
            }
        } catch (DAOException e) {
            TerminalUI.exception(e.getMessage());
        }
    }

    private void mostrarArticulos() {
        TerminalUI.sectionTitle("LISTADO DE ARTÍCULOS");

        try {
            TerminalUI.showArticlesTable(controlador.obtenerTodosArticulos());
        } catch (DAOException e) {
            TerminalUI.error("Error al conectar con la base de datos para obtener el listado: " + e.getMessage());
        }
    }

    private void anadirStockArticulo() {
        TerminalUI.sectionTitle("AÑADIR STOCK A ARTÍCULO EXISTENTE");

        String codigo = leerTextoNoVacio("Código del artículo: ");

        try {
            Articulo articulo = controlador.buscarArticulo(codigo);

            TerminalUI.info("Artículo localizado correctamente.");
            TerminalUI.showArticleCard(articulo);

            int cantidad = leerEntero("Cantidad a añadir al stock: ");
            controlador.sumarStockArticulo(codigo, cantidad);

            Articulo articuloActualizado = controlador.buscarArticulo(codigo);
            TerminalUI.success("¡Stock actualizado correctamente!");
            TerminalUI.info("Stock actual del artículo: " + articuloActualizado.getCantidadDisponible() + " unidades");
            TerminalUI.showArticleCard(articuloActualizado);
            TerminalUI.spotlight("STOCK ACTUALIZADO");

        } catch (RecursoNoEncontradoException | DAOException e) {
            TerminalUI.exception(e.getMessage());
        }
    }

    private void eliminarArticulo() {
        TerminalUI.sectionTitle("ELIMINAR ARTÍCULO");

        String codigo = leerTextoNoVacio("Introduce el código del artículo: ");

        try {
            Articulo articulo = controlador.buscarArticulo(codigo);

            TerminalUI.info("Artículo localizado correctamente.");
            TerminalUI.showArticleCard(articulo);

            String conf = leerTextoNoVacio("¿Estás seguro de eliminar este artículo? (S/N): ");
            if (!conf.equalsIgnoreCase("S")) {
                TerminalUI.warning("Operación cancelada.");
                return;
            }

            controlador.eliminarArticulo(codigo);

            TerminalUI.success("¡Artículo eliminado!");
            TerminalUI.spotlight("OPERACIÓN FINALIZADA");

        } catch (RecursoNoEncontradoException | DAOException e) {
            TerminalUI.exception(e.getMessage());
        }
    }

    private void menuClientes() {
        int opcion;

        do {
            TerminalUI.showMenu("GESTIÓN DE CLIENTES", new String[]{
                    "1. Añadir cliente",
                    "2. Buscar cliente",
                    "3. Mostrar todos los clientes",
                    "4. Mostrar clientes estándar",
                    "5. Mostrar clientes premium",
                    "6. Eliminar cliente",
                    "0. Volver"
            });

            opcion = leerEntero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    anadirCliente();
                    break;
                case 2:
                    buscarCliente();
                    break;
                case 3:
                    obtenerTodosClientes();
                    break;
                case 4:
                    obtenerClientesEstandar();
                    break;
                case 5:
                    obtenerClientesPremium();
                    break;
                case 6:
                    eliminarCliente();
                    break;
                case 0:
                    break;
                default:
                    TerminalUI.error("Opción no válida.");
            }

        } while (opcion != 0);
    }

    private void anadirCliente() {
        TerminalUI.sectionTitle("AÑADIR CLIENTE");

        try {
            String email = leerTextoNoVacio("Email: ");

            controlador.emailValido(email);
            controlador.existeCliente(email);

            String nombre = leerTextoNoVacio("Nombre: ");
            String domicilio = leerTextoNoVacio("Domicilio: ");
            String nif = leerTextoNoVacio("NIF: ");
            int tipoCliente = leerEntero("Tipo de cliente (1- Estándar, 2- Premium): ");

            Cliente nuevo = controlador.anadirCliente(email, nombre, domicilio, nif, tipoCliente);

            TerminalUI.success("¡Cliente añadido correctamente!");
            TerminalUI.showClientCard(nuevo);

        } catch (DAOException | EmailInvalidoException | TipoClienteInvalidoException e) {
            TerminalUI.exception(e.getMessage());
        }

        TerminalUI.sciFiDivider();
    }

    private void buscarCliente() {
        TerminalUI.sectionTitle("BUSCAR CLIENTE");

        String email = leerTextoNoVacio("Introduce el Email del cliente: ");

        try {
            Cliente clienteEncontrado = controlador.buscarCliente(email);
            TerminalUI.showClientCard(clienteEncontrado);

        } catch (EmailInvalidoException | RecursoNoEncontradoException | DAOException e) {
            TerminalUI.exception(e.getMessage());
        }
    }

    private void obtenerTodosClientes() {
        TerminalUI.sectionTitle("LISTADO DE TODOS LOS CLIENTES");

        try {
            List<Cliente> lista = controlador.obtenerTodosClientes();
            imprimirClientes("No hay clientes registrados.", lista);

        } catch (DAOException e) {
            TerminalUI.exception("Error al acceder a los datos: " + e.getMessage());
        }

        TerminalUI.sciFiDivider();
    }

    private void obtenerClientesEstandar() {
        TerminalUI.sectionTitle("LISTADO DE CLIENTES ESTÁNDAR");
        try {
            List<Cliente> lista = controlador.obtenerClientesEstandar();
            imprimirClientes("No hay clientes estándar registrados.", lista);
        } catch (DAOException e) {
            // Cambiado error por exception para ser coherentes
            TerminalUI.exception("Error al recuperar los clientes: " + e.getMessage());
        }
        TerminalUI.sciFiDivider(); // Añadido
    }

    private void obtenerClientesPremium() {
        TerminalUI.sectionTitle("LISTADO DE CLIENTES PREMIUM");
        try {
            imprimirClientes("No hay clientes premium registrados.", controlador.obtenerClientesPremium());
        } catch (DAOException e) {
            TerminalUI.exception(e.getMessage());
        }
        TerminalUI.sciFiDivider(); // Añadido
    }

    private void eliminarCliente() {
        TerminalUI.sectionTitle("ELIMINAR CLIENTE");
        String email = leerTextoNoVacio("Introduce el Email del cliente a eliminar: ");

        try {
            Cliente aEliminar = controlador.buscarCliente(email);

            TerminalUI.info("Cliente localizado correctamente.");
            TerminalUI.showClientCard(aEliminar);

            String conf = leerTextoNoVacio("¿Estás seguro de eliminar a este cliente? (S/N): ");
            if (!conf.equalsIgnoreCase("S")) return;

            controlador.eliminarCliente(aEliminar);

            TerminalUI.success("¡Cliente eliminado con éxito!");

        } catch (EmailInvalidoException | RecursoNoEncontradoException | DAOException e) {
            TerminalUI.exception(e.getMessage());
        }
    }

    private void menuPedidos() {
        int opcion;

        do {
            TerminalUI.showMenu("GESTIÓN DE PEDIDOS", new String[]{
                    "1. Añadir pedido",
                    "2. Eliminar pedido",
                    "3. Mostrar pedidos pendientes",
                    "4. Mostrar pedidos enviados",
                    "5. Marcar pedido como enviado",
                    "0. Volver"
            });

            opcion = leerEntero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    anadirPedido();
                    break;
                case 2:
                    eliminarPedido();
                    break;
                case 3:
                    mostrarPedidosPendientes();
                    break;
                case 4:
                    mostrarPedidosEnviados();
                    break;
                case 5:
                    cambiarEstadoPedido();
                    break;
                case 0:
                    break;
                default:
                    TerminalUI.error("Opción no válida.");
            }

        } while (opcion != 0);
    }

    private void anadirPedido() {
        TerminalUI.sectionTitle("AÑADIR PEDIDO");
        String emailCliente = leerTextoNoVacio("Email del cliente: ");
        Cliente cliente = null;

        try {
            cliente = controlador.buscarCliente(emailCliente);
            TerminalUI.info("Cliente encontrado.");
            TerminalUI.showClientCard(cliente);

        } catch (RecursoNoEncontradoException e) {
            TerminalUI.warning("El cliente no existe. ¿Desea crearlo? (s/n): ");
            String respuesta = leerTextoNoVacio("");

            if (respuesta.equalsIgnoreCase("s")) {
                TerminalUI.info("Procedemos a la creación del cliente.");
                String nombre = leerTextoNoVacio("Nombre: ");
                String domicilio = leerTextoNoVacio("Domicilio: ");
                String nif = leerTextoNoVacio("NIF: ");
                int tipoSeleccionado = leerEntero("Tipo cliente (1-Estándar, 2-Premium): ");

                try {
                    cliente = controlador.anadirCliente(emailCliente, nombre, domicilio, nif, tipoSeleccionado);
                    TerminalUI.success("¡Cliente creado correctamente!");
                } catch (TipoClienteInvalidoException | DAOException | EmailInvalidoException ex) {
                    TerminalUI.exception(ex.getMessage());
                    return;
                }
            } else {
                TerminalUI.error("Operación cancelada.");
                return;
            }
        } catch (EmailInvalidoException | DAOException e) {
            TerminalUI.exception(e.getMessage());
            return;
        }

        try {
            TerminalUI.info("Datos del pedido:");
            String codigoArticulo = leerTextoNoVacio("Código del artículo: ");

            if (!controlador.existeArticulo(codigoArticulo)) {
                throw new RecursoNoEncontradoException("Artículo", codigoArticulo);
            }

            int cantidad = leerEntero("Cantidad: ");

            Pedido pedidoRealizado = controlador.anadirPedido(emailCliente, codigoArticulo, cantidad);

            TerminalUI.success("¡Pedido realizado con éxito!");
            TerminalUI.showOrderCard(pedidoRealizado);

        } catch (DAOException | RecursoNoEncontradoException | EmailInvalidoException e) {
            TerminalUI.exception(e.getMessage());
        }

        TerminalUI.sciFiDivider();
    }

    private void eliminarPedido() {
        TerminalUI.sectionTitle("ELIMINAR PEDIDO");

        int numeroPedido = leerEntero("Número de pedido: ");

        try {
            controlador.eliminarPedido(numeroPedido);

            TerminalUI.success("¡Pedido eliminado correctamente!");
            TerminalUI.spotlight("PEDIDO CANCELADO");

        } catch (RecursoNoEncontradoException | PedidoNoCancelableException e) {
            TerminalUI.exception(e.getMessage());

        } catch (DAOException e) {
            TerminalUI.exception(e.getMessage());

        }

        TerminalUI.sciFiDivider();
    }

    private void mostrarPedidosPendientes() {
        TerminalUI.sectionTitle("PEDIDOS PENDIENTES");

        String email = leerTextoOpcional("Introduce email del cliente (o deja vacío para ver todos): ");

        try {
            List<Pedido> pendientes = controlador.obtenerPedidosPendientes(email);

            if (pendientes.isEmpty()) {
                TerminalUI.warning("No hay pedidos pendientes actualmente.");
            } else {
                TerminalUI.showOrdersTable(pendientes);
                TerminalUI.info("Total de pedidos encontrados: " + pendientes.size());
            }

        } catch (EmailInvalidoException e) {
            TerminalUI.exception(e.getMessage());
        } catch (RecursoNoEncontradoException e) {
            TerminalUI.exception("El cliente no existe: " + e.getMessage());
        } catch (DAOException e) {
            TerminalUI.exception("Error de base de datos: " + e.getMessage());
        }

        TerminalUI.sciFiDivider();
    }

    private void mostrarPedidosEnviados() {
        TerminalUI.sectionTitle("PEDIDOS ENVIADOS");
        String emailFiltro = leerTextoOpcional("Filtrar por email del cliente (dejar vacío para todos): ");
        try {
            List<Pedido> pedidos = controlador.obtenerPedidosEnviados(emailFiltro);

            if (pedidos.isEmpty()) {
                TerminalUI.empty("No hay pedidos enviados que mostrar.");
            } else {
                TerminalUI.showOrdersTable(pedidos);
            }

        } catch (EmailInvalidoException | RecursoNoEncontradoException | DAOException e) {
            TerminalUI.exception(e.getMessage());
        }
    }

    private void cambiarEstadoPedido() {
        TerminalUI.sectionTitle("MARCAR PEDIDO COMO ENVIADO");

        // Solo pedimos el número de pedido
        int numeroPedido = leerEntero("Introduce el número de pedido para marcar como ENVIADO: ");

        try {
            // Llamamos al nuevo método del controlador que solo gestiona el envío
            controlador.marcarPedidoComoEnviado(numeroPedido);

            TerminalUI.success("El pedido nº " + numeroPedido + " ha sido marcado como ENVIADO.");
            TerminalUI.spotlight("OPERACIÓN COMPLETADA");

        } catch (RecursoNoEncontradoException | DAOException | CambioEstadoPedidoNoPermitidoException e) {
            // Aquí saltarán las excepciones si el ID no existe o si ya estaba enviado
            TerminalUI.exception(e.getMessage());
        }
    }

    private String leerTextoNoVacio(String mensaje) {
        while (true) {
            TerminalUI.prompt(mensaje);
            String linea = teclado.nextLine().trim();

            if (!linea.isEmpty()) {
                return linea;
            }

            TerminalUI.error("El texto no puede estar vacío. Inténtalo de nuevo.");
        }
    }

    private String leerTextoOpcional(String mensaje) {
        TerminalUI.prompt(mensaje);
        return teclado.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            TerminalUI.prompt(mensaje);
            String linea = teclado.nextLine().trim();

            if (linea.isEmpty()) {
                TerminalUI.error("No se permiten valores vacíos.");
                continue;
            }

            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                TerminalUI.error("Debes introducir un número válido.\n");
            }
        }
    }

    private BigDecimal leerBigDecimal(String mensaje) {
        while (true) {
            TerminalUI.prompt(mensaje);
            String linea = teclado.nextLine().trim();

            if (linea.isEmpty()) {
                TerminalUI.error("No se permiten valores vacíos.");
                continue;
            }

            try {
                return new BigDecimal(linea.replace(',', '.'));
            } catch (NumberFormatException e) {
                TerminalUI.error("Debes introducir un número válido.\n");
            }
        }
    }

    private void imprimirClientes(String mensajePersonalizado, List<Cliente> clientes) {
        if (clientes == null || clientes.isEmpty()) {
            TerminalUI.empty(mensajePersonalizado);
        } else {
            TerminalUI.showClientsTable(clientes);
        }
    }
}