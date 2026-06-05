package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.application.port.out.EtiquetaGateway;
import co.edu.javeriana.proyecto.domain.Etiqueta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcEtiquetaGateway implements EtiquetaGateway {

    private final String url;

    public JdbcEtiquetaGateway(String url) {
        this.url = url;
        inicializarTabla();
    }

    private void inicializarTabla() {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS etiquetas (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "nombre VARCHAR(100) NOT NULL UNIQUE)"
            );
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar tabla etiquetas", e);
        }
    }

    @Override
    public List<Etiqueta> obtenerTodas() {
        List<Etiqueta> lista = new ArrayList<>();
        // El conteo escanea el CSV de libros.etiquetas para las etiquetas de la BD
        String sql = "SELECT e.id, e.nombre, " +
                     "(SELECT COUNT(*) FROM libros l WHERE LOWER(l.etiquetas) LIKE CONCAT('%', LOWER(e.nombre), '%')) AS total_libros " +
                     "FROM etiquetas e ORDER BY e.nombre";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Etiqueta e = new Etiqueta(rs.getLong("id"), rs.getString("nombre"));
                e.setTotalLibros(rs.getInt("total_libros"));
                lista.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public void guardar(Etiqueta etiqueta) {
        String sql = "INSERT INTO etiquetas (nombre) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, etiqueta.getNombre());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) etiqueta.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar etiqueta: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Etiqueta etiqueta) {
        String sql = "UPDATE etiquetas SET nombre = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, etiqueta.getNombre());
            pstmt.setLong(2, etiqueta.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar etiqueta: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "DELETE FROM etiquetas WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar etiqueta: " + e.getMessage(), e);
        }
    }
}
