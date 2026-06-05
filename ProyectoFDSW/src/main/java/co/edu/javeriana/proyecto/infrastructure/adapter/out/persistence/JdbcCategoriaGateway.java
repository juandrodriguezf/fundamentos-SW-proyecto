package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.application.port.out.CategoriaGateway;
import co.edu.javeriana.proyecto.domain.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCategoriaGateway implements CategoriaGateway {

    private final String url;

    public JdbcCategoriaGateway(String url) {
        this.url = url;
        inicializarTabla();
    }

    private void inicializarTabla() {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // 1. Crear tabla categorias
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS categorias (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "nombre VARCHAR(100) NOT NULL UNIQUE, " +
                "descripcion VARCHAR(500) DEFAULT '')"
            );

            // 2. Migración: poblar categorías desde libros existentes
            stmt.execute(
                "INSERT INTO categorias (nombre, descripcion) " +
                "SELECT DISTINCT categoria, '' FROM libros " +
                "WHERE categoria IS NOT NULL AND categoria <> '' " +
                "AND categoria NOT IN (SELECT nombre FROM categorias)"
            );

            // 3. Agregar columna categoria_id a libros (si no existe)
            try {
                stmt.execute("ALTER TABLE libros ADD COLUMN IF NOT EXISTS categoria_id BIGINT");
            } catch (SQLException ignore) {}

            // 4. Actualizar categoria_id en libros desde el nombre de la categoria
            stmt.execute(
                "UPDATE libros SET categoria_id = " +
                "(SELECT id FROM categorias WHERE nombre = libros.categoria) " +
                "WHERE categoria_id IS NULL AND categoria IS NOT NULL"
            );

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar tabla categorias", e);
        }
    }

    @Override
    public List<Categoria> obtenerTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql =
            "SELECT c.id, c.nombre, c.descripcion, " +
            "COUNT(l.id) AS total_libros " +
            "FROM categorias c " +
            "LEFT JOIN libros l ON LOWER(l.categoria) = LOWER(c.nombre) " +
            "GROUP BY c.id, c.nombre, c.descripcion " +
            "ORDER BY c.nombre";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Categoria c = new Categoria(
                    rs.getLong("id"),
                    rs.getString("nombre"),
                    rs.getString("descripcion")
                );
                c.setTotalLibros(rs.getInt("total_libros"));
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public void guardar(Categoria categoria) {
        String sql = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, categoria.getNombre());
            pstmt.setString(2, categoria.getDescripcion() != null ? categoria.getDescripcion() : "");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) categoria.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Categoria categoria) {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoria.getNombre());
            pstmt.setString(2, categoria.getDescripcion() != null ? categoria.getDescripcion() : "");
            pstmt.setLong(3, categoria.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar categoría: " + e.getMessage(), e);
        }
    }
}
