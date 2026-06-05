# 🎓 OpenLibMarket — Guía Completa de Sustentación

**Proyecto**: OpenLibMarket — Biblioteca Virtual  
**Materia**: Fundamentos de Software  
**Universidad**: Pontificia Universidad Javeriana

Este documento está diseñado como una **guía estructurada para la exposición frente al profesor**. Detalla la arquitectura utilizada, las decisiones de diseño, las tecnologías y un resumen completo de todo lo que se construyó.

---

## 1. 📖 Visión General del Proyecto
**OpenLibMarket** es una plataforma de software de escritorio simulando una tienda de libros digitales y biblioteca virtual. Permite a los usuarios explorar un catálogo, ver tendencias, recibir recomendaciones basadas en su historial, agregar productos a un carrito, realizar procesos de checkout (pago) y obtener facturas en PDF. A su vez, permite la publicación y aprobación de nuevos libros.

El objetivo principal de este proyecto no fue solo hacer que "funcionara", sino **aplicar buenas prácticas de Ingeniería de Software**, enfocándonos fuertemente en el diseño, la arquitectura y los patrones de diseño.

---

## 2. 🏛️ Arquitectura Utilizada: Clean Architecture (Arquitectura Limpia)

### ¿Qué arquitectura se usó?
El proyecto fue construido bajo los principios de **Clean Architecture** (Arquitectura Limpia), popularizada por Robert C. Martin (Uncle Bob), utilizando fuertemente el patrón de **Puertos y Adaptadores** (Arquitectura Hexagonal).

### ¿Por qué se eligió esta arquitectura?
En la industria del software, el código tiende a acoplarse a la base de datos o a la interfaz gráfica. Si mañana quisiéramos cambiar JavaFX por una página web (Spring Boot + React), o la base de datos H2 por MongoDB, en un diseño tradicional tendríamos que reescribir casi todo.

**Con Clean Architecture logramos:**
1. **Independencia de Frameworks**: La lógica de negocio pura no depende de JavaFX ni de librerías externas.
2. **Independencia de la Base de Datos**: El núcleo del sistema no sabe que usamos H2 o SQL. Solo conoce "interfaces" (Puertos).
3. **Alta Testabilidad**: Al estar todo desacoplado, podemos probar la lógica de negocio simulando (mocking) la base de datos fácilmente.
4. **Mantenibilidad a largo plazo**: Cumple a rajatabla con los principios **S.O.L.I.D.** (especialmente Inversión de Dependencias y Responsabilidad Única).

### Explicación de las Capas (Cómo está estructurado)

El código está dividido en 3 grandes capas concéntricas (de adentro hacia afuera):

#### 🟩 1. Capa de Dominio (`domain`) - El Corazón
Aquí viven las **Entidades** puras de Java (`Libro`, `Usuario`, `Compra`, `Orden`, `CarritoItem`).
* **Regla de oro**: Esta capa no importa nada de afuera. No sabe de bases de datos, no sabe de pantallas. Solo contiene los datos y las reglas esenciales de las entidades.

#### 🟦 2. Capa de Aplicación (`application`) - El Cerebro
Contiene los **Casos de Uso** (`usecase`) y los **Puertos** (`port`).
* **Casos de Uso**: Son los orquestadores. Hay 18 en total (ej. `LoginUsuarioUseCase`, `AgregarAlCarritoUseCase`). Cada uno encapsula una única operación (Patrón Command y Transaction Script).
* **Puertos de Salida (`port.out`)**: Son interfaces (ej. `LibroGateway`). Definen **qué** necesita la aplicación guardar o buscar, pero no **cómo**. La aplicación dice *"Necesito buscar un libro"*, pero no sabe nada de SQL.

#### 🟧 3. Capa de Infraestructura (`infrastructure`) - Las Extremidades
Contiene los **Adaptadores** que conectan nuestra aplicación con el mundo exterior.
* **Adaptadores de Entrada (`in.ui`)**: Los controladores de JavaFX (`BibliotecaController`, `LoginController`). Toman los clics del usuario y llaman a los Casos de Uso.
* **Adaptadores de Salida (`out.persistence`)**: Implementan las interfaces (Puertos) definidas en la capa de Aplicación. Aquí están los `JdbcLibroGateway`, que sí conocen de SQL, de JDBC y de la base de datos H2.

#### 🧩 El Ensamblaje (`Main.java`)
El archivo `Main` actúa como el **inyector de dependencias manual**. Crea los Gateways (Infraestructura), se los pasa a los Casos de Uso (Aplicación), y estos se los pasa a los Controladores (UI).

---

## 3. 💽 Tecnologías y Base de Datos (H2 Database)

