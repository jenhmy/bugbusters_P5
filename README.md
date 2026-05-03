# BugBusters · Producto 4

## Implementación mediante ORM con JPA + Hibernate

**Asignatura:** FP.447 - (P) Programación orientada a objetos con acceso a bases de datos  
**Institución:** UOC  
**Proyecto:** BugBusters Producto 4  
**Grupo:** BugBusters  

---

## Integrantes

- Erick Coll Rodríguez
- Carles Miguel Millán
- Ferran Arnaus García
- Jennifer Hernández Marín
- Arnau Bordas Fornieles

---

## 1. Descripción general

**BugBusters Producto 4** es la evolución del proyecto desarrollado en entregas anteriores, adaptado en esta fase a una arquitectura basada en **ORM con JPA + Hibernate**, manteniendo la estructura general del sistema y sustituyendo la capa de persistencia anterior por una solución moderna, desacoplada y orientada a entidades.

Se trata de una **aplicación Java de consola** organizada según el patrón **MVC**, con una interfaz terminal estructurada, una capa de control centralizada y una capa de persistencia basada en entidades JPA, DAOs y una factoría de acceso a datos.

El objetivo principal de esta entrega ha sido **migrar la persistencia del sistema a ORM**, garantizando que la aplicación continúe funcionando correctamente, que conserve la lógica de negocio y que aproveche las ventajas de Hibernate en el mapeo objeto-relacional.

---

## 2. Objetivo del Producto 4

Los objetivos principales de esta fase del proyecto son:

- Implantar persistencia ORM mediante **JPA + Hibernate**.
- Mapear correctamente las entidades del dominio a la base de datos.
- Mantener la arquitectura **MVC**.
- Preservar la lógica funcional del sistema.
- Organizar la aplicación en una estructura clara, modular y extensible.
- Centralizar el acceso a datos mediante **DAO + Factory**.
- Validar el funcionamiento completo del sistema tras la migración.

---

## 3. Estado actual del proyecto

En su estado actual, el proyecto se encuentra **operativo y funcional**.

### 3.1. El proyecto ya incluye

- Aplicación Java de consola funcional.
- Arquitectura MVC consolidada.
- Persistencia ORM con JPA + Hibernate.
- Conexión a base de datos MySQL remota.
- Entidades correctamente mapeadas.
- Herencia ORM en la jerarquía de clientes.
- Relaciones entre entidades.
- Capa DAO estructurada.
- Factoría DAO para desacoplar la persistencia.
- Interfaz terminal con menús y tablas visuales.
- Lógica de negocio integrada en el controlador.
- Compilación mediante Maven.
- Arranque correcto desde `Vista.Main`.

### 3.2. Validado en el estado actual

- Compilación correcta con Maven.
- Arranque correcto del programa.
- Navegación por menús.
- Lectura y gestión de datos.
- Integración funcional tras los últimos ajustes de mapeo e integración.

---

## 4. Arquitectura del proyecto

La aplicación sigue una arquitectura basada en **MVC** y se apoya en una separación clara de responsabilidades.

### 4.1. Modelo

Representa el dominio del sistema y contiene:

- Entidades del negocio.
- Reglas propias del modelo.
- Jerarquías de herencia.
- Excepciones del dominio.

### 4.2. Vista

Se encarga de la interacción con el usuario mediante terminal:

- Menús.
- Tablas.
- Colores ANSI.
- Mensajes de validación.
- Tarjetas y bloques visuales.

### 4.3. Controlador

Actúa como punto central de coordinación:

- Recibe acciones de la vista.
- Valida entradas.
- Consulta o modifica datos.
- Coordina transacciones.
- Aplica reglas de negocio.

### 4.4. Persistencia

La persistencia se organiza mediante:

- Entidades JPA.
- DAOs.
- Factoría de DAOs.
- Utilidades de inicialización de JPA.

---

## 5. Tecnologías utilizadas

### 5.1. Lenguaje principal

- Java

### 5.2. Persistencia y ORM

- JPA
- Hibernate

### 5.3. Base de datos

- MySQL

### 5.4. Gestión de dependencias

- Maven

### 5.5. Entorno de desarrollo

- IntelliJ IDEA

