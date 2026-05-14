# BugBusters · Producto 5

## Implementación de la aplicación de escritorio con interfaz gráfica

**Asignatura:** FP.447 - (P) Programación orientada a objetos con acceso a bases de datos  
**Institución:** UOC  
**Grupo:** BugBusters 

---

## Integrantes

- Erick Coll Rodríguez
- Ferran Arnaus García
- Jennifer Hernández Marín
- Arnau Bordas Fornieles

---

## 1. Descripción general

**BugBusters Producto 5** representa la transformación definitiva del proyecto hacia una **aplicación de escritorio profesional**. En esta fase, se ha sustituido la **interfaz de consola por una interfaz gráfica (GUI) desarrollada en JavaFX**, implementando un modelo de interacción orientado a eventos y un diseño visual tecnológico.

La aplicación mantiene su robusta base de persistencia **ORM (JPA + Hibernate)**, pero evoluciona su arquitectura **MVC** para desacoplar la vista (FXML) del comportamiento (Controladores JavaFX), incorporando herramientas avanzadas de gestión empresarial como un Dashboard con KPIs en tiempo real.

---

## 2. Objetivo del Producto 4

Los objetivos principales de esta fase final son:

- Migrar la interacción del usuario de consola a **entorno gráfico con JavaFX**.
- Implementar el diseño de interfaces mediante archivos **FXML y CSS**.
- Evolucionar el patrón **MVC** hacia una arquitectura orientada a eventos.
- Desarrollar un **Dashboard empresarial** con KPIs reales (stock, clientes, facturación).
- Mejorar la experiencia de usuario (UX) mediante **animaciones y sonidos de interacción**.
- Mantener la integridad de la persistencia ORM bajo un entorno multi-ventana.
- Reestructurar el proyecto siguiendo el estándar **Maven** (`src/main/java` y `src/main/resources`).

---

## 3. Estado actual del proyecto

En su estado actual, el proyecto se encuentra **100% operativo y funcional**.

### 3.1. Funcionalidades destacadas

- **Dashboard Empresarial:** Panel principal con indicadores reales de inventario, clientes premium y facturación mensual.
- **Gestión de Artículos:** Tabla dinámica con filtros de stock y alertas visuales por colores (Crítico/Sin Stock).
- **Gestión de Clientes:** Panel CRM para clasificar y buscar clientes Estandar/Premium de forma ágil.
- **Gestión de Pedidos:** Control de estados (Pendiente, Enviado, Cancelado) y cálculos financieros automáticos.
- **Navegación Fluida:** Sistema de navegación interna con botones de retorno e interfaces modales.
- **Capa Sensorial:** Efectos visuales de profundidad y sistema de sonido sintetizado para feedback de acciones.

### 3.2. Validado en el estado actual

- Compilación correcta con Maven.
- Arranque correcto del programa.
- Navegación por menús.
- Lectura y gestión de datos.

---

## 4. Arquitectura del proyecto

La implementación de JavaFX ha permitido una separación de responsabilidades profesional:

### 4.1. Vista (View)
- **FXML:** Define la jerarquía de los componentes (botones, tablas, paneles).
- **CSS:** Gestiona de forma externa el diseño visual, colores y efectos.

### 4.2. Controlador (Controller)
- **Controladores FXML:** Gestionan la interacción inmediata y los eventos (`ActionEvent`, `MouseEvent`, `KeyEvent`).
- **Controlador Lógico:** Actúa como puente entre la interfaz y la capa de datos (DAO).

### 4.3. Modelo y Persistencia
- **Entidades JPA:** El núcleo de datos del sistema.
- **DAO + Factory:** Garantiza que la persistencia siga siendo independiente de la interfaz gráfica.

---

## 5. Tecnologías utilizadas

- **Lenguaje:** Java 17
- **Interfaz Gráfica:** JavaFX (FXML, CSS)
- **Persistencia:** JPA / Hibernate (ORM)
- **Base de Datos:** MySQL
- **Gestión de Dependencias:** Maven
- **Entorno de Desarrollo:** IntelliJ IDEA

---

## 6. Estructura del proyecto

