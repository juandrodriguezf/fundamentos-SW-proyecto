package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.Usuario;
import co.edu.javeriana.proyecto.domain.exception.CredencialesInvalidasException;
import co.edu.javeriana.proyecto.domain.exception.UsuarioBloqueadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUsuarioUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    private LoginUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUsuarioUseCase(usuarioGateway);
    }

    @Test
    void testEjecutar_LoginExitoso() {
        String password = "password123";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        Usuario usuario = new Usuario(1L, "test@test.com", hash, "Juan", true);

        when(usuarioGateway.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = useCase.ejecutar("test@test.com", password);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(usuarioGateway).actualizarIntentosFallidos("test@test.com", 0);
    }

    @Test
    void testEjecutar_FallaConPasswordIncorrectoYRegistraIntento() {
        String hash = BCrypt.hashpw("password123", BCrypt.gensalt());
        Usuario usuario = new Usuario(1L, "test@test.com", hash, "Juan", true, 0);

        when(usuarioGateway.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar("test@test.com", "malapassword"));

        assertEquals(1, usuario.getIntentosFallidos());
        verify(usuarioGateway).actualizarIntentosFallidos("test@test.com", 1);
    }

    @Test
    void testEjecutar_BloqueaAlTercerIntentoFallido() {
        String hash = BCrypt.hashpw("password123", BCrypt.gensalt());
        Usuario usuario = new Usuario(1L, "test@test.com", hash, "Juan", true, 2);

        when(usuarioGateway.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(UsuarioBloqueadoException.class, () -> useCase.ejecutar("test@test.com", "malapassword"));

        assertFalse(usuario.isActivo());
        assertEquals(3, usuario.getIntentosFallidos());
        verify(usuarioGateway).actualizarActivo("test@test.com", false);
    }

    @Test
    void testEjecutar_RechazaUsuarioYaBloqueado() {
        Usuario usuario = new Usuario(1L, "test@test.com", "hash", "Juan", false, 3);
        when(usuarioGateway.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(UsuarioBloqueadoException.class, () -> useCase.ejecutar("test@test.com", "cualquierpassword"));
        
        // No debería chequear hash ni base de datos más allá de buscarPorEmail
        verify(usuarioGateway, never()).actualizarIntentosFallidos(anyString(), anyInt());
    }
}
