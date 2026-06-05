package co.edu.javeriana.proyecto.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LibroTest {

    @Test
    void testPatronState_TransicionesCorrectas() {
        Libro libro = new Libro(); // Por defecto es PENDIENTE
        assertEquals("PENDIENTE", libro.getEstado());

        libro.aprobar();
        assertEquals("APROBADO", libro.getEstado());

        assertThrows(IllegalStateException.class, libro::aprobar);
    }

    @Test
    void testPatronState_RechazarLibro() {
        Libro libro = new Libro(); 
        libro.rechazar();
        assertEquals("RECHAZADO", libro.getEstado());

        assertThrows(IllegalStateException.class, libro::rechazar);
        
        // De rechazado puede pasar a aprobado
        libro.aprobar();
        assertEquals("APROBADO", libro.getEstado());
    }

    @Test
    void testPatronPrototype_ClonarCreaCopiaIndependiente() {
        Libro original = new Libro();
        original.setId(10L);
        original.setTitulo("Patrones GoF");

        Libro clon = original.clonar();

        assertNotSame(original, clon);
        assertEquals(original.getId(), clon.getId());
        assertEquals(original.getTitulo(), clon.getTitulo());

        clon.setTitulo("Patrones GoF - 2da Edición");
        assertNotEquals(original.getTitulo(), clon.getTitulo());
    }

    @Test
    void testReducirStock_FallaSiNoHaySuficiente() {
        Libro libro = new Libro();
        libro.setStock(5);

        libro.reducirStock(3);
        assertEquals(2, libro.getStock());

        assertThrows(IllegalStateException.class, () -> libro.reducirStock(5));
    }
}