```text
BugBusters Producto 5/
└── main/
    ├── java/
    │   ├── Controlador/
    │   │   └── Controlador.java
    │   │
    │   ├── DAO/
    │   │   ├── Interfaces/
    │   │   └── MySQL/
    │   │
    │   ├── Database/
    │   │   └── scripts SQL de apoyo
    │   │
    │   ├── Excepciones/
    │   │   └── excepciones de aplicación
    │   │
    │   ├── Factory/
    │   │   ├── DAOFactory.java
    │   │   └── MySQLDAOFactory.java
    │   │
    │   ├── META-INF/
    │   │   └── persistence.xml
    │   │
    │   ├── Modelo/
    │   │   ├── Articulo.java
    │   │   ├── Cliente.java
    │   │   ├── ClienteEstandar.java
    │   │   ├── ClientePremium.java
    │   │   ├── Pedido.java
    │   │   └── Excepciones/
    │   │
    │   ├── Util/
    │   │   ├── JPAUtil.java
    │   │   └── PruebaConexionJPA.java
    │   │
    │   └── Vista/
    │       ├── controllers/
    │       │   ├── formularios/
    │       │   │   ├── FormularioArticuloController.java
    │       │   │   ├── FormularioClienteController.java
    │       │   │   └── FormularioPedidoController.java
    │       │   │
    │       │   ├── ConfirmacionController.java
    │       │   ├── GenericoController.java
    │       │   ├── GestionArticulosController.java
    │       │   ├── GestionClientesController.java
    │       │   ├── GestionPedidosController.java
    │       │   └── MenuPrincipalController.java
    │       │
    │       ├── fx/
    │       │   ├── AmbientOrbs.java
    │       │   ├── Animations.java
    │       │   ├── DashboardWelcome.java
    │       │   ├── DataStream.java
    │       │   ├── Hologram3D.java
    │       │   └── SoundFX.java
    │       │
    │       ├── services/
    │       │   ├── DashboardStats.java
    │       │   └── NavigationService.java
    │       │
    │       ├── Launcher.java
    │       └── Main.java
    │
    └── resources/
        ├── META-INF/
        │   └── persistence.xml
        │
        └── Vista/
            ├── css/
            │   └── estilos.css
            │
            └── fxml/
                ├── formularios/
                │   ├── FormularioArticulo.fxml
                │   ├── FormularioCliente.fxml
                │   └── FormularioPedido.fxml
                │
                ├── ConfirmacionDialog.fxml
                ├── GestionArticulos.fxml
                ├── GestionClientes.fxml
                ├── GestionPedidos.fxml
                └── MenuPrincipal.fxml
```

---

## 7. Implementación de Eventos

La aplicación utiliza un modelo de gestión de eventos jerárquico (*Event Dispatch Chain*):

- **ActionEvent**: utilizado en botones para ejecutar acciones de guardado, cancelación o navegación.

- **WindowEvent**: captura el ciclo de vida de la ventana, permitiendo por ejemplo centrar las modales al mostrarse mediante `setOnShown`.

- **MouseEvent**: gestiona clics en elementos gráficos personalizados y efectos de paso del ratón (*hover*).

- **KeyEvent**: permite el filtrado de búsquedas dinámicas en tiempo real mientras el usuario escribe en los buscadores (`KeyReleased`).

---

## 8. Persistencia y ORM

Se mantiene la configuración de **JPA + Hibernate** del Producto 4, asegurando que las transacciones y las relaciones de herencia (`ClienteEstandar` / `ClientePremium`) funcionen correctamente en el nuevo entorno gráfico.

Las estadísticas del Dashboard se obtienen mediante la clase `DashboardStats`, que realiza consultas agregadas a través del ORM de forma eficiente.

---

## 9. Instrucciones de ejecución

### 9.1. Requisitos previos

- JDK 17 o superior.
- Maven integrado en el entorno.
- Base de datos MySQL configurada según los parámetros definidos en `persistence.xml`.

### 9.2. Arranque

1. Ejecutar el siguiente comando para preparar dependencias y recursos:

```bash
mvn clean install
```

2. Iniciar la aplicación desde la clase:

```text
Vista.Launcher
```

> Se recomienda utilizar `Vista.Launcher` para evitar posibles problemas relacionados con los módulos de JavaFX.

---

## 10. Conclusión

**BugBusters Producto 5** supone la culminación del proyecto, transformándolo en una herramienta empresarial moderna.

La implementación de JavaFX sobre una base sólida de ORM demuestra que es posible crear aplicaciones de escritorio potentes, mantenibles y visualmente atractivas, respetando estrictamente el patrón arquitectónico MVC.


---

## 25. Autores

Proyecto desarrollado por el equipo **BugBusters** para la asignatura:

**FP.447 - (P) Programación orientada a objetos con acceso a bases de datos**  
**UOC - Universitat Oberta de Catalunya**
