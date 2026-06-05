package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.exception.CredencialesInvalidasException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import co.edu.javeriana.proyecto.domain.Usuario;

public class CambiarContrasenaUseCase {

    private final UsuarioGateway usuarioGateway;

    public CambiarContrasenaUseCase(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    /**
     * Cambia la contraseña del usuario. Si el usuario estaba bloqueado, también lo reactiva.
     * @param email El email del usuario
     * @param nuevaPassword La nueva contraseña en texto plano
     */
    public void ejecutar(String email, String nuevaPassword) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        Optional<Usuario> optionalUsuario = usuarioGateway.buscarPorEmail(email);
        if (optionalUsuario.isEmpty()) {
            throw new CredencialesInvalidasException("No existe ninguna cuenta con ese correo electrónico.");
        }

        // Cifrar la nueva contraseña con BCrypt
        String nuevoHash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt(10));

        // Actualizar en BD: también resetea intentos y reactiva cuenta si estaba bloqueada
        usuarioGateway.actualizarPassword(email, nuevoHash);

        System.out.println("[SISTEMA] Contraseña actualizada para: " + email);
    }
}