### 5.6. Patrones y arquitectura

- MVC
- DAO
- Factory
- ORM

---

## 6. Estructura del proyecto

```text
BugBusters Producto 4/
│
├── src/
│   ├── Controlador/
│   │   └── Controlador.java
│   │
│   ├── DAO/
│   │   ├── Interfaces/
│   │   └── MySQL/
│   │       ├── ArticuloDAOJPA.java
│   │       ├── ClienteDAOJPA.java
│   │       └── PedidoDAOJPA.java
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
│       ├── Main.java
│       ├── Vista.java
│       └── TerminalUI.java
│
├── lib/
├── target/
├── pom.xml
├── .gitignore
└── README.md
```

### 6.1. Observación importante sobre la estructura

Este proyecto no utiliza la estructura Maven estándar:

```text
src/main/java
```

En su lugar, el código fuente se encuentra directamente en:

```text
src/
```

Por ese motivo, el archivo `pom.xml` define explícitamente:

```xml
<sourceDirectory>src</sourceDirectory>
```

Esto es importante para comprender la compilación del proyecto y su apertura correcta en IntelliJ IDEA.

---

## 7. Entidades principales del dominio

### 7.1. Articulo

Representa los artículos disponibles para venta o gestión.

Incluye, entre otros atributos:

- Código.
- Descripción.
- Precio de venta.
- Gastos de envío.
- Tiempo de preparación.
- Cantidad disponible.

### 7.2. Cliente

Clase abstracta base del sistema de clientes.

Contiene:

- Email.
- Nombre.
- Domicilio.
- NIF.
- ID de cliente.

Además, define el comportamiento común y los métodos abstractos:

```java
calcularCuota()
descuentoEnvio()
```

### 7.3. ClienteEstandar

Subtipo de cliente sin cuota fija ni descuento en gastos de envío.

### 7.4. ClientePremium

Subtipo de cliente con:

- Cuota fija.
- Descuento en gastos de envío.

### 7.5. Pedido

Entidad que representa los pedidos del sistema.

Relaciona:

- Un cliente.
- Un artículo.

Y contiene:

- Cantidad.
- Fecha y hora.
- Estado.
- Lógica asociada a cancelación.
- Envío automático.
- Cálculo total.

---

## 8. Persistencia ORM

La persistencia se ha implementado con **JPA + Hibernate**.

### 8.1. Unidad de persistencia

La unidad definida en `persistence.xml` es:

```xml
<persistence-unit name="Producto4PU" transaction-type="RESOURCE_LOCAL">
```

### 8.2. Provider utilizado

El proveedor ORM utilizado es Hibernate:

```xml
<provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
```

### 8.3. Entidades registradas

Se registran explícitamente las entidades principales:

- `Modelo.Articulo`
- `Modelo.Cliente`
- `Modelo.ClienteEstandar`
- `Modelo.ClientePremium`
- `Modelo.Pedido`

Esto es importante porque en la configuración se utiliza:

```xml
<exclude-unlisted-classes>true</exclude-unlisted-classes>
```

Por tanto, las clases deben declararse manualmente en el archivo de persistencia.

---

## 9. Herencia y relaciones ORM

Uno de los aspectos más importantes del proyecto es la implementación del modelo ORM.

### 9.1. Herencia en clientes

La entidad `Cliente` está configurada con herencia de tabla única:

```java
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_cliente")
```

Las clases hijas se distinguen mediante valores discriminadores:

```java
@DiscriminatorValue("Estandar")
@DiscriminatorValue("Premium")
```

### 9.2. Relaciones en Pedido

La entidad `Pedido` mantiene relaciones `@ManyToOne` con:

- `Cliente`
- `Articulo`

Esto permite modelar correctamente los pedidos desde el punto de vista de la base de datos y del dominio Java.

---

## 10. DAO y factoría de acceso a datos

La aplicación no accede directamente a Hibernate desde la vista ni desde el modelo.

La persistencia está organizada mediante una capa intermedia basada en **DAO + Factory**.

### 10.1. DAOs principales

- `ArticuloDAOJPA`
- `ClienteDAOJPA`
- `PedidoDAOJPA`

### 10.2. Factoría