Para el almacenamiento de los datos utilizamos **H2 Database**. Es un motor de base de datos relacional escrito en Java que se eligió por las siguientes razones:

1. **Base de Datos Embebida (Embedded)**: H2 corre dentro de la misma aplicación (no requiere instalar servidores como MySQL o Postgres). Esto significa que al ejecutar el proyecto, la base de datos se levanta automáticamente.
2. **Persistencia Local**: Los datos se guardan en un archivo local (`mylib.mv.db`), lo que garantiza que la información (usuarios, compras, libros) sobreviva entre reinicios de la aplicación.
3. **Simplicidad para Entornos Educativos**: Permite usar todo el poder de SQL estándar (Consultas, Tablas, Claves Primarias) sin la fricción de configuraciones complejas para los evaluadores.
4. **Auto-inicialización (Data Seeding)**: Al arrancar la aplicación, los Gateways JDBC verifican si las tablas existen. Si no, ejecutan sentencias `CREATE TABLE` y poblan la base de datos con 12 libros de prueba iniciales de forma automática.

---

## 4. 🛠️ Qué se hizo (Funcionalidades Implementadas)

Se implementó un sistema robusto "End-to-End". Todo esto ocurre respetando la arquitectura:

1. **Gestión de Usuarios (Autenticación Segura):**
   - Registro de usuarios con contraseñas encriptadas usando **jBCrypt**.
   - Login con bloqueo automático tras 3 intentos fallidos (prevención de fuerza bruta).
   - Recuperación de cuenta mediante cambio de contraseña.

2. **Catálogo y Motor de Búsqueda Avanzado:**
   - Visualización de libros, portadas, precios y calificaciones.
   - Buscador por título, autor, ISBN, categorías y etiquetas.
   - Filtros de precio y algoritmos de ordenamiento (precio, relevancia, populares).

3. **Módulo de Compras (Shopping Cart & Checkout):**
   - Carrito de compras que persiste en base de datos.
   - Flujo de Checkout tipo "Wizard" (Asistente en 3 pasos: Dirección, Pago, Resumen).
   - Generación automática de **Facturas en PDF** tras la compra utilizando la librería **iText**.

4. **Sistema de Recomendaciones:**
   - **Venta en Frío**: Si el usuario es nuevo, se le recomiendan los libros "Trending" (basado en clics).
   - **Recomendaciones Inteligentes**: Si el usuario tiene compras, se analiza su historial y se le recomiendan libros relacionados a sus intereses.

5. **Subida y Aprobación de Libros:**
   - Módulo para "Publicadores" donde pueden subir sus libros en PDF o EPUB.
   - Quedan en estado `PENDIENTE` hasta que un administrador los pasa a `APROBADO`.

---

## 5. 🧩 Resumen de Patrones de Diseño Aplicados

*(Ver archivo `PATRONES_DE_DISEÑO.md` para el código exacto, pero aquí tienes el resumen para exponer).*

Para que el código fuera elegante y fácil de extender, aplicamos 5 patrones de diseño de la banda de los 4 (GoF) y patrones arquitectónicos de software empresarial:

1. **Strategy (Estrategia)**: Usado en la conexión a la Base de Datos. La capa de aplicación usa interfaces (`LibroGateway`). La infraestructura provee la estrategia concreta (`JdbcLibroGateway`).
2. **Command (Comando)**: Cada acción del usuario es un "Caso de Uso" independiente. Aisla la petición de quien la ejecuta.
3. **Transaction Script**: Dentro de los Casos de Uso complejos (como el Login o el Pago), la lógica de la transacción completa está escrita de arriba a abajo secuencialmente.
4. **Prototype (Prototipo)**: Para crear entidades (como libros nuevos o datos de prueba) se usan objetos plantilla con valores predeterminados.
5. **Estado (State)**: El comportamiento cambia según estados internos. Ejemplo: Un usuario pasa de `ACTIVO` a `BLOQUEADO` alterando si puede hacer login. Los libros pasan de `PENDIENTE` a `APROBADO` alterando si son visibles en la tienda.

---

## 6. 🎯 Conclusión para la Exposición

> *"Este proyecto demuestra que desarrollar software no se trata solo de hacer pantallas bonitas o escribir queries SQL, sino de **diseñar sistemas sostenibles**. Gracias a la **Clean Architecture**, tenemos un sistema donde las reglas de negocio están totalmente blindadas. Si mañana la Universidad nos pide cambiar de aplicación de escritorio a una aplicación Web, o cambiar de la base de datos H2 a Oracle, **nuestra lógica de negocio (el 70% del código) no tendría que modificarse en absoluto**. Esto, sumado al uso de patrones de diseño, hace que OpenLibMarket sea un software verdaderamente profesional y escalable."*
