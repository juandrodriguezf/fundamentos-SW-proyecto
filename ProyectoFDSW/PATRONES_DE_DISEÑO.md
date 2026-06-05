# 🏗️ Patrones de Diseño Aplicados — OpenLibMarket

**Proyecto**: OpenLibMarket — Biblioteca Virtual  
**Materia**: Fundamentos de Software  
**Universidad**: Pontificia Universidad Javeriana

Este documento explica los **5 patrones de diseño** implementados en el proyecto, indicando **exactamente dónde se encuentran en el código** y **por qué se aplicaron**.

---

## 📁 Estructura del Proyecto (Clean Architecture)

```
src/main/java/co/edu/javeriana/proyecto/
├── domain/                          ← Entidades del negocio
│   ├── Libro.java
│   ├── Usuario.java
│   ├── Compra.java
│   ├── Orden.java
│   ├── CarritoItem.java
│   └── exception/
│       ├── CredencialesInvalidasException.java
│       ├── UsuarioBloqueadoException.java
│       └── UsuarioYaExisteException.java
├── application/                     ← Lógica de aplicación
│   ├── port/out/                    ← Interfaces (contratos)
│   │   ├── LibroGateway.java
│   │   ├── UsuarioGateway.java
│   │   ├── CarritoGateway.java
│   │   └── CompraGateway.java
│   └── usecase/                     ← Casos de uso (operaciones)
│       ├── LoginUsuarioUseCase.java
│       ├── RegistrarUsuarioUseCase.java
│       ├── AgregarAlCarritoUseCase.java
│       ├── SubirLibroUseCase.java
│       ├── AprobarLibroUseCase.java
│       └── ... (18 casos de uso en total)
├── infrastructure/                  ← Implementaciones concretas
│   └── adapter/
│       ├── in/ui/                   ← Controladores JavaFX
│       │   ├── BibliotecaController.java
│       │   ├── LoginController.java
│       │   ├── CheckoutController.java
│       │   └── ...
│       └── out/persistence/         ← Acceso a base de datos H2
│           ├── JdbcLibroGateway.java
│           ├── JdbcUsuarioGateway.java
│           ├── JdbcCarritoGateway.java
│           └── JdbcCompraGateway.java
└── Main.java                        ← Punto de entrada y ensamblaje
```

---

## 1. 🔀 Patrón Strategy (Estrategia)

### ¿Qué es?
Define una familia de algoritmos intercambiables. El cliente usa una interfaz sin conocer la implementación concreta.

### ¿Dónde está en el código?

Las **interfaces Gateway** son las estrategias, y las **clases Jdbc\*Gateway** son las implementaciones concretas. Los casos de uso nunca saben qué base de datos se usa — solo conocen la interfaz.

| Archivo | Ruta | Rol |
|---------|------|-----|
| `LibroGateway.java` | `application/port/out/LibroGateway.java` | **Interfaz Strategy** — define las operaciones disponibles |
| `UsuarioGateway.java` | `application/port/out/UsuarioGateway.java` | **Interfaz Strategy** |
| `CarritoGateway.java` | `application/port/out/CarritoGateway.java` | **Interfaz Strategy** |
| `CompraGateway.java` | `application/port/out/CompraGateway.java` | **Interfaz Strategy** |
| `JdbcLibroGateway.java` | `infrastructure/adapter/out/persistence/JdbcLibroGateway.java` | **Estrategia concreta** — implementa con H2/JDBC |
| `JdbcUsuarioGateway.java` | `infrastructure/adapter/out/persistence/JdbcUsuarioGateway.java` | **Estrategia concreta** |
| `JdbcCarritoGateway.java` | `infrastructure/adapter/out/persistence/JdbcCarritoGateway.java` | **Estrategia concreta** |
| `JdbcCompraGateway.java` | `infrastructure/adapter/out/persistence/JdbcCompraGateway.java` | **Estrategia concreta** |
| `Main.java` | `Main.java` (líneas 26-29) | **Cliente** — elige qué estrategia inyectar |

### Ejemplo directo en el código

