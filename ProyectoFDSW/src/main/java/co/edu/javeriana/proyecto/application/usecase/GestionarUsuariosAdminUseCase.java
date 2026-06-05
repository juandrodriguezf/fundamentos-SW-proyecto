package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.domain.Usuario;

import java.util.List;

public class GestionarUsuariosAdminUseCase {
    private final UsuarioGateway usuarioGateway;

    public GestionarUsuariosAdminUseCase(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    public List<Usuario> listarTodos() {
        return usuarioGateway.obtenerTodos();
    }

    public void activar(String email) {
        usuarioGateway.actualizarActivo(email, true);
        usuarioGateway.actualizarIntentosFallidos(email, 0);
    }

    public void desactivar(String email) {
        usuarioGateway.actualizarActivo(email, false);
    }

    public void actualizar(Usuario usuario) {
        usuarioGateway.actualizar(usuario);
    }

    public void eliminar(Long id) {
        usuarioGateway.eliminar(id);
    }
}
