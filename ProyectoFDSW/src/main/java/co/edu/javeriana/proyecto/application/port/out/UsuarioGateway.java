package co.edu.javeriana.proyecto.application.port.out;

import co.edu.javeriana.proyecto.domain.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioGateway {
    void guardar(Usuario usuario);
    boolean existeEmail(String email);
    Optional<Usuario> buscarPorEmail(String email);
    void actualizarIntentosFallidos(String email, int intentos);
    void actualizarActivo(String email, boolean activo);
    void actualizarPassword(String email, String nuevoPasswordHash);

    // --- Admin ---
    List<Usuario> obtenerTodos();
    void actualizar(Usuario usuario);
    void eliminar(Long id);
}
