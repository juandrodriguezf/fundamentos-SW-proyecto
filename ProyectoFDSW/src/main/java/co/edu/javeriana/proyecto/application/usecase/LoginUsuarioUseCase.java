package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.Usuario;
import co.edu.javeriana.proyecto.domain.exception.CredencialesInvalidasException;
import co.edu.javeriana.proyecto.domain.exception.UsuarioBloqueadoException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class LoginUsuarioUseCase {

    private static final int MAX_INTENTOS = 3;
    private final UsuarioGateway usuarioGateway;

    public LoginUsuarioUseCase(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    /**
     * Intenta hacer login con email y contraseña.
     * @return El Usuario autenticado si es exitoso.
     * @throws UsuarioBloqueadoException si la cuenta ya está bloqueada.
     * @throws CredencialesInvalidasException si la contraseña es incorrecta (incluye intentos restantes).
     */
    public Usuario ejecutar(String email, String rawPassword) {
        Optional<Usuario> optionalUsuario = usuarioGateway.buscarPorEmail(email);

        if (optionalUsuario.isEmpty()) {
            // No revelar si el email existe o no por seguridad
            throw new CredencialesInvalidasException("Credenciales incorrectas. Verifica tu email y contraseña.");
        }

        Usuario usuario = optionalUsuario.get();

        // Verificar si ya está bloqueado
        if (!usuario.puedeIntentarLogin()) {
            throw new UsuarioBloqueadoException("Cuenta bloqueada por múltiples intentos fallidos.");
        }

        // Verificar contraseña con BCrypt
        boolean passwordCorrecta = BCrypt.checkpw(rawPassword, usuario.getPasswordHash());

        if (!passwordCorrecta) {
            // Delegar la lógica de intentos fallidos y bloqueo a la entidad
            usuario.registrarIntentoFallido();
            // Guardamos el usuario modificado
            usuarioGateway.actualizarIntentosFallidos(email, usuario.getIntentosFallidos());
            if (!usuario.isActivo()) {
                usuarioGateway.actualizarActivo(email, false);
                throw new UsuarioBloqueadoException(
                    "Has superado los " + MAX_INTENTOS + " intentos fallidos. " +
                    "Tu cuenta ha sido bloqueada. Usa 'Cambiar contraseña' para recuperarla."
                );
            } else {
                int restantes = MAX_INTENTOS - usuario.getIntentosFallidos();
                throw new CredencialesInvalidasException(
                    "Contraseña incorrecta. Te quedan " + restantes + " intento(s) antes de que tu cuenta sea bloqueada."
                );
            }
        }

        // Login exitoso: resetear intentos y confirmar activo
        usuario.resetearIntentos();
        usuarioGateway.actualizarIntentosFallidos(email, usuario.getIntentosFallidos());
        if (!usuario.isActivo()) {
            usuario.desbloquear();
            usuarioGateway.actualizarActivo(email, true);
        }

        System.out.println("[SISTEMA] Login exitoso para: " + email);
        return usuario;
    }
}
