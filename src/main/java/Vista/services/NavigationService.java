package Vista.services;

/**
 * Servicio simple de navegación interna entre vistas.
 *
 * Permite que controladores secundarios soliciten volver al dashboard
 * sin depender directamente del MenuPrincipalController.
 */
public final class NavigationService {

    private static Runnable dashboardAction;

    private NavigationService() {
    }

    public static void registrarDashboardAction(Runnable action) {
        dashboardAction = action;
    }

    public static void irADashboard() {
        if (dashboardAction != null) {
            dashboardAction.run();
        }
    }
}