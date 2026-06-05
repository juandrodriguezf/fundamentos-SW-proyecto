package co.edu.javeriana.proyecto.domain.exception;

public class UsuarioBloqueadoException extends RuntimeException {
    public UsuarioBloqueadoException(String message) {
        super(message);
    }
}
