package Util;

import jakarta.persistence.EntityManager;

/**
 * Clase de prueba mínima para comprobar que JPA e Hibernate
 * arrancan correctamente en el Producto 4.
 *
 * Esta prueba no modifica datos.
 * Solo intenta crear un EntityManager y ejecutar una consulta
 * muy simple contra la base de datos.
 */
public class PruebaConexionJPA {

    public static void main(String[] args) {
        EntityManager entityManager = null;

        try {
            System.out.println("Iniciando prueba de conexión JPA...");

            entityManager = JPAUtil.getEntityManager();

            Object resultado = entityManager
                    .createNativeQuery("SELECT 1")
                    .getSingleResult();

            System.out.println("Conexión JPA establecida correctamente.");
            System.out.println("Resultado de la consulta de prueba: " + resultado);

        } catch (Exception e) {
            System.err.println("Error al arrancar JPA o conectar con la base de datos.");
            e.printStackTrace();
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }

            JPAUtil.cerrarEntityManagerFactory();
            System.out.println("Prueba finalizada.");
        }
    }
}