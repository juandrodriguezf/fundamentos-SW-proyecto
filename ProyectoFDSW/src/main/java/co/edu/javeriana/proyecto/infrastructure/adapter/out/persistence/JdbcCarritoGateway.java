package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.application.port.out.CarritoGateway;
import co.edu.javeriana.proyecto.domain.CarritoItem;
import co.edu.javeriana.proyecto.domain.Libro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcCarritoGateway implements CarritoGateway {
    private final String url;

    public JdbcCarritoGateway(String url) {
        this.url = url;
        inicializarBaseDeDatos();
    }

    private void inicializarBaseDeDatos() {
        String sql = "CREATE TABLE IF NOT EXISTS carrito_items (" +
                     "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                     "session_id VARCHAR(255) NOT NULL, " +
                     "libro_id BIGINT NOT NULL, " +
                     "cantidad INT DEFAULT 1, " +
                     "UNIQUE(session_id, libro_id), " +
                     "FOREIGN KEY (libro_id) REFERENCES libros(id)" +
                     ")";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar la base de datos de carrito", e);
        }
    }

    @Override
    public void agregarItem(String sessionId, Long libroId, int cantidad) {
        try (Connection conn = DriverManager.getConnection(url)) {
            String updateSql = "UPDATE carrito_items SET cantidad = cantidad + ? WHERE session_id = ? AND libro_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, cantidad);
                updateStmt.setString(2, sessionId);
                updateStmt.setLong(3, libroId);
                int rows = updateStmt.executeUpdate();
                
                if (rows == 0) {
                    String insertSql = "INSERT INTO carrito_items (session_id, libro_id, cantidad) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, sessionId);
                        insertStmt.setLong(2, libroId);
                        insertStmt.setInt(3, cantidad);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void eliminarItem(String sessionId, Long libroId) {
        String sql = "DELETE FROM carrito_items WHERE session_id = ? AND libro_id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.setLong(2, libroId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<CarritoItem> obtenerContenidoCarrito(String sessionId) {
        List<CarritoItem> items = new ArrayList<>();
        String sql = "SELECT c.cantidad, l.id, l.titulo, l.autor, l.isbn, l.categoria, l.etiquetas, l.clics, l.precio, l.portada, l.stock, l.calificacion_promedio " +
                     "FROM carrito_items c JOIN libros l ON c.libro_id = l.id " +
                     "WHERE c.session_id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Libro libro = new Libro(
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
                        "APROBADO",
                        ""
                    );
                    items.add(new CarritoItem(libro, rs.getInt("cantidad")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public void limpiarCarrito(String sessionId) {
        String sql = "DELETE FROM carrito_items WHERE session_id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