- `DAOFactory`
- `MySQLDAOFactory`

### 10.3. Ventajas de este enfoque

- Desacopla la lógica de negocio del acceso a datos.
- Centraliza la creación de DAOs.
- Facilita el mantenimiento.
- Permite cambiar la implementación de persistencia sin romper la arquitectura general.

---

## 11. Lógica de negocio destacada

El proyecto no se limita a persistir entidades, sino que conserva lógica de negocio real.

Entre las operaciones implementadas y gestionadas desde el controlador se encuentran:

- Añadir pedidos.
- Validar emails.
- Buscar clientes y artículos.
- Comprobar disponibilidad de stock.
- Iniciar y confirmar transacciones.
- Cancelar transacciones ante error.
- Eliminar pedidos.
- Cambiar estados.
- Actualizar automáticamente pedidos enviados.
- Listar pedidos según su estado.

Esto demuestra que la migración a ORM no ha roto el comportamiento funcional del sistema.

---

## 12. Gestión de transacciones

La aplicación utiliza transacciones controladas desde la capa de factoría/controlador.

En la práctica, el flujo funcional se organiza con métodos como:

```java
iniciarTransaccion()
confirmarTransaccion()
cancelarTransaccion()
```

Este enfoque evita mezclar lógica de negocio con detalles internos de persistencia y aporta mayor robustez al sistema.

---

## 13. Interfaz de usuario por terminal

La aplicación se ejecuta como programa de consola, pero la interfaz no es mínima ni improvisada.

### 13.1. Elementos visuales incluidos

- Menús estructurados.
- Tablas ASCII.
- Bloques de información.
- Colores ANSI.
- Mensajes ordenados.
- Presentación clara para el usuario.

### 13.2. Clases implicadas

- `Vista.java`
- `TerminalUI.java`
- `Main.java`

Esto permite que el proyecto resulte más claro, más profesional y más fácil de demostrar en vídeo.

---

## 14. Configuración del proyecto

### 14.1. Archivo `pom.xml`

El proyecto utiliza Maven y define las dependencias principales de la capa ORM.

### 14.2. Dependencias relevantes

- Hibernate Core.
- Jakarta Persistence API.
- MySQL Connector/J.

### 14.3. Versiones utilizadas

- Hibernate `7.3.1.Final`.
- Jakarta Persistence `3.2.0`.
- MySQL Connector `8.0.33`.
- Java `17`.

---

## 15. Configuración de persistencia

El archivo clave es:

```text
src/META-INF/persistence.xml
```

### 15.1. Aspectos importantes

- Conexión MySQL configurada.
- Uso de Hibernate como provider.
- Entidades registradas manualmente.
- `hbm2ddl.auto` configurado para mantener estabilidad del entorno.
- Dialecto definido para MySQL.

### 15.2. Criterio adoptado

Durante la integración final se mantuvo una configuración estable, evitando cambios innecesarios que pudieran romper el sistema una vez validado.

---

## 16. Ejecución del proyecto

### 16.1. Requisitos previos

- Java 17.
- Maven correctamente integrado en IntelliJ IDEA.
- Acceso a la base de datos configurada en `persistence.xml`.

### 16.2. Compilación recomendada

Desde la ventana Maven de IntelliJ IDEA:

```text
clean
compile
```

### 16.3. Ejecución

La aplicación arranca desde:

```text
src/Vista/Main.java
```

Clase principal:

```text
Vista.Main
```

---

## 17. Recomendaciones para abrir el proyecto en IntelliJ IDEA

Para evitar errores de configuración, se recomienda abrir el proyecto correctamente desde Maven.

### 17.1. Apertura correcta

Abrir el proyecto desde:

```text
pom.xml
```

Y no únicamente desde la carpeta raíz.

### 17.2. Aspectos importantes

Al tratarse de una estructura no estándar de Maven, IntelliJ IDEA debe reconocer correctamente:

- `src` como fuente.
- `target/classes` como salida del módulo.
- El módulo Maven asociado al proyecto.

### 17.3. Si IntelliJ IDEA da problemas

Se recomienda:

- Cerrar el proyecto.
- Eliminar `.idea` y archivos `.iml`.
- Reabrir directamente desde `pom.xml`.
- Dejar que IntelliJ IDEA regenere la configuración.

---

## 18. Incidencias relevantes resueltas

Durante la fase final del proyecto se resolvieron varias incidencias importantes.

### 18.1. Integración de la parte POO

Tras fusionar la PR correspondiente a la parte de POO, aparecieron errores de compilación e incompatibilidades de integración.

### 18.2. Problemas detectados

- Entidades JPA incompletamente integradas.
- Incompatibilidad entre modelos y DAOs.
- Conflicto entre la nueva capa ORM y la lógica previa.
- Errores de compilación que impedían generar `Vista.Main`.

### 18.3. Solución aplicada

- Revisión completa de `persistence.xml`.
- Declaración explícita de entidades.
- Corrección de imports y anotaciones JPA.
- Recuperación de compatibilidad en `Cliente` y `Pedido`.
- Validación completa con Maven.
- Pruebas funcionales del programa.

### 18.4. Resultado

El proyecto volvió a:

- Compilar correctamente.
- Generar las clases necesarias.
- Arrancar sin error.
- Mantener operativas sus funcionalidades.

---

## 19. Estado final del proyecto

A fecha actual, el proyecto puede considerarse terminado a nivel funcional y técnico, dentro del alcance del Producto 4.

El sistema final:

- Compila con Maven.
- Arranca correctamente.
- Utiliza ORM con JPA + Hibernate.
- Mantiene la arquitectura MVC.
- Conserva la lógica funcional principal.
- Incorpora herencia ORM.
- Incorpora relaciones entre entidades.
- Está preparado para demostración y defensa académica.

---

## 20. Historial y trazabilidad

El historial Git del proyecto refleja la evolución real del trabajo:

- Infraestructura inicial.
- Preparación del entorno.
- Adaptación a ORM.
- Mapeo de entidades.
- Integración de la parte POO.
- Corrección posterior de integración.
- Consolidación final del proyecto.

El último estado estable incluye la corrección que restableció la compilación y ejecución tras el merge de la parte POO.

---

## 21. Puntos fuertes del proyecto

Este proyecto destaca especialmente por:

- Implantación real de ORM, no meramente decorativa.
- Uso claro de herencia en JPA.
- Relaciones bien definidas entre entidades.
- Estructura MVC coherente.
- Separación por DAO + Factory.
- Lógica de negocio integrada y no superficial.
- Interfaz terminal cuidada visualmente.
- Trazabilidad real de integración y correcciones.

---

## 22. Consideraciones académicas

Este Producto 4 no se limita a mostrar código con anotaciones JPA, sino que refleja una migración funcional y estructural de la persistencia del sistema.

Desde el punto de vista académico, el valor principal del proyecto está en que:

- No rompe la arquitectura anterior.
- Adapta el modelo a ORM.
- Mantiene la coherencia entre capas.
- Resuelve incidencias reales de integración.
- Deja una aplicación completa, ejecutable y defendible.

---

## 23. Arranque rápido

### 23.1. Compilar

Desde Maven:

```text
clean
compile
```

### 23.2. Ejecutar

Desde IntelliJ IDEA:

```text
Vista.Main
```

### 23.3. Archivo clave de configuración

```text
src/META-INF/persistence.xml
```

### 23.4. Archivo clave de dependencias

```text
pom.xml
```

---

## 24. Conclusión

**BugBusters Producto 4** representa una evolución completa del sistema hacia una persistencia basada en ORM, manteniendo una estructura arquitectónica ordenada y un comportamiento funcional estable.

No se trata únicamente de haber añadido anotaciones JPA, sino de haber integrado correctamente el modelo, la lógica de negocio, la capa DAO, la factoría de persistencia y la ejecución general del programa dentro de una solución coherente, compilable y operativa.

El resultado final es una aplicación académicamente defendible, técnicamente sólida y preparada para ser mostrada en vídeo con claridad y profundidad.

---

## 25. Autores

Proyecto desarrollado por el equipo **BugBusters** para la asignatura:

**FP.447 - (P) Programación orientada a objetos con acceso a bases de datos**  
**UOC - Universitat Oberta de Catalunya**
