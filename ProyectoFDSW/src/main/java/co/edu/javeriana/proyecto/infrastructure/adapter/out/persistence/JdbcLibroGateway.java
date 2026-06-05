package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.domain.Libro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcLibroGateway implements LibroGateway {
    private final String url;

    public JdbcLibroGateway(String url) {
        this.url = url;
        inicializarBaseDeDatos();
    }

    private void inicializarBaseDeDatos() {
        String sql = "CREATE TABLE IF NOT EXISTS libros (" +
                     "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                     "titulo VARCHAR(255) NOT NULL, " +
                     "autor VARCHAR(255) NOT NULL, " +
                     "isbn VARCHAR(20) DEFAULT '', " +
                     "categoria VARCHAR(100) DEFAULT 'General', " +
                     "etiquetas VARCHAR(500) DEFAULT '', " +
                     "clics INT DEFAULT 0, " +
                     "precio DOUBLE DEFAULT 0.0, " +
                     "portada VARCHAR(255) DEFAULT '', " +
                     "stock INT DEFAULT 10, " +
                     "calificacion_promedio DOUBLE DEFAULT 0.0, " +
                     "estado VARCHAR(20) DEFAULT 'APROBADO', " +
                     "ruta_archivo VARCHAR(500) DEFAULT ''" +
                     ")";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            try {
                stmt.execute("ALTER TABLE libros ADD COLUMN estado VARCHAR(20) DEFAULT 'APROBADO'");
                stmt.execute("ALTER TABLE libros ADD COLUMN ruta_archivo VARCHAR(500) DEFAULT ''");
            } catch (SQLException ignore) {}
            insertarDatosDePrueba(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar la base de datos", e);
        }
    }

    private void insertarDatosDePrueba(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM libros";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO libros (titulo, autor, isbn, categoria, etiquetas, clics, precio, portada, stock, calificacion_promedio, estado, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'APROBADO', '')";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    Object[][] libros = {
                        {"Clean Code", "Robert C. Martin", "978-0132350884", "Ingenieria de Software", "programacion,buenas practicas,codigo limpio", 150, 45.99, "https://covers.openlibrary.org/b/isbn/9780132350884-M.jpg", 10, 4.7},
                        {"The Pragmatic Programmer", "Andrew Hunt", "978-0135957059", "Ingenieria de Software", "programacion,productividad,carrera", 120, 39.50, "https://covers.openlibrary.org/b/isbn/9780135957059-M.jpg", 5, 4.8},
                        {"Design Patterns", "Erich Gamma", "978-0201633610", "Arquitectura", "patrones,diseño,orientado a objetos", 90, 50.00, "https://covers.openlibrary.org/b/isbn/9780201633610-M.jpg", 8, 4.5},
                        {"Refactoring", "Martin Fowler", "978-0134757599", "Ingenieria de Software", "refactorizacion,codigo,mejora continua", 80, 42.00, "https://covers.openlibrary.org/b/isbn/9780134757599-M.jpg", 12, 4.6},
                        {"Domain-Driven Design", "Eric Evans", "978-0321125217", "Arquitectura", "DDD,dominio,modelado", 110, 55.00, "https://covers.openlibrary.org/b/isbn/9780321125217-M.jpg", 3, 4.9},
                        {"Effective Java", "Joshua Bloch", "978-0134685991", "Programacion", "java,buenas practicas,rendimiento", 95, 48.00, "https://covers.openlibrary.org/b/isbn/9780134685991-M.jpg", 7, 4.8},
                        {"Head First Java", "Kathy Sierra", "978-0596009205", "Programacion", "java,principiantes,aprendizaje", 70, 35.00, "https://covers.openlibrary.org/b/isbn/9780596009205-M.jpg", 15, 4.3},
                        {"Introduction to Algorithms", "Thomas Cormen", "978-0262033848", "Ciencias de la Computacion", "algoritmos,estructuras de datos,matematicas", 60, 75.00, "https://covers.openlibrary.org/b/isbn/9780262033848-M.jpg", 4, 4.7},
                        {"Cien Anos de Soledad", "Gabriel Garcia Marquez", "978-0307474728", "Literatura", "realismo magico,clasico,latinoamerica", 200, 25.00, "https://covers.openlibrary.org/b/isbn/9780307474728-M.jpg", 20, 4.9},
                        {"El Principito", "Antoine de Saint-Exupery", "978-0156012195", "Literatura", "fabula,clasico,infantil", 180, 15.00, "https://covers.openlibrary.org/b/isbn/9780156012195-M.jpg", 25, 4.8},
                        {"1984", "George Orwell", "978-0451524935", "Literatura", "distopia,clasico,politica", 160, 20.00, "https://covers.openlibrary.org/b/isbn/9780451524935-M.jpg", 18, 4.7},
                        {"Sapiens", "Yuval Noah Harari", "978-0062316097", "Historia", "humanidad,historia,ciencia", 140, 30.00, "https://covers.openlibrary.org/b/isbn/9780062316097-M.jpg", 10, 4.6}
                    };
                    for (Object[] libro : libros) {
                        pstmt.setString(1, (String) libro[0]);
                        pstmt.setString(2, (String) libro[1]);
                        pstmt.setString(3, (String) libro[2]);
                        pstmt.setString(4, (String) libro[3]);
                        pstmt.setString(5, (String) libro[4]);
                        pstmt.setInt(6, (Integer) libro[5]);
                        pstmt.setDouble(7, (Double) libro[6]);
                        pstmt.setString(8, (String) libro[7]);
                        pstmt.setInt(9, (Integer) libro[8]);
                        pstmt.setDouble(10, (Double) libro[9]);
                        pstmt.executeUpdate();
                    }
                }
            }
        }
    }

    @Override
    public List<Libro> buscarPorTitulo(String filtro) {
        List<Libro> libros = new ArrayList<>();
        String sql = "SELECT * FROM libros WHERE estado = 'APROBADO' AND (LOWER(titulo) LIKE ? OR LOWER(autor) LIKE ? OR LOWER(isbn) LIKE ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String like = "%" + filtro.toLowerCase() + "%";
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            pstmt.setString(3, like);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    libros.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return libros;
    }

    @Override
    public List<Libro> buscarAvanzado(String texto, String categoria, double precioMin, double precioMax, String ordenamiento) {
        List<Libro> libros = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM libros WHERE estado = 'APROBADO'");
        List<Object> params = new ArrayList<>();

        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (LOWER(titulo) LIKE ? OR LOWER(autor) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(etiquetas) LIKE ?)");
            String like = "%" + texto.toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (categoria != null && !categoria.trim().isEmpty() && !categoria.equals("Todas")) {
            sql.append(" AND LOWER(categoria) = ?");
            params.add(categoria.toLowerCase());
        }

        if (precioMin >= 0) {
            sql.append(" AND precio >= ?");
            params.add(precioMin);
        }

        if (precioMax > 0) {
            sql.append(" AND precio <= ?");
            params.add(precioMax);
        }

        // Ordenamiento
        switch (ordenamiento != null ? ordenamiento : "relevancia") {
            case "precio_asc":
                sql.append(" ORDER BY precio ASC");
                break;
            case "precio_desc":
                sql.append(" ORDER BY precio DESC");
                break;
            case "mejor_calificados":
                sql.append(" ORDER BY calificacion_promedio DESC");
                break;
            case "mas_populares":
                sql.append(" ORDER BY clics DESC");
                break;
            default: // relevancia
                sql.append(" ORDER BY clics DESC, calificacion_promedio DESC");
                break;
        }

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) param);
                }
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    libros.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return libros;
    }

    @Override
    public java.util.Optional<Libro> buscarPorId(Long id) {
        String sql = "SELECT * FROM libros WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return java.util.Optional.of(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return java.util.Optional.empty();
    }

    @Override
    public List<Libro> obtenerTendencias(int limite) {
        List<Libro> libros = new ArrayList<>();
        String sql = "SELECT * FROM libros WHERE estado = 'APROBADO' ORDER BY clics DESC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    libros.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return libros;
    }

    @Override
    public List<String> obtenerCategorias() {
        List<String> categorias = new ArrayList<>();
        String sql = "SELECT DISTINCT categoria FROM libros ORDER BY categoria";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categorias.add(rs.getString("categoria"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias;
    }

    @Override
    public List<String> obtenerEtiquetas() {
        List<String> todasEtiquetas = new ArrayList<>();
        String sql = "SELECT DISTINCT etiquetas FROM libros";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String etiquetas = rs.getString("etiquetas");
                if (etiquetas != null && !etiquetas.isEmpty()) {
                    for (String tag : etiquetas.split(",")) {
                        String trimmed = tag.trim();
                        if (!trimmed.isEmpty() && !todasEtiquetas.contains(trimmed)) {
                            todasEtiquetas.add(trimmed);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        java.util.Collections.sort(todasEtiquetas);
        return todasEtiquetas;
    }

    @Override
    public void incrementarClics(Long libroId) {
        String sql = "UPDATE libros SET clics = clics + 1 WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, libroId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void guardar(Libro libro) {
        String sql = "INSERT INTO libros (titulo, autor, isbn, categoria, etiquetas, clics, precio, portada, stock, calificacion_promedio, estado, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, libro.getTitulo());
            pstmt.setString(2, libro.getAutor());
            pstmt.setString(3, libro.getIsbn() != null ? libro.getIsbn() : "");
            pstmt.setString(4, libro.getCategoria());
            pstmt.setString(5, libro.getEtiquetas() != null ? libro.getEtiquetas() : "");
            pstmt.setInt(6, libro.getClics());
            pstmt.setDouble(7, libro.getPrecio());
            pstmt.setString(8, libro.getPortada() != null ? libro.getPortada() : "");
            pstmt.setInt(9, libro.getStock());
            pstmt.setDouble(10, libro.getCalificacionPromedio());
            pstmt.setString(11, libro.getEstado() != null ? libro.getEstado() : "PENDIENTE");
            pstmt.setString(12, libro.getRutaArchivo() != null ? libro.getRutaArchivo() : "");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    libro.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Libro> obtenerTodos() {
        List<Libro> libros = new ArrayList<>();
        String sql = "SELECT * FROM libros ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                libros.add(extraerLibro(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return libros;
    }

    @Override
    public void actualizar(Libro libro) {
        String sql = "UPDATE libros SET titulo=?, autor=?, isbn=?, categoria=?, etiquetas=?, " +
                     "precio=?, portada=?, stock=?, calificacion_promedio=?, estado=?, ruta_archivo=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, libro.getTitulo());
            pstmt.setString(2, libro.getAutor());
            pstmt.setString(3, libro.getIsbn() != null ? libro.getIsbn() : "");
            pstmt.setString(4, libro.getCategoria());
            pstmt.setString(5, libro.getEtiquetas() != null ? libro.getEtiquetas() : "");
            pstmt.setDouble(6, libro.getPrecio());
            pstmt.setString(7, libro.getPortada() != null ? libro.getPortada() : "");
            pstmt.setInt(8, libro.getStock());
            pstmt.setDouble(9, libro.getCalificacionPromedio());
            pstmt.setString(10, libro.getEstado() != null ? libro.getEstado() : "APROBADO");
            pstmt.setString(11, libro.getRutaArchivo() != null ? libro.getRutaArchivo() : "");
            pstmt.setLong(12, libro.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar libro: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long libroId) {
        String sql = "DELETE FROM libros WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, libroId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar libro: " + e.getMessage(), e);
        }
    }

    @Override
    public java.util.Map<String, Integer> librosPorCategoria() {
        java.util.Map<String, Integer> mapa = new java.util.LinkedHashMap<>();
        String sql = "SELECT categoria, COUNT(*) AS cnt FROM libros GROUP BY categoria ORDER BY cnt DESC";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mapa.put(rs.getString("categoria"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mapa;
    }

    @Override
    public List<Libro> obtenerConProblemas() {
        List<Libro> libros = new ArrayList<>();
        String sql = "SELECT * FROM libros WHERE " +
                     "(portada IS NULL OR portada = '') OR " +
                     "(isbn IS NULL OR isbn = '') OR " +
                     "precio = 0 " +
                     "ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                libros.add(extraerLibro(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return libros;
    }

    @Override
    public void actualizarEstado(Long libroId, String estado) {
        String sql = "UPDATE libros SET estado = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            pstmt.setLong(2, libroId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
}
