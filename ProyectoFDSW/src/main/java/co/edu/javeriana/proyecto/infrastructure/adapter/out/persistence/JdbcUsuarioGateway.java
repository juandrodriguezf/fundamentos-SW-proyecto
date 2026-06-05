package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUsuarioGateway implements UsuarioGateway {
    private final String url;

    public JdbcUsuarioGateway(String url) {
        this.url = url;
        inicializarBaseDeDatos();
    }

    private void inicializarBaseDeDatos() {
        String sqlTabla = "CREATE TABLE IF NOT EXISTS usuarios (" +
                          "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                          "email VARCHAR(255) NOT NULL UNIQUE, " +
                          "password_hash VARCHAR(255) NOT NULL, " +
                          "nombre VARCHAR(255) NOT NULL, " +
                          "activo BOOLEAN DEFAULT TRUE, " +
                          "intentos_fallidos INT DEFAULT 0" +
                          ")";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlTabla);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email)");

            // Migración segura: agregar columna si no existe (por compatibilidad con BD antigua)
            try {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS intentos_fallidos INT DEFAULT 0");
            } catch (SQLException ignored) {
                // Ya existe, se ignora
            }

            // Seed: usuario admin hardcodeado (admin@openlib.com / Admin123!)
            String checkAdmin = "SELECT COUNT(*) FROM usuarios WHERE email = 'admin@openlib.com'";
            try (ResultSet rs = stmt.executeQuery(checkAdmin)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String adminHash = org.mindrot.jbcrypt.BCrypt.hashpw("Admin123!", org.mindrot.jbcrypt.BCrypt.gensalt(10));
                    stmt.execute(
                        "INSERT INTO usuarios (email, password_hash, nombre, activo, intentos_fallidos) VALUES (" +
                        "'admin@openlib.com', '" + adminHash + "', 'Administrador', TRUE, 0)"
                    );
                    System.out.println("[SISTEMA] Usuario admin creado: admin@openlib.com / Admin123!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar la base de datos de usuarios", e);
        }
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (email, password_hash, nombre, activo, intentos_fallidos) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getEmail());
            pstmt.setString(2, usuario.getPasswordHash());
            pstmt.setString(3, usuario.getNombre());
            pstmt.setBoolean(4, usuario.isActivo());
            pstmt.setInt(5, usuario.getIntentosFallidos());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar el usuario en la base de datos.", e);
        }
    }

    @Override
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT id, email, password_hash, nombre, activo, intentos_fallidos FROM usuarios WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("nombre"),
                        rs.getBoolean("activo"),
                        rs.getInt("intentos_fallidos")
                    );
                    return Optional.of(usuario);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void actualizarIntentosFallidos(String email, int intentos) {
        String sql = "UPDATE usuarios SET intentos_fallidos = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, intentos);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar intentos fallidos.", e);
        }
    }

    @Override
    public void actualizarActivo(String email, boolean activo) {
        String sql = "UPDATE usuarios SET activo = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, activo);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar estado activo del usuario.", e);
        }
    }

    @Override
    public void actualizarPassword(String email, String nuevoPasswordHash) {
        String sql = "UPDATE usuarios SET password_hash = ?, intentos_fallidos = 0, activo = TRUE WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoPasswordHash);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar la contraseña.", e);
        }
    }
    @Override
    public List<Usuario> obtenerTodos() {
        List<Usuario> lista = new java.util.ArrayList<>();
        String sql = "SELECT id, email, password_hash, nombre, activo, intentos_fallidos FROM usuarios ORDER BY id";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("nombre"),
                    rs.getBoolean("activo"),
                    rs.getInt("intentos_fallidos")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, email = ?, activo = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setBoolean(3, usuario.isActivo());
            pstmt.setLong(4, usuario.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }
}
