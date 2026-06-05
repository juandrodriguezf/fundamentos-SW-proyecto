# Plan de Implementación: Gestionar Carrito de Compras (Versión 2 Proyecto)

El objetivo es permitir a un usuario (visitante no registrado) agregar y remover libros en un carrito de compras temporal, manteniendo persistencia a través de la base de datos local H2.

> [!IMPORTANT]
> ## Modificaciones Adicionales (Propuestas sobre el plan original)
> 1. **Modelo Libro:** El modelo de `Libro` actual (`id`, `titulo`, `autor`, `clics`) **no tiene precio ni portada**. Se modificará `Libro.java` para incluir `precio` (`double` o `BigDecimal`) y `portada` (`String` url), y se actualizará la tabla H2 en `JdbcLibroGateway` para inyectar precios aleatorios o fijos a los datos de prueba. 
> 2. **Navegación UI:** Dado que actualmente la aplicación tiene una sola vista fija (BorderPane), se agregará un botón en la barra superior (Top) que diga "🛒 Ver Carrito (0)". Al presionarlo, el centro de la pantalla cambiará para mostrar el listado del carrito y el botón volverá a "🏠 Volver al inicio".

---

## 🛠️ Plan de Implementación: Persistencia del Carrito en H2 Local

Este documento define la lista de tareas para implementar la gestión y persistencia del carrito de compras utilizando la base de datos H2 (en modo local embebido), implementando el `CarritoGateway` bajo Arquitectura Limpia (Capa de Infraestructura / Adaptadores de Salida).

### Diseño de la Solución
* **Estrategia de Sesión:** Como es una app de escritorio (JavaFX) y el usuario es un visitante (no logueado), usaremos un identificador único de sesión temporal (UUID) generado al abrir la aplicación para asociar sus ítems en la base de datos.
* **Archivo de datos:** Reutiliza el mismo archivo local `mylib.mv.db` en la raíz del proyecto.
* **Adaptador:** Se creará la clase `JdbcCarritoGateway` en el paquete `infrastructure.adapter.out.persistence` que implementará la interfaz `CarritoGateway`.

---

### Lista de Tareas (Implementación Persistencia)

#### [ ] 1. Diseñar el Modelo de Datos (Esquema SQL):
Crear un script de inicialización en el Gateway para asegurar que existan las tablas necesarias:
* Tabla `carrito_items`: `id` (PK), `session_id` (VARCHAR), `libro_id` (FK), `cantidad` (INT).
* Añadir restricciones para que un mismo `session_id` y `libro_id` no se dupliquen (índice único para facilitar el MERGE o UPSERT).

#### [ ] 2. Definir el Contrato en la Capa de Dominio (CarritoGateway):
Crear la interfaz en la capa de dominio/puertos de salida con las siguientes operaciones:
* `agregarItem(String sessionId, Long libroId, int cantidad): void`
* `eliminarItem(String sessionId, Long libroId): void`
* `obtenerContenidoCarrito(String sessionId): List<CarritoItem>`
* `limpiarCarrito(String sessionId): void`

#### [ ] 3. Crear el Adaptador de Persistencia (JdbcCarritoGateway):
* Crear la clase en `co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence`.
* Implementar constructor que asegure la conexión JDBC usando el pool de conexiones o el DriverManager hacia H2.

#### [ ] 4. Implementar las Operaciones del Gateway (Lógica SQL en H2):
* `agregarItem`: Implementar una sentencia `MERGE INTO carrito_items (session_id, libro_id, cantidad) KEY(session_id, libro_id) VALUES (?, ?, ?)` para que si el libro ya existe en el carrito, solo sume/actualice la cantidad.
* `eliminarItem`: Implementar `DELETE FROM carrito_items WHERE session_id = ? AND libro_id = ?`.
* `obtenerContenidoCarrito`: Implementar un `SELECT` haciendo un `JOIN` entre `carrito_items` y la tabla `libros` para traer el título, precio y portada de cada libro agregado.

#### [ ] 5. Escribir Pruebas de Integración (JdbcCarritoGatewayTest):
* Crear las pruebas unitarias usando la base de datos en memoria (`jdbc:h2:mem:test`) utilizando `@BeforeEach` para poblar libros de prueba y validar que sume, reste y calcule correctamente los elementos.

#### [ ] 6. Conectar con el Controlador de JavaFX (Capa de Presentación):
* Modificar el orquestador principal (`Main.java`) para inyectar el `JdbcCarritoGateway` en los casos de uso (`AgregarAlCarritoUseCase`, `VerCarritoUseCase`).
* **Uso de Observables:** En el controlador de JavaFX de la pantalla del carrito, mapear los resultados a una `ObservableList<CarritoItem>` de JavaFX para asegurar que cualquier cambio en las cantidades actualice el precio total en pantalla instantáneamente.
