package Vista.services;

import Modelo.Articulo;
import Modelo.Cliente;
import Modelo.ClientePremium;
import Modelo.Pedido;
import Util.JPAUtil;
import jakarta.persistence.EntityManager;
import javafx.concurrent.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de lectura para alimentar el dashboard empresarial.
 *
 * Centraliza el cálculo de indicadores reales de negocio a partir de la base
 * de datos: artículos, stock, clientes, pedidos, facturación, ticket medio,
 * estados operativos y artículo más vendido.
 *
 * Las consultas se ejecutan en segundo plano mediante Task para no bloquear
 * el hilo gráfico de JavaFX.
 */
public final class DashboardStats {

    private static final int UMBRAL_STOCK_CRITICO = 5;

    private DashboardStats() {
        // Clase de utilidad: no debe instanciarse.
    }

    /**
     * DTO con todos los indicadores necesarios para el dashboard.
     */
    public static class Stats {

        public final int articulosTotal;
        public final int stockTotal;
        public final int articulosSinStock;
        public final int articulosStockCritico;

        public final int clientesTotal;
        public final int clientesPremium;
        public final double porcentajeClientesPremium;

        public final int pedidosTotal;
        public final int pedidosMes;
        public final int pedidosPendientes;
        public final int pedidosEnviados;
        public final double porcentajePedidosPendientes;

        public final BigDecimal facturacionTotal;
        public final BigDecimal facturacionMes;
        public final BigDecimal ticketMedio;

        public final String articuloMasVendido;
        public final int unidadesArticuloMasVendido;

        public Stats(
                int articulosTotal,
                int stockTotal,
                int articulosSinStock,
                int articulosStockCritico,
                int clientesTotal,
                int clientesPremium,
                double porcentajeClientesPremium,
                int pedidosTotal,
                int pedidosMes,
                int pedidosPendientes,
                int pedidosEnviados,
                double porcentajePedidosPendientes,
                BigDecimal facturacionTotal,
                BigDecimal facturacionMes,
                BigDecimal ticketMedio,
                String articuloMasVendido,
                int unidadesArticuloMasVendido
        ) {
            this.articulosTotal = articulosTotal;
            this.stockTotal = stockTotal;
            this.articulosSinStock = articulosSinStock;
            this.articulosStockCritico = articulosStockCritico;
            this.clientesTotal = clientesTotal;
            this.clientesPremium = clientesPremium;
            this.porcentajeClientesPremium = porcentajeClientesPremium;
            this.pedidosTotal = pedidosTotal;
            this.pedidosMes = pedidosMes;
            this.pedidosPendientes = pedidosPendientes;
            this.pedidosEnviados = pedidosEnviados;
            this.porcentajePedidosPendientes = porcentajePedidosPendientes;
            this.facturacionTotal = facturacionTotal == null ? BigDecimal.ZERO : facturacionTotal;
            this.facturacionMes = facturacionMes == null ? BigDecimal.ZERO : facturacionMes;
            this.ticketMedio = ticketMedio == null ? BigDecimal.ZERO : ticketMedio;
            this.articuloMasVendido = articuloMasVendido == null || articuloMasVendido.isBlank()
                    ? "Sin ventas registradas"
                    : articuloMasVendido;
            this.unidadesArticuloMasVendido = unidadesArticuloMasVendido;
        }

        public static Stats empty() {
            return new Stats(
                    0, 0, 0, 0,
                    0, 0, 0,
                    0, 0, 0, 0, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    "Sin ventas registradas", 0
            );
        }
    }

    /**
     * Carga los indicadores empresariales en segundo plano.
     *
     * @return tarea JavaFX con las estadísticas calculadas
     */
    public static Task<Stats> loadAsync() {
        return new Task<>() {
            @Override
            protected Stats call() {
                EntityManager entityManager = null;

                try {
                    entityManager = JPAUtil.getEntityManager();

                    List<Articulo> articulos = entityManager
                            .createQuery("SELECT a FROM Articulo a", Articulo.class)
                            .getResultList();

                    List<Cliente> clientes = entityManager
                            .createQuery("SELECT c FROM Cliente c", Cliente.class)
                            .getResultList();

                    List<Pedido> pedidos = entityManager
                            .createQuery("SELECT p FROM Pedido p", Pedido.class)
                            .getResultList();

                    return calcularStats(articulos, clientes, pedidos);

                } catch (Exception e) {
                    System.err.println("DashboardStats: no se pudieron cargar las estadísticas: " + e.getMessage());
                    return Stats.empty();

                } finally {
                    if (entityManager != null && entityManager.isOpen()) {
                        entityManager.close();
                    }
                }
            }
        };
    }

