package co.edu.javeriana.proyecto.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void testRegistrarIntentoFallido_BloqueaAlTercerIntento() {
        Usuario usuario = new Usuario(1L, "test@test.com", "hash", "Juan", true);
        
        usuario.registrarIntentoFallido();
        assertTrue(usuario.isActivo());
        assertEquals(1, usuario.getIntentosFallidos());

        usuario.registrarIntentoFallido();
        assertTrue(usuario.isActivo());
        assertEquals(2, usuario.getIntentosFallidos());

        usuario.registrarIntentoFallido();
        assertFalse(usuario.isActivo());
        assertFalse(usuario.puedeIntentarLogin());
        assertEquals(3, usuario.getIntentosFallidos());
    }

    @Test
    void testDesbloquearUsuario_ReactivaYReseteaIntentos() {
        Usuario usuario = new Usuario(1L, "test@test.com", "hash", "Juan", false, 3);
        
        usuario.desbloquear();
        
        assertTrue(usuario.isActivo());
        assertTrue(usuario.puedeIntentarLogin());
        assertEquals(0, usuario.getIntentosFallidos());
    }
}