**Interfaz (Strategy)** — `application/port/out/LibroGateway.java`:
```java
public interface LibroGateway {
    List<Libro> buscarPorTitulo(String filtro);
    List<Libro> buscarAvanzado(String texto, String categoria, double precioMin, double precioMax, String ordenamiento);
    List<Libro> obtenerTendencias(int limite);
    void incrementarClics(Long libroId);
    void guardar(Libro libro);
    void actualizarEstado(Long libroId, String estado);
}
```

**Implementación concreta (ConcreteStrategy)** — `infrastructure/adapter/out/persistence/JdbcLibroGateway.java`:
```java
public class JdbcLibroGateway implements LibroGateway {
    private final String url;
    // Implementa todos los métodos usando SQL y JDBC
    @Override
    public List<Libro> buscarPorTitulo(String filtro) {
        String sql = "SELECT * FROM libros WHERE LOWER(titulo) LIKE ?";
        // ... ejecuta contra H2
    }
}
```

**Inyección en Main.java** (líneas 26-31):
```java
LibroGateway libroGateway = new JdbcLibroGateway(DB_URL);       // Se elige la estrategia concreta
BuscarLibroUseCase buscarUC = new BuscarLibroUseCase(libroGateway); // Se inyecta al caso de uso
```

### ¿Por qué se aplicó?
Si mañana queremos cambiar de H2 a MySQL o MongoDB, solo hay que crear una nueva clase `MongoLibroGateway implements LibroGateway` y cambiar **una sola línea** en `Main.java`. Ningún caso de uso se modifica.

---

## 2. 📋 Patrón Command (Comando)

### ¿Qué es?
Encapsula una solicitud como un objeto independiente, con toda la información necesaria para ejecutarla. Permite desacoplar quién pide la acción de quién la ejecuta.

### ¿Dónde está en el código?

Cada **caso de uso** (UseCase) es un **Command**. Todos siguen la misma estructura:
- Una clase por operación
- Un método `ejecutar(...)` que realiza la acción
- Recibe su dependencia (Receiver) por constructor

| Archivo (Command) | Ruta | Acción que encapsula |
|--------------------|------|----------------------|
| `AgregarAlCarritoUseCase.java` | `application/usecase/` | Agregar libro al carrito |
| `EliminarDelCarritoUseCase.java` | `application/usecase/` | Eliminar libro del carrito |
| `LimpiarCarritoUseCase.java` | `application/usecase/` | Vaciar todo el carrito |
| `RegistrarCompraUseCase.java` | `application/usecase/` | Registrar una compra en BD |
| `RegistrarUsuarioUseCase.java` | `application/usecase/` | Registrar nuevo usuario |
| `LoginUsuarioUseCase.java` | `application/usecase/` | Autenticar usuario |
| `BuscarLibroUseCase.java` | `application/usecase/` | Buscar libros por título |
| `SubirLibroUseCase.java` | `application/usecase/` | Publicador sube un libro |
| `AprobarLibroUseCase.java` | `application/usecase/` | Admin aprueba un libro |
| `GenerarFacturaPdfUseCase.java` | `application/usecase/` | Generar factura en PDF |
| **... y 8 más** | `application/usecase/` | (18 commands en total) |

**Invoker (quien dispara los comandos):**
| Archivo (Invoker) | Ruta |
|--------------------|------|
| `BibliotecaController.java` | `infrastructure/adapter/in/ui/` |
| `LoginController.java` | `infrastructure/adapter/in/ui/` |
| `CheckoutController.java` | `infrastructure/adapter/in/ui/` |
| `RegistroController.java` | `infrastructure/adapter/in/ui/` |

### Ejemplo directo en el código

**Command** — `application/usecase/AgregarAlCarritoUseCase.java`:
```java
public class AgregarAlCarritoUseCase {
    private final CarritoGateway carritoGateway;  // Receiver

    public AgregarAlCarritoUseCase(CarritoGateway carritoGateway) {
        this.carritoGateway = carritoGateway;
    }

    public void ejecutar(String sessionId, Long libroId, int cantidad) {
        carritoGateway.agregarItem(sessionId, libroId, cantidad);  // Ejecuta la acción
    }
}
```

**Invoker** — `BibliotecaController.java` llama al comando:
```java
agregarAlCarritoUseCase.ejecutar(sessionId, libro.getId(), 1);
```

