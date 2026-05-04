package Util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Clase utilitaria para gestionar JPA en el Producto 4.
 *
 * Esta clase crea una única instancia de EntityManagerFactory
 * a partir de la unidad de persistencia definida en persistence.xml.
 *
 * Se utiliza un enfoque tipo Singleton porque EntityManagerFactory
 * es costosa de crear y conviene reutilizarla durante toda la ejecución.
 */
public final class JPAUtil {

    /**
     * Nombre de la unidad de persistencia definida en persistence.xml.
     */
    private static final String UNIDAD_PERSISTENCIA = "Producto4PU";

    /**
     * Instancia única de la fábrica de EntityManager.
     */
    private static EntityManagerFactory entityManagerFactory;

    /**
     * Constructor privado para evitar instanciación.
     */
    private JPAUtil() {
    }

    /**
     * Devuelve la única instancia de EntityManagerFactory.
     * Si no existe o está cerrada, la crea.
     *
     * @return EntityManagerFactory lista para utilizarse
     */
    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            try {
                entityManagerFactory = Persistence.createEntityManagerFactory(UNIDAD_PERSISTENCIA);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error al crear la EntityManagerFactory. " +
                                "Revisa persistence.xml, las dependencias Maven y la conexión con MySQL.",
                        e
                );
            }
        }
        return entityManagerFactory;
    }

    /**
     * Crea y devuelve un nuevo EntityManager.
     *
     * @return EntityManager nuevo
     */
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Cierra la EntityManagerFactory si está abierta.
     */
    public static synchronized void cerrarEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}