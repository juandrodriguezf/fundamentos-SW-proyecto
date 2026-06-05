package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.Libro;
import co.edu.javeriana.proyecto.domain.Usuario;

import java.util.List;
import java.util.stream.Collectors;

public class ValidarCalidadDatosUseCase {
    private final LibroGateway libroGateway;
    private final UsuarioGateway usuarioGateway;

    public ValidarCalidadDatosUseCase(LibroGateway libroGateway, UsuarioGateway usuarioGateway) {
        this.libroGateway = libroGateway;
        this.usuarioGateway = usuarioGateway;
    }

    /** Libros sin portada, sin ISBN o con precio 0 */
    public List<Libro> obtenerLibrosConProblemas() {
        return libroGateway.obtenerConProblemas();
    }

    /** Usuarios con cuenta desactivada (bloqueados) */
    public List<Usuario> obtenerUsuariosBloqueados() {
        return usuarioGateway.obtenerTodos().stream()
                .filter(u -> !u.isActivo())
                .collect(Collectors.toList());
    }

    /** Usuarios con múltiples intentos fallidos (> 0) */
    public List<Usuario> obtenerUsuariosConIntentosFallidos() {
        return usuarioGateway.obtenerTodos().stream()
                .filter(u -> u.getIntentosFallidos() > 0)
                .collect(Collectors.toList());
    }
}
