package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.application.port.out.AdminGateway;
import co.edu.javeriana.proyecto.domain.Libro;
import co.edu.javeriana.proyecto.domain.MetricasAdmin;

import java.sql.*;
import java.util.*;

public class JdbcAdminGateway implements AdminGateway {

    private final String url;

    public JdbcAdminGateway(String url) {
        this.url = url;
    }

    @Override
    public MetricasAdmin obtenerMetricas() {
        MetricasAdmin m = new MetricasAdmin();
        try (Connection conn = DriverManager.getConnection(url)) {
            // Usuarios
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios");
                if (rs.next()) m.setTotalUsuarios(rs.getInt(1));
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios WHERE activo = TRUE");
                if (rs.next()) m.setUsuariosActivos(rs.getInt(1));
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios WHERE activo = FALSE");
                if (rs.next()) m.setUsuariosBloqueados(rs.getInt(1));
                rs.close();

                // Libros
                rs = stmt.executeQuery("SELECT COUNT(*) FROM libros");
                if (rs.next()) m.setTotalLibros(rs.getInt(1));
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM libros WHERE estado = 'PENDIENTE'");
                if (rs.next()) m.setLibrosPendientes(rs.getInt(1));
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM libros WHERE estado = 'APROBADO'");
                if (rs.next()) m.setLibrosAprobados(rs.getInt(1));
                rs.close();

                // Ingresos y órdenes (tabla ordenes / compras)
                try {
                    rs = stmt.executeQuery("SELECT COUNT(*), COALESCE(SUM(monto), 0) FROM ordenes");
                    if (rs.next()) {
                        m.setTotalOrdenes(rs.getInt(1));
                        m.setIngresosTotales(rs.getDouble(2));
                    }
                    rs.close();
                } catch (SQLException ignore) {
                    // La tabla puede tener otro nombre; intentar con compras
                    try {
                        rs = stmt.executeQuery("SELECT COUNT(*), COALESCE(SUM(precio_unitario * cantidad), 0) FROM compras");
                        if (rs.next()) {
                            m.setTotalOrdenes(rs.getInt(1));
                            m.setIngresosTotales(rs.getDouble(2));
                        }
                        rs.close();
                    } catch (SQLException ignored2) {}
                }

                // Top libros por clics
                List<Libro> topLibros = new ArrayList<>();
                rs = stmt.executeQuery(
                    "SELECT id, titulo, autor, isbn, categoria, etiquetas, clics, precio, " +
                    "portada, stock, calificacion_promedio, estado, ruta_archivo " +
                    "FROM libros ORDER BY clics DESC LIMIT 5"
                );
                while (rs.next()) {
                    topLibros.add(extraerLibro(rs));
                }
                rs.close();
                m.setTopLibros(topLibros);

                // Distribución por categoría
                Map<String, Integer> porCategoria = new LinkedHashMap<>();
                rs = stmt.executeQuery(
                    "SELECT categoria, COUNT(*) AS cnt FROM libros " +
                    "GROUP BY categoria ORDER BY cnt DESC"
                );
                while (rs.next()) {
                    porCategoria.put(rs.getString("categoria"), rs.getInt("cnt"));
                }
                rs.close();
                m.setLibrosPorCategoria(porCategoria);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return m;
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