    private static Stats calcularStats(List<Articulo> articulos, List<Cliente> clientes, List<Pedido> pedidos) {
        LocalDateTime inicioMes = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        int articulosTotal = articulos == null ? 0 : articulos.size();

        int stockTotal = 0;
        int articulosSinStock = 0;
        int articulosStockCritico = 0;

        if (articulos != null) {
            for (Articulo articulo : articulos) {
                int stock = Math.max(0, articulo.getCantidadDisponible());
                stockTotal += stock;

                if (stock == 0) {
                    articulosSinStock++;
                }

                if (stock > 0 && stock <= UMBRAL_STOCK_CRITICO) {
                    articulosStockCritico++;
                }
            }
        }

        int clientesTotal = clientes == null ? 0 : clientes.size();
        int clientesPremium = 0;

        if (clientes != null) {
            for (Cliente cliente : clientes) {
                if (cliente instanceof ClientePremium) {
                    clientesPremium++;
                }
            }
        }

        double porcentajeClientesPremium = porcentaje(clientesPremium, clientesTotal);

        int pedidosTotal = pedidos == null ? 0 : pedidos.size();
        int pedidosMes = 0;
        int pedidosPendientes = 0;
        int pedidosEnviados = 0;

        BigDecimal facturacionTotal = BigDecimal.ZERO;
        BigDecimal facturacionMes = BigDecimal.ZERO;

        Map<String, Integer> unidadesPorArticulo = new HashMap<>();

        if (pedidos != null) {
            for (Pedido pedido : pedidos) {
                if (pedido == null) continue;

                boolean cancelado = esEstado(pedido, "CANCELADO");
                boolean pendiente = esEstado(pedido, "PENDIENTE");
                boolean enviado = esEstado(pedido, "ENVIADO");

                if (pendiente) {
                    pedidosPendientes++;
                }

                if (enviado) {
                    pedidosEnviados++;
                }

                boolean esDelMes = pedido.getFechaHora() != null
                        && !pedido.getFechaHora().isBefore(inicioMes);

                if (esDelMes) {
                    pedidosMes++;
                }

                if (!cancelado) {
                    BigDecimal totalPedido = calcularTotalSeguro(pedido);
                    facturacionTotal = facturacionTotal.add(totalPedido);

                    if (esDelMes) {
                        facturacionMes = facturacionMes.add(totalPedido);
                    }

                    String descripcionArticulo = obtenerDescripcionArticulo(pedido);
                    int cantidad = Math.max(0, pedido.getCantidad());

                    if (!descripcionArticulo.isBlank() && cantidad > 0) {
                        unidadesPorArticulo.merge(descripcionArticulo, cantidad, Integer::sum);
                    }
                }
            }
        }

        BigDecimal ticketMedio = pedidosTotal == 0
                ? BigDecimal.ZERO
                : facturacionTotal.divide(BigDecimal.valueOf(pedidosTotal), 2, RoundingMode.HALF_UP);

        Map.Entry<String, Integer> topArticulo = unidadesPorArticulo.entrySet()
                .stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);

        String articuloMasVendido = topArticulo == null ? "Sin ventas registradas" : topArticulo.getKey();
        int unidadesArticuloMasVendido = topArticulo == null ? 0 : topArticulo.getValue();

        return new Stats(
                articulosTotal,
                stockTotal,
                articulosSinStock,
                articulosStockCritico,
                clientesTotal,
                clientesPremium,
                porcentajeClientesPremium,
                pedidosTotal,
                pedidosMes,
                pedidosPendientes,
                pedidosEnviados,
                porcentaje(pedidosPendientes, pedidosTotal),
                facturacionTotal,
                facturacionMes,
                ticketMedio,
                articuloMasVendido,
                unidadesArticuloMasVendido
        );
    }

    private static boolean esEstado(Pedido pedido, String estado) {
        return pedido.getEstado() != null && pedido.getEstado().equalsIgnoreCase(estado);
    }

    private static BigDecimal calcularTotalSeguro(Pedido pedido) {
        try {
            BigDecimal total = pedido.calcularTotal();
            return total == null ? BigDecimal.ZERO : total;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static String obtenerDescripcionArticulo(Pedido pedido) {
        try {
            if (pedido.getArticulo() == null || pedido.getArticulo().getDescripcion() == null) {
                return "";
            }
            return pedido.getArticulo().getDescripcion();
        } catch (Exception e) {
            return "";
        }
    }

    private static double porcentaje(int parte, int total) {
        if (total <= 0) return 0;
        return (parte * 100.0) / total;
    }
}