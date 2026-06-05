package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.application.port.out.CompraGateway;
import co.edu.javeriana.proyecto.domain.Compra;
import co.edu.javeriana.proyecto.domain.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCompraGateway implements CompraGateway {
    private final String url;

    public JdbcCompraGateway(String url) {
        this.url = url;
        inicializarBaseDeDatos();
    }

    private void inicializarBaseDeDatos() {
        String sql = "CREATE TABLE IF NOT EXISTS compras (" +
                     "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                     "usuario_id BIGINT NOT NULL, " +
                     "libro_id BIGINT NOT NULL, " +
                     "cantidad INT DEFAULT 1, " +
                     "precio_unitario DOUBLE DEFAULT 0.0, " +
                     "orden_id VARCHAR(50) NOT NULL, " +
                     "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (libro_id) REFERENCES libros(id)" +
                     ")";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar la tabla de compras", e);
        }
    }

    @Override
    public void registrarCompra(Compra compra) {
        String sql = "INSERT INTO compras (usuario_id, libro_id, cantidad, precio_unitario, orden_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, compra.getUsuarioId());
            pstmt.setLong(2, compra.getLibroId());
            pstmt.setInt(3, compra.getCantidad());
            pstmt.setDouble(4, compra.getPrecioUnitario());
            pstmt.setString(5, compra.getOrdenId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Compra> obtenerHistorial(Long usuarioId) {
        List<Compra> historial = new ArrayList<>();
        String sql = "SELECT c.id, c.usuario_id, c.libro_id, c.cantidad, c.precio_unitario, c.orden_id, c.fecha, " +
                     "l.titulo, l.autor, l.isbn, l.categoria, l.etiquetas, l.clics, l.precio, l.portada, l.stock, l.calificacion_promedio " +
                     "FROM compras c JOIN libros l ON c.libro_id = l.id " +
                     "WHERE c.usuario_id = ? ORDER BY c.fecha DESC";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Compra compra = new Compra(
                        rs.getLong("id"), rs.getLong("usuario_id"), rs.getLong("libro_id"),
                        rs.getInt("cantidad"), rs.getDouble("precio_unitario"),
                        rs.getString("orden_id"), rs.getTimestamp("fecha").toLocalDateTime()
                    );
                    compra.setLibro(extraerLibro(rs));
                    historial.add(compra);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historial;
    }

    /**
     * Recomendaciones basadas en historial:
     * Busca libros de las mismas categorias que el usuario ha comprado,
     * excluyendo los que ya compro.
     */
    @Override
    public List<Libro> recomendarPorHistorial(Long usuarioId, int limite) {
        List<Libro> recomendaciones = new ArrayList<>();
        String sql = "SELECT DISTINCT l.* FROM libros l " +
                     "WHERE l.categoria IN (" +
                     "  SELECT DISTINCT l2.categoria FROM compras c2 " +
                     "  JOIN libros l2 ON c2.libro_id = l2.id " +
                     "  WHERE c2.usuario_id = ?" +
                     ") " +
                     "AND l.id NOT IN (" +
                     "  SELECT libro_id FROM compras WHERE usuario_id = ?" +
                     ") " +
                     "ORDER BY l.calificacion_promedio DESC, l.clics DESC " +
                     "LIMIT ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, usuarioId);
            pstmt.setLong(2, usuarioId);
            pstmt.setInt(3, limite);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recomendaciones.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recomendaciones;
    }

    /**
     * Filtrado colaborativo simple:
     * "Usuarios que compraron este libro tambien compraron..."
     */
    @Override
    public List<Libro> recomendarPorLibro(Long libroId, int limite) {
        List<Libro> recomendaciones = new ArrayList<>();
        String sql = "SELECT l.*, COUNT(*) as coincidencias FROM libros l " +
                     "JOIN compras c ON l.id = c.libro_id " +
                     "WHERE c.usuario_id IN (" +
                     "  SELECT usuario_id FROM compras WHERE libro_id = ?" +
                     ") " +
                     "AND l.id != ? " +
                     "GROUP BY l.id " +
                     "ORDER BY coincidencias DESC, l.calificacion_promedio DESC " +
                     "LIMIT ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, libroId);
            pstmt.setLong(2, libroId);
            pstmt.setInt(3, limite);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recomendaciones.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recomendaciones;
    }

    /**
     * Fallback para usuarios nuevos (venta en frio):
     * Retorna los mejor calificados.
     */
    @Override
    public List<Libro> recomendarFallback(int limite) {
        List<Libro> recomendaciones = new ArrayList<>();
        String sql = "SELECT * FROM libros ORDER BY calificacion_promedio DESC, clics DESC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recomendaciones.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recomendaciones;
    }

    @Override
    public List<Libro> obtenerLibrosComprados(Long usuarioId) {
        List<Libro> biblioteca = new ArrayList<>();
        String sql = "SELECT DISTINCT l.* FROM libros l " +
                     "JOIN compras c ON l.id = c.libro_id " +
                     "WHERE c.usuario_id = ? " +
                     "ORDER BY l.titulo ASC";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    biblioteca.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return biblioteca;
    }

    private Libro extraerLibro(ResultSet rs) throws SQLException {
        String estado;
        String rutaArchivo;
        try {
            estado = rs.getString("estado");
            rutaArchivo = rs.getString("ruta_archivo");
        } catch (SQLException e) {
            estado = "APROBADO";
            rutaArchivo = "";
        }
        return new Libro(
            rs.getLong("id"), rs.getString("titulo"), rs.getString("autor"),
            rs.getString("isbn"), rs.getString("categoria"), rs.getString("etiquetas"),
            rs.getInt("clics"), rs.getDouble("precio"), rs.getString("portada"),
            rs.getInt("stock"), rs.getDouble("calificacion_promedio"),
            estado, rutaArchivo
        );
    }
}