**Ensamblaje en Main.java** (líneas 35-38):
```java
AgregarAlCarritoUseCase agregarAlCarritoUseCase = new AgregarAlCarritoUseCase(carritoGateway);
EliminarDelCarritoUseCase eliminarDelCarritoUseCase = new EliminarDelCarritoUseCase(carritoGateway);
VerCarritoUseCase verCarritoUseCase = new VerCarritoUseCase(carritoGateway);
LimpiarCarritoUseCase limpiarCarritoUseCase = new LimpiarCarritoUseCase(carritoGateway);
```

### ¿Por qué se aplicó?
Cada operación del sistema está **aislada en su propia clase**. Esto facilita testear, mantener y agregar nuevas operaciones sin afectar las existentes. Además, el Controller no sabe cómo se ejecuta la lógica — solo invoca el comando.

---

## 3. 📝 Patrón Transaction Script

### ¿Qué es?
Organiza la lógica de negocio como un procedimiento secuencial que maneja una transacción completa de principio a fin. Cada script recibe la solicitud, la procesa paso a paso, y produce el resultado.

### ¿Dónde está en el código?

Los casos de uso más complejos implementan Transaction Script dentro de su método `ejecutar()`.

| Archivo | Ruta | Transacción que gestiona |
|---------|------|--------------------------|
| `LoginUsuarioUseCase.java` | `application/usecase/` | Login completo con bloqueo por intentos |
| `RegistrarUsuarioUseCase.java` | `application/usecase/` | Registro de usuario paso a paso |
| `CambiarContrasenaUseCase.java` | `application/usecase/` | Cambio de contraseña con reactivación |
| `SubirLibroUseCase.java` | `application/usecase/` | Subida de libro con validación de formato |
| `ObtenerHistorialOrdenesUseCase.java` | `application/usecase/` | Agrupar compras en órdenes |
| `CheckoutController.java` → `procesarPago()` | `infrastructure/adapter/in/ui/` (línea 196) | Proceso completo de pago |

### Ejemplo directo en el código

**Transaction Script más completo** — `application/usecase/LoginUsuarioUseCase.java` (líneas 26-75):
```java
public Usuario ejecutar(String email, String rawPassword) {
    // PASO 1: Buscar usuario en la base de datos
    Optional<Usuario> optionalUsuario = usuarioGateway.buscarPorEmail(email);
    if (optionalUsuario.isEmpty()) {
        throw new CredencialesInvalidasException("Credenciales incorrectas.");
    }
    Usuario usuario = optionalUsuario.get();

    // PASO 2: Verificar si la cuenta está bloqueada
    if (!usuario.isActivo()) {
        throw new UsuarioBloqueadoException("Tu cuenta ha sido bloqueada...");
    }

    // PASO 3: Verificar contraseña con BCrypt
    boolean passwordCorrecta = BCrypt.checkpw(rawPassword, usuario.getPasswordHash());

    // PASO 4: Si es incorrecta, incrementar intentos fallidos
    if (!passwordCorrecta) {
        int nuevosIntentos = usuario.getIntentosFallidos() + 1;
        if (nuevosIntentos >= MAX_INTENTOS) {
            // PASO 4a: Bloquear cuenta si llega a 3 intentos
            usuarioGateway.actualizarActivo(email, false);
            usuarioGateway.actualizarIntentosFallidos(email, nuevosIntentos);
            throw new UsuarioBloqueadoException("Has superado los 3 intentos...");
        } else {
            // PASO 4b: Guardar intento fallido
            usuarioGateway.actualizarIntentosFallidos(email, nuevosIntentos);
            throw new CredencialesInvalidasException("Contraseña incorrecta. Te quedan " + restantes + " intentos.");
        }
    }

    // PASO 5: Login exitoso — resetear intentos
    usuarioGateway.actualizarIntentosFallidos(email, 0);

    // PASO 6: Retornar usuario autenticado
    return usuario;
}
```

**Otro ejemplo** — `application/usecase/RegistrarUsuarioUseCase.java` (líneas 15-40):
```java
public void ejecutar(String email, String rawPassword, String nombre) {
    // PASO 1: Validar formato de email
    if (email == null || !email.contains("@")) {
        throw new IllegalArgumentException("Email inválido.");
    }
    // PASO 2: Validar longitud de contraseña
    if (rawPassword == null || rawPassword.length() < 6) {
        throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
    }
    // PASO 3: Verificar que el email no esté registrado
    if (usuarioGateway.existeEmail(email)) {
        throw new UsuarioYaExisteException("El correo ya está registrado.");
    }
    // PASO 4: Cifrar contraseña con BCrypt
    String passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    // PASO 5: Crear el objeto de dominio
    Usuario nuevoUsuario = new Usuario(null, email, passwordHash, nombre, true);
    // PASO 6: Persistir en base de datos
    usuarioGateway.guardar(nuevoUsuario);
    // PASO 7: Notificación
    System.out.println("[SISTEMA] ¡Correo de bienvenida simulado enviado a " + email + "!");
}
```

### ¿Por qué se aplicó?
Cada operación de negocio tiene una secuencia clara de pasos que deben ejecutarse en orden. El Transaction Script permite leer el flujo completo de una transacción en un solo lugar, facilitando la comprensión y el debugging.

---

## 4. 🧬 Patrón Prototype (Prototipo)

### ¿Qué es?
Permite crear nuevos objetos **copiando o basándose en una instancia existente** (prototipo), en lugar de construirlos completamente desde cero. Se usa cuando hay valores por defecto o plantillas reutilizables.

### ¿Dónde está en el código?

| Archivo | Ruta | Uso del Prototype |
|---------|------|-------------------|
| `SubirLibroUseCase.java` | `application/usecase/` (líneas 18-29) | Crea libros con valores prototipo predeterminados |
| `JdbcLibroGateway.java` | `infrastructure/adapter/out/persistence/` (líneas 295-310) | `extraerLibro()` — clona datos del ResultSet a objetos Libro |
| `JdbcLibroGateway.java` | `infrastructure/adapter/out/persistence/` (líneas 53-89) | Datos semilla como arreglo de prototipos |

### Ejemplo directo en el código

**Prototype con valores predeterminados** — `application/usecase/SubirLibroUseCase.java` (líneas 13-29):
```java
public void subirLibro(String titulo, String autor, String categoria, double precio, String rutaArchivo) {
    // Validar formato del archivo
    if (rutaArchivo == null || (!rutaArchivo.toLowerCase().endsWith(".pdf") 
            && !rutaArchivo.toLowerCase().endsWith(".epub"))) {
        throw new IllegalArgumentException("Formato no soportado. Use PDF o EPUB.");
    }

    // PROTOTYPE: Se crea un objeto base y se le asignan valores prototipo fijos
    Libro nuevoLibro = new Libro();              // Instancia base (prototipo)
    nuevoLibro.setTitulo(titulo);                // Dato del usuario
    nuevoLibro.setAutor(autor);                  // Dato del usuario
    nuevoLibro.setCategoria(categoria);           // Dato del usuario
    nuevoLibro.setPrecio(precio);                 // Dato del usuario
    nuevoLibro.setRutaArchivo(rutaArchivo);       // Dato del usuario
    nuevoLibro.setEstado("PENDIENTE");            // ← Valor prototipo fijo
    nuevoLibro.setCalificacionPromedio(0.0);      // ← Valor prototipo fijo
    nuevoLibro.setClics(0);                       // ← Valor prototipo fijo
    nuevoLibro.setStock(1);                       // ← Valor prototipo fijo

    libroGateway.guardar(nuevoLibro);
}
```

**Prototype desde base de datos** — `JdbcLibroGateway.java` (líneas 295-310):
```java
// Método reutilizable que "clona" un registro de BD a un objeto Java
private Libro extraerLibro(ResultSet rs) throws SQLException {
    return new Libro(
        rs.getLong("id"),
        rs.getString("titulo"),
        rs.getString("autor"),
        rs.getString("isbn"),
        rs.getString("categoria"),
        rs.getString("etiquetas"),
        rs.getInt("clics"),
        rs.getDouble("precio"),
        rs.getString("portada"),
        rs.getInt("stock"),
        rs.getDouble("calificacion_promedio"),
        rs.getString("estado"),
        rs.getString("ruta_archivo")
    );
}
// Se reutiliza en: buscarPorTitulo(), buscarAvanzado(), obtenerTendencias()
```

**Prototipos de datos semilla** — `JdbcLibroGateway.java` (líneas 60-73):
```java
// Cada fila del arreglo actúa como un PROTOTIPO/plantilla de libro
Object[][] libros = {
    {"Clean Code",        "Robert C. Martin",  "978-0132350884", "Ingeniería de Software", ...},
    {"Design Patterns",   "Erich Gamma",       "978-0201633610", "Arquitectura",           ...},
    {"Cien Años de Soledad", "García Márquez", "978-0307474728", "Literatura",              ...},
    // ... 12 prototipos en total
};
for (Object[] libro : libros) {
    // Se clonan los datos del prototipo al PreparedStatement
    pstmt.setString(1, (String) libro[0]);
    pstmt.setString(2, (String) libro[1]);
    // ...
}
```

### ¿Por qué se aplicó?
Evita repetir la lógica de creación de objetos. Cuando se sube un libro nuevo, siempre empieza con `estado="PENDIENTE"`, `clics=0`, `stock=1` — estos valores prototipo son reutilizados consistentemente.

---

## 5. 🔄 Patrón Estado (State)

### ¿Qué es?
Permite que un objeto cambie su comportamiento cuando su estado interno cambia. Las transiciones de estado están claramente definidas y el sistema reacciona de forma diferente según el estado actual.

### ¿Dónde está en el código?

El patrón Estado se aplica en **tres partes del sistema**:

### 5.1 — Estado del Usuario (Activo / Bloqueado)

| Archivo | Ruta | Rol |
|---------|------|-----|
| `Usuario.java` | `domain/Usuario.java` (campos `activo`, `intentosFallidos`) | Entidad con estado |
| `LoginUsuarioUseCase.java` | `application/usecase/` (líneas 26-75) | Gestiona las transiciones |
| `CambiarContrasenaUseCase.java` | `application/usecase/` (líneas 23-43) | Permite recuperación |

**Estados y transiciones:**

```
ACTIVO (activo=true, intentos=0)
  │
  ├── Login exitoso → se mantiene ACTIVO (resetea intentos a 0)
  │
  ├── Contraseña incorrecta (intentos < 3) → EN_RIESGO (activo=true, intentos 1-2)
  │
  └── 3er intento fallido → BLOQUEADO (activo=false, intentos=3)
                                │
                                └── Cambiar contraseña → vuelve a ACTIVO
```

**Código de transiciones** — `LoginUsuarioUseCase.java`:
```java
// Verificar estado BLOQUEADO
if (!usuario.isActivo()) {
    throw new UsuarioBloqueadoException("Tu cuenta ha sido bloqueada...");
}

// Transición a BLOQUEADO cuando se alcanzan 3 intentos
if (nuevosIntentos >= MAX_INTENTOS) {
    usuarioGateway.actualizarActivo(email, false);          // → BLOQUEADO
}

// Login exitoso: transición a ACTIVO
usuarioGateway.actualizarIntentosFallidos(email, 0);        // → ACTIVO
```

**Recuperación** — `CambiarContrasenaUseCase.java`:
```java
// Actualizar contraseña también reactiva la cuenta (resetea intentos y activa)
usuarioGateway.actualizarPassword(email, nuevoHash);        // → ACTIVO
```

### 5.2 — Estado del Libro (Pendiente / Aprobado)

| Archivo | Ruta | Rol |
|---------|------|-----|
| `Libro.java` | `domain/Libro.java` (campo `estado`) | Entidad con estado |
| `SubirLibroUseCase.java` | `application/usecase/` (línea 24) | Establece estado inicial PENDIENTE |
| `AprobarLibroUseCase.java` | `application/usecase/` (línea 13) | Transición a APROBADO |
| `JdbcLibroGateway.java` | `infrastructure/adapter/out/persistence/` (líneas 95, 116, 186) | Filtra por estado en queries |

**Estados y transiciones:**

```
PENDIENTE (libro recién subido por publicador)
  │
  └── Admin aprueba → APROBADO (visible en catálogo, buscable, comprable)
```

**Código de transiciones:**
```java
// SubirLibroUseCase.java — Estado inicial
nuevoLibro.setEstado("PENDIENTE");

// AprobarLibroUseCase.java — Transición a APROBADO
public void aprobarLibro(Long libroId) {
    libroGateway.actualizarEstado(libroId, "APROBADO");
}

// JdbcLibroGateway.java — El estado afecta el comportamiento (filtrado)
String sql = "SELECT * FROM libros WHERE estado = 'APROBADO' AND ...";
// Los libros PENDIENTES NO aparecen en búsquedas ni tendencias
```

### 5.3 — Estado del Checkout (Wizard de 3 Pasos)

| Archivo | Ruta | Rol |
|---------|------|-----|
| `CheckoutController.java` | `infrastructure/adapter/in/ui/` (líneas 115-145) | Máquina de estados del checkout |

**Estados y transiciones:**

```
Paso 1 (Dirección) → Paso 2 (Pago) → Paso 3 (Revisión) → Pago Completado
       ←                    ←
```

**Código de transiciones** — `CheckoutController.java`:
```java
// Transición a Paso 2 (con validación)
private void irAPaso2() {
    if (txtNombre.getText().isEmpty() || txtDireccion.getText().isEmpty()) {
        mostrarErrorAlerta("Por favor completa los datos.");
        return;  // No cambia de estado si falla validación
    }
    vboxPaso1.setVisible(false);  // Ocultar estado anterior
    vboxPaso2.setVisible(true);   // Mostrar estado actual
    actualizarProgreso(2);        // Actualizar indicador visual
}

// Transición a Paso 3
private void irAPaso3() { ... }

// Estado final: Pago completado
private void procesarPago() {
    // Simula pago → registra compra → limpia carrito → muestra confirmación
    btnPagar.setVisible(false);
    btnVolver.setVisible(true);  // Cambia controles según estado final
}
```

### ¿Por qué se aplicó?
El sistema necesita comportarse de forma diferente según el estado: un usuario bloqueado no puede hacer login, un libro pendiente no aparece en búsquedas, y el checkout avanza por pasos controlados. Sin el patrón Estado, esta lógica estaría dispersa y sería difícil de mantener.

---

## 📊 Resumen General

| # | Patrón | Archivos Principales | Propósito |
|---|--------|----------------------|-----------|
| 1 | **Strategy** | `*Gateway.java` (interfaces) → `Jdbc*Gateway.java` (impl) | Intercambiar implementaciones de persistencia sin modificar la lógica de negocio |
| 2 | **Command** | 18 clases `*UseCase.java` con método `ejecutar()` | Encapsular cada operación del sistema como un objeto independiente |
| 3 | **Transaction Script** | `LoginUsuarioUseCase`, `RegistrarUsuarioUseCase`, `CambiarContrasenaUseCase` | Organizar transacciones complejas como secuencias de pasos |
| 4 | **Prototype** | `SubirLibroUseCase`, `JdbcLibroGateway.extraerLibro()` | Crear objetos con valores base predeterminados reutilizables |
| 5 | **Estado** | `Usuario` (activo/bloqueado), `Libro` (pendiente/aprobado), `CheckoutController` (wizard) | Controlar el comportamiento del sistema según el estado actual de las entidades |

---

## 🔗 Relación entre los Patrones

```
┌─────────────────────────────────────────────────────────────┐
│                        Main.java                            │
│    Ensambla Strategy (inyecta Gateways a Use Cases)         │
└──────────────────────────┬──────────────────────────────────┘
                           │
            ┌──────────────▼──────────────┐
            │     Controllers (Invoker)    │
            │  Disparan Commands y         │
            │  gestionan Estado del UI     │
            │  (CheckoutController wizard) │
            └──────────────┬──────────────┘
                           │
            ┌──────────────▼──────────────┐
            │    Use Cases (Command +      │
            │    Transaction Script)       │
            │  ejecutar() = script         │
            │  secuencial de la            │
            │  transacción                 │
            └──────────────┬──────────────┘
                           │
           ┌───────────────┼───────────────┐
           │               │               │
    ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
    │  Gateway    │ │  Domain     │ │  Prototype  │
    │ (Strategy)  │ │  Entities   │ │  (creación  │
    │  Interface  │ │  con State  │ │  de objetos)│
    │      ↓      │ │             │ │             │
    │ Jdbc*Gateway│ │ Usuario:    │ │ SubirLibro: │
    │ (Concrete)  │ │ activo/bloq │ │ estado=PEND │
    │             │ │ Libro:      │ │ clics=0     │
    │             │ │ pend/aprob  │ │ stock=1     │
    └─────────────┘ └─────────────┘ └─────────────┘
```
