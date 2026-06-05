package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.domain.CarritoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcCarritoGatewayTest {
    private JdbcLibroGateway libroGateway;
    private JdbcCarritoGateway carritoGateway;
    private final String dbUrl = "jdbc:h2:mem:test_carrito;DB_CLOSE_DELAY=-1";
    private final String testSessionId = "SESSION_TEST_123";

    @BeforeEach
    public void setUp() {
        // Inicializa la base de datos de libros con datos dummy
        libroGateway = new JdbcLibroGateway(dbUrl);
        // Inicializa la base de datos del carrito
        carritoGateway = new JdbcCarritoGateway(dbUrl);
        carritoGateway.limpiarCarrito(testSessionId);
    }

    @Test
    public void testAgregarYObtenerItem() {
        carritoGateway.agregarItem(testSessionId, 1L, 2);
        
        List<CarritoItem> items = carritoGateway.obtenerContenidoCarrito(testSessionId);
        
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getCantidad());
        assertEquals(1L, items.get(0).getLibro().getId());
    }

    @Test
    public void testAgregarMismoItemIncrementaCantidad() {
        carritoGateway.agregarItem(testSessionId, 2L, 1);
        carritoGateway.agregarItem(testSessionId, 2L, 3);
        
        List<CarritoItem> items = carritoGateway.obtenerContenidoCarrito(testSessionId);
        
        assertEquals(1, items.size());
        assertEquals(4, items.get(0).getCantidad()); // 1 + 3 = 4
    }

    @Test
    public void testEliminarItem() {
        carritoGateway.agregarItem(testSessionId, 3L, 1);
        carritoGateway.eliminarItem(testSessionId, 3L);
        
        List<CarritoItem> items = carritoGateway.obtenerContenidoCarrito(testSessionId);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testCalcularSubtotal() {
        carritoGateway.agregarItem(testSessionId, 1L, 2); // 1 = Clean Code (precio 45.99)
        List<CarritoItem> items = carritoGateway.obtenerContenidoCarrito(testSessionId);
        
        assertEquals(1, items.size());
        CarritoItem item = items.get(0);
        
        assertEquals(45.99 * 2, item.getSubtotal(), 0.01);
    }
}
