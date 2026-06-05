package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.Usuario;
import co.edu.javeriana.proyecto.domain.exception.UsuarioYaExisteException;
import org.mindrot.jbcrypt.BCrypt;

public class RegistrarUsuarioUseCase {
    private final UsuarioGateway usuarioGateway;

    public RegistrarUsuarioUseCase(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    public void ejecutar(String email, String rawPassword, String nombre) {
        // Validar reglas de negocio básicas (el controlador también hace algunas, pero aquí es la fuente de verdad)
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        
        // Validar existencia
        if (usuarioGateway.existeEmail(email)) {
            throw new UsuarioYaExisteException("El correo " + email + " ya está registrado.");
        }

        // Cifrar contraseña con BCrypt
        String passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));

        // Crear usuario por defecto activo
        Usuario nuevoUsuario = new Usuario(null, email, passwordHash, nombre, true);

        // Guardar
        usuarioGateway.guardar(nuevoUsuario);
        
        // Simular envío de correo
        System.out.println("[SISTEMA] ¡Correo de bienvenida simulado enviado a " + email + "!");
    }
}
