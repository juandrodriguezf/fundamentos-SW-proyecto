package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.domain.Usuario;
import co.edu.javeriana.proyecto.domain.exception.UsuarioYaExisteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class JdbcUsuarioGatewayTest {

    private JdbcUsuarioGateway gateway;
    private final String dbUrl = "jdbc:h2:mem:test_usuarios;DB_CLOSE_DELAY=-1";

    @BeforeEach
    void setUp() {
        gateway = new JdbcUsuarioGateway(dbUrl);
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void guardarUsuario_DeberiaInsertarEnBaseDeDatosYAsignarId() {
        Usuario usuario = new Usuario(null, "test@test.com", "hashed123", "Test User", true);
        
        assertDoesNotThrow(() -> gateway.guardar(usuario));
        
        assertNotNull(usuario.getId(), "El ID debe haber sido generado por la base de datos");
        assertTrue(usuario.getId() > 0);
    }

    @Test
    void existeEmail_DeberiaRetornarTrueSiExiste() {
        Usuario usuario = new Usuario(null, "existe@test.com", "hash", "User", true);
        gateway.guardar(usuario);
        
        assertTrue(gateway.existeEmail("existe@test.com"));
    }

    @Test
    void existeEmail_DeberiaRetornarFalseSiNoExiste() {
        assertFalse(gateway.existeEmail("noexiste@test.com"));
    }
}
