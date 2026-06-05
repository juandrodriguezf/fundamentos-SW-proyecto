package co.edu.javeriana.proyecto.domain;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CarritoItemTest {

    @Test
    public void testSumaTotalCarritoConDecimales() {
        // Arrange
        Libro libro1 = new Libro(1L, "Libro 1", "Autor", "", "", "", 0, 10.99, "", 10, 0);
        Libro libro2 = new Libro(2L, "Libro 2", "Autor", "", "", "", 0, 5.50, "", 10, 0);
        Libro libro3 = new Libro(3L, "Libro 3", "Autor", "", "", "", 0, 0.10, "", 10, 0);

        CarritoItem item1 = new CarritoItem(libro1, 2); // 10.99 * 2 = 21.98
        CarritoItem item2 = new CarritoItem(libro2, 1); // 5.50 * 1 = 5.50
        CarritoItem item3 = new CarritoItem(libro3, 3); // 0.10 * 3 = 0.30

        List<CarritoItem> carrito = Arrays.asList(item1, item2, item3);

        // Act
        double total = carrito.stream()
                .mapToDouble(CarritoItem::getSubtotal)
                .sum();
        
        // Assert: 21.98 + 5.50 + 0.30 = 27.78
        // Using delta for double comparison to avoid floating point precision issues
        assertEquals(27.78, total, 0.001, "La suma total del carrito debe ser exacta manejando decimales");
    }
}
