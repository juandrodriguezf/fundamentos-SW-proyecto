package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.domain.Libro;

import java.util.List;

public class GestionarLibrosAdminUseCase {
    private final LibroGateway libroGateway;

    public GestionarLibrosAdminUseCase(LibroGateway libroGateway) {
        this.libroGateway = libroGateway;
    }

    public List<Libro> listarTodos() {
        return libroGateway.obtenerTodos();
    }

    public void aprobar(Long libroId) {
        Libro libro = libroGateway.buscarPorId(libroId).orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));
        libro.aprobar();
        libroGateway.actualizarEstado(libroId, libro.getEstado());
    }

    public void rechazar(Long libroId) {
        Libro libro = libroGateway.buscarPorId(libroId).orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));
        libro.rechazar();
        libroGateway.actualizarEstado(libroId, libro.getEstado());
    }

    public void actualizar(Libro libro) {
        libroGateway.actualizar(libro);
    }

    public void eliminar(Long libroId) {
        libroGateway.eliminar(libroId);
    }

    public List<Libro> obtenerConProblemas() {
        return libroGateway.obtenerConProblemas();
    }
}
