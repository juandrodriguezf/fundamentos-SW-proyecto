package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.domain.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcLibroGatewayTest {

    private JdbcLibroGateway gateway;
    private final String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";

    @BeforeEach
    void setUp() throws Exception {
        // Limpiar base de datos en memoria antes de cada prueba
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS libros");
        }
        
        gateway = new JdbcLibroGateway(url);
    }

    @Test
    void testBuscarPorTitulo() {
        List<Libro> libros = gateway.buscarPorTitulo("clean code");
        assertFalse(libros.isEmpty(), "Debería encontrar 'Clean Code'");
        assertEquals("Clean Code", libros.get(0).getTitulo());
    }

    @Test
    void testObtenerTendencias() {
        List<Libro> tendencias = gateway.obtenerTendencias(3);
        assertEquals(3, tendencias.size(), "Debería retornar 3 tendencias");
        // El de mayor clics en los datos de prueba es "Cien Anos de Soledad" con 200
        assertEquals("Cien Anos de Soledad", tendencias.get(0).getTitulo());
    }

    @Test
    void testIncrementarClics() {
        List<Libro> librosInicial = gateway.buscarPorTitulo("Refactoring");
        Libro libro = librosInicial.get(0);
        int clicsIniciales = libro.getClics();

        gateway.incrementarClics(libro.getId());

        List<Libro> librosActualizados = gateway.buscarPorTitulo("Refactoring");
        assertEquals(clicsIniciales + 1, librosActualizados.get(0).getClics(), "Debería haber incrementado los clics en 1");
    }
}
