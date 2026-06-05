# OpenLib Market

Aplicación de biblioteca digital desarrollada con **JavaFX 21**, **H2 Database** y una **API REST de reseñas en Spring Boot**.

---

## Cómo ejecutar

### Requisitos
- **JDK 17 o superior** instalado y configurado en el `PATH`

### 1. App principal (JavaFX)
```bash
cd ProyectoFDSW
./mvnw clean javafx:run
```
> **Nota:** No funciona darle "play" directamente a `Launcher.java` desde el IDE porque JavaFX no está incluido en el JDK desde Java 11. El `mvnw` (Maven Wrapper) descarga las dependencias automáticamente.

Si usas IntelliJ, puedes crear un Run Configuration de Maven congoal `javafx:run`, o configurar los VM arguments con `--module-path` apuntando al SDK de JavaFX.

### 2. API de reseñas (Spring Boot) — opcional
Para que las reseñas funcionen, inicia este microservicio en otra terminal:
```bash
cd reviews-api
./mvnw spring-boot:run
```
Corre en `http://localhost:8081`. La app principal funciona sin esto, pero las reseñas no cargarán.

---

## Plan de Entrega 1 (MVP)

Este repositorio contiene la **primera versión inicial** del proyecto **OpenLib Market**, estructurado bajo el patrón de **Arquitectura Limpia (Clean Architecture)**.

> **Estado Actual del Proyecto:** Actualmente, este repositorio **solo contiene la implementación de la primera historia de usuario ("Buscar Libro" y funcionalidades relacionadas)**. A medida que el desarrollo avance, se irán realizando commits integrando los demás módulos descritos a continuación.

## Alcance Funcional Planeado (MVP)

A continuación se describe el plan del alcance funcional total que tendrá el MVP una vez se completen las entregas:

### MÓDULO DE BUYER (Storefront)
* **Registro y login (PROYECTOSW-26):** Autenticación y control de roles de usuario.
* **Catalogo Interactivo (PROYECTOSW-24):** Panel principal de la tienda.
* **Filtros Avanzados (PROYECTOSW-34):** Búsqueda específica por etiquetas, ISBN y categorías.
* **Recomendaciones (PROYECTOSW-37):** Motor de libros sugeridos al usuario en base al histórico.
* **Ver Detalles y Reviews (PROYECTOSW-36):** Vista ampliada del libro incorporando sistema de reseñas.
* **Lista de Favoritos (PROYECTOSW-38):** Funcionalidad de Wishlist.
* **Carrito de Compras (PROYECTOSW-25):** Gestión de artículos a comprar o descargar.
* **Checkout Simulado (PROYECTOSW-27):** Proceso de facturación y transacción simbólica.
* **Biblioteca y Descargas Seguras (PROYECTOSW-41):** Acceso a descargas protegidas por la sesión del cliente (URLs seguras).
* **Historial de Compras (PROYECTOSW-33):** Registro y control de gastos del usuario.

### MÓDULO SELLER (Creación)
* **Registro y login (PROYECTOSW-26):** Acceso exclusivo como publicador o creador.
* **Gestion de publicacion (PROYECTOSW-28):** Permitir subir materiales, definir portadas, adjuntar el PDF/ePub, y poner metadatos.
* **Editar stock/precios (PROYECTOSW-32):** Actualización de los datos del libro ya publicado.
* **Ver Ventas (PROYECTOSW-31 / PROYECTOSW-40):** Revisar cantidad de descargas/adquisiciones e informes estadísticos.

### MÓDULO ADMIN (Storeback)
* **Dashboard de Metricas (PROYECTOSW-1):** Gráficos o listados rápidos de libros más descargados, usuarios activos, categorías populares.
* **Moderacion y Calidad (PROYECTOSW-30):** Curaduría de reseñas, revisión de catálogos y libros subidos.
* **Gestion de usuarios y plataforma (PROYECTOSW-29):** Control absoluto del catálogo, etiquetas, categorías y banear/gestionar clientes.

---

## Atributos de Calidad y Arquitectura

* **Seguridad:** Autenticación manejada mediante JWT y contraseñas encriptadas.
* **Performance:** Consultas optimizadas con paginación en PostgreSQL para respuesta rápida.
* **Arquitectura:** Controles en capa (MVC) limpios para Spring Boot.

### Stack Tecnológico Práctico
* **Backend:** Java 21 o superior, y Spring Boot 3 o superior (REST, JPA/Hibernate, Spring Security).
* **Base de Datos:** PostgreSQL para transaccionalidad robusta. Guardado local de bytes/pdfs. *(Nota: En esta primera iteración se emplea H2 Embebido para desarrollo y pruebas)*.
* **Frontend:** Aplicación de escritorio moderna en JavaFX separando lógicas de red con llamadas HTTP asíncronas.

---

## Explicación de la Arquitectura - Historia (Buscar Libro)

El proyecto implementa el patrón de **Arquitectura Limpia (Clean Architecture)**, dividiendo el código en capas concéntricas para que la lógica de negocio no dependa de la base de datos ni de la interfaz gráfica. A continuación se detalla la estructura implementada en esta primera entrega:

### 1. Capa de Dominio (`domain/Libro.java`)
Es el corazón de la aplicación. Aquí definimos la entidad `Libro` (id, titulo, autor, clics). Esta clase es Java "puro": no depende de ninguna base de datos ni de interfaces gráficas. Es agnóstica a la tecnología externa.

### 2. Capa de Aplicación (`application/...`)
Define los casos de uso y reglas de negocio.
* **`LibroGateway.java`:** Interfaz (Puerto de salida) que define los contratos de persistencia (buscar libros, ver tendencias, aumentar clics).
* **Casos de Uso (`BuscarLibroUseCase`, `ObtenerTendenciasUseCase`, `IncrementarClicsUseCase`):** Intermediarios que reciben las órdenes de la UI y utilizan el `LibroGateway`. Respetan el Principio de Responsabilidad Única (SOLID).

### 3. Capa de Infraestructura: Persistencia (`infrastructure/adapter/out/persistence/...`)
Implementación técnica de la persistencia de datos usando la base de datos H2.
* **`JdbcLibroGateway.java`:** Implementa la interfaz `LibroGateway`. Crea la tabla de libros mediante SQL si no existe e inyecta libros de prueba. Utiliza la base de datos H2 en modo embebido (`jdbc:h2:./mylib`) guardando los datos en `mylib.mv.db`. Emplea `PreparedStatement` para prevenir inyección SQL y optimizar las búsquedas (LIKE).

### 4. Capa de Infraestructura: Interfaz Gráfica (`infrastructure/adapter/in/ui/...`)
Maneja la presentación y la interacción del usuario.
* **`BibliotecaView.fxml`:** Archivo XML declarativo que define la interfaz gráfica (botones, listas, diseño).
* **`BibliotecaController.java`:** El "cerebro" de la pantalla. Recibe los casos de uso por inyección de dependencias.
* **Hilos Concurrentes (`javafx.concurrent.Task`):** Las consultas a base de datos se envuelven en un `Task` y se ejecutan en un hilo secundario (`new Thread()`). Esto previene que la interfaz se congele, manteniendo la aplicación fluida mientras H2 procesa la solicitud.

### 5. Configuración y Arranque (`Main.java` y `Launcher.java`)
* **`Main.java`:** El punto de ensamblaje (Composición Root). Aquí se instancia la conexión a la base de datos (`JdbcLibroGateway`), se inyecta a los Casos de Uso, y éstos a su vez al Controlador. Se utiliza Inyección de Dependencias Manual.
* **`Launcher.java`:** Clase encargada de arrancar `Main.main()`. Soluciona las restricciones de módulos introducidas desde Java 11 para JavaFX al ejecutar la aplicación directamente desde el IDE.

### 6. Pruebas de Integración (`JdbcLibroGatewayTest.java`)
Entorno de pruebas automatizadas que utiliza una base de datos en memoria (`jdbc:h2:mem:testdb`). Asegura que las operaciones SQL funcionen correctamente y se destruye al terminar la prueba sin afectar los datos reales de la aplicación.

> **Resumen:** Se ha diseñado un sistema donde la UI se comunica con los Casos de Uso, éstos con la Interfaz del Gateway, y finalmente la implementación en H2 maneja la persistencia. Esto garantiza un código escalable, fácil de mantener y de testear.

---

## Explicación de la Arquitectura - Historia (Carrito de Compras) - Versión 2

En la **Versión 2 (v2)**, se implementó la gestión del Carrito de Compras manteniendo la filosofía de Arquitectura Limpia, sin romper lo que ya estaba hecho.

### 1. Capa de Dominio
* **`Libro.java`:** Se actualizó para incluir `precio` y `portada`.
* **`CarritoItem.java`:** Se creó esta nueva entidad para representar "un renglón" del carrito, asociando un libro con su cantidad elegida, y un método para calcular su subtotal de forma dinámica.

### 2. Capa de Aplicación
Se creó el contrato o puerto de salida `CarritoGateway` y tres nuevos Casos de Uso:
* **`AgregarAlCarritoUseCase`, `EliminarDelCarritoUseCase`, `VerCarritoUseCase`:** Clases puras que intermedian entre la interfaz gráfica y la persistencia.

### 3. Capa de Infraestructura: Persistencia
* **`JdbcLibroGateway.java`:** Modificado para inyectar precios reales al momento de crear la base de datos `mylib.mv.db`.
* **`JdbcCarritoGateway.java`:** Nueva clase que crea y gestiona la tabla `carrito_items`. Implementa operaciones de `UPDATE` para sumar cantidades si el libro ya está, e `INSERT` si no lo está. Utiliza `JOIN` para unir la tabla de libros con el carrito y devolver los datos completos al usuario.

### 4. Capa de Infraestructura: Interfaz Gráfica
* **`BibliotecaView.fxml`:** Se añadió un contenedor dinámico (`StackPane`) que permite alternar la vista entre el Catálogo y el Carrito. Se integró el botón "🛒 Ver Carrito".
* **`BibliotecaController.java`:** 
  * Se genera y asigna un **Session ID (`UUID`)** único en memoria para no mezclar carritos.
  * Se asignan hilos secundarios (`Task`) para añadir y eliminar del carrito sin congelar la pantalla.
  * Colecciones `ObservableList<CarritoItem>` mantienen actualizado el total a pagar y la cantidad del botón superior en tiempo real.

### 5. Configuración y Pruebas
* **`Main.java`:** Se instanciaron manualmente las dependencias de los 3 nuevos Casos de Uso y se inyectaron en el Controlador.
* **`JdbcCarritoGatewayTest.java`:** Pruebas automatizadas en H2 (`jdbc:h2:mem:test_carrito`) validando sumas, eliminaciones y cálculo de subtotales.

> Puedes explorar el repositorio navegando por la rama/tag `v1` (solo búsqueda) y `v2` (búsqueda + carrito) para ver paso a paso cómo evolucionó el código bajo esta arquitectura.

---

## Explicación de la Arquitectura - Historia (Registro de Usuarios) - Versión 3

En la **Versión 3 (v3)**, se implementó el módulo de Registro de Usuarios y un rediseño visual inmersivo completo, manteniendo la filosofía de Arquitectura Limpia sin romper lo ya construido.

### 1. Capa de Dominio
* **`Usuario.java`:** Se creó la entidad para representar a un usuario en el sistema, con los atributos `id`, `email`, `passwordHash`, `nombre` y estado `activo`.
* **`UsuarioYaExisteException.java`:** Se creó esta excepción de negocio (pura) para manejar la regla de dominio que impide registrar dos veces el mismo correo electrónico.

### 2. Capa de Aplicación
Se creó el contrato o puerto de salida `UsuarioGateway` y un nuevo Caso de Uso:
* **`RegistrarUsuarioUseCase.java`:** Clase pura que intermedia entre la interfaz y la persistencia. Se encarga de aplicar la regla de negocio: verifica si el correo ya existe, encripta la contraseña en texto plano utilizando **BCrypt**, y finalmente llama al gateway para persistir la información segura.

### 3. Capa de Infraestructura: Persistencia
* **`JdbcUsuarioGateway.java`:** Nueva clase que crea y gestiona la tabla `usuarios` en H2. Define el campo `email` con la restricción `UNIQUE`. Implementa las operaciones SQL (`INSERT` para registrar y `SELECT` para verificar la existencia del correo) a través de JDBC puro.

### 4. Capa de Infraestructura: Interfaz Gráfica
* **`RegistroView.fxml`:** Se diseñó una nueva vista modal (ventana emergente) limpia y moderna, con campos de texto para nombre, email y contraseña, además de mensajes de error en tiempo real.
* **`RegistroController.java`:**
  * Se encarga de capturar la interacción del usuario en la ventana de registro.
  * Se asignan hilos secundarios (`Task`) para procesar la encriptación de BCrypt y la inserción en base de datos de manera asíncrona, evitando congelar la interfaz principal.
  * Gestiona las alertas visuales y el cierre automático del modal tras un registro exitoso.
* **`BibliotecaView.fxml` y `BibliotecaController.java` (Rediseño Visual v3):**
  * La pantalla principal fue rediseñada con un sistema de capas (`StackPane`) que permite mostrar una imagen de fondo de pantalla completa de forma inmersiva.
  * Se implementó un **carrusel automático** de 3 imágenes artísticas que rotan mediante una animación suave de desvanecimiento (`FadeTransition`) cada 7 segundos usando un `Timeline` cíclico.
  * Se implementó un **efecto de desenfoque dinámico** (`GaussianBlur`) sobre el fondo: al hacer clic en el buscador el fondo se difumina levemente y al escribir se desenfoca completamente para dar protagonismo a los resultados.
  * Se añadió una **Frase del Día** de autores célebres (Borges, Kafka, García Márquez, Cervantes, etc.) que rota aleatoriamente junto al cambio de imagen de fondo.
  * El flujo de registro fue integrado directamente dentro del **Carrito de Compras** como un botón de "Continuar con el proceso", haciendo el flujo de compra más natural.

### 5. Configuración y Pruebas
* **`pom.xml`:** Se integró la librería de seguridad `org.mindrot:jbcrypt` para garantizar un cifrado profesional de las contraseñas.
* **`Main.java`:** Se instanció manualmente el nuevo `JdbcUsuarioGateway` junto con el `RegistrarUsuarioUseCase`, inyectándolos en la cascada de dependencias hasta llegar al controlador.
* **`JdbcUsuarioGatewayTest.java`:** Pruebas automatizadas en H2 en memoria que validan tanto la correcta persistencia de un usuario nuevo como el comportamiento esperado (lanzamiento de excepción) al intentar insertar correos duplicados.

> Puedes explorar el repositorio navegando por los tags `v1`, `v2`, `v3`, `v4` y `v5` para ver paso a paso cómo evolucionó el código bajo esta arquitectura.

---

## Explicación de la Arquitectura - Historia (Sistema Completo Casit) - Versión 4

En la **Versión 4 (v4)**, el proyecto alcanzó la funcionalidad requerida del módulo BUYER integrando características avanzadas de seguridad, facturación e historial.

### 1. Capa de Dominio
* **`Orden.java` y `Compra.java`:** Entidades encargadas de gestionar el estado de los pedidos y registrar los items adquiridos.
* **Excepciones de Negocio:** Se añadieron `CredencialesInvalidasException` y `UsuarioBloqueadoException`.

### 2. Capa de Aplicación
* **Autenticación:** `LoginUsuarioUseCase` y `CambiarContrasenaUseCase`.
* **Procesamiento:** `CheckoutController` utiliza `RegistrarCompraUseCase`.
* **Generación de Archivos:** `GenerarFacturaPdfUseCase` toma una orden finalizada y emite un PDF directamente.
* **Historial y Biblioteca:** `ObtenerHistorialOrdenesUseCase` y `ObtenerBibliotecaPersonalUseCase` traen los libros comprados.
* **Búsqueda:** Se optimizó con `BuscarLibroAvanzadoUseCase`.

### 3. Capa de Infraestructura: Interfaz Gráfica
* **`LoginView.fxml` y `CambiarPasswordView.fxml`:** Integradas al flujo de seguridad.
* **`CheckoutView.fxml`:** Simula el entorno de pago y confirma transacciones.
* **`HistorialComprasView.fxml` y `BibliotecaPersonalView.fxml`:** Proveen un dashboard al usuario.

---

## Mejoras de Rendimiento y Archivos Reales - Versión 5

En la **Versión 5 (v5)**, se introdujeron mejoras importantes de calidad de vida y se incluyeron los libros reales en el sistema:

### 1. Sistema de Caché de Imágenes
* **`BibliotecaController.java`:** Se implementó una memoria caché estática (`imageCache`) para las portadas de los libros. Esto mejora dramáticamente el rendimiento gráfico de la aplicación, previniendo que se descargue o decodifique la misma imagen múltiples veces.

### 2. Procesamiento de PDFs Reales
* **`BibliotecaPersonalController.java`:** Dejó de generar archivos de texto simulados y ahora interactúa con PDFs físicos:
  * **Descarga Segura:** Transmite el contenido binario del PDF en `src/main/resources/books/` hacia la ruta elegida por el usuario.
  * **Lectura en Línea:** Copia el PDF a un archivo temporal y utiliza `java.awt.Desktop.getDesktop().open()` para delegar la apertura del eBook al visor nativo del sistema operativo del usuario.

### 3. Archivos Embebidos
* Se añadieron 12 libros y guías reales (Clean Code, Effective Java, Sapiens, 1984, etc.) al directorio `resources` de la aplicación, consolidando una experiencia completa.
