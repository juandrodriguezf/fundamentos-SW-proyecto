package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;

public class AprobarLibroUseCase {
    private final LibroGateway libroGateway;

    public AprobarLibroUseCase(LibroGateway libroGateway) {
        this.libroGateway = libroGateway;
    }

    public void aprobarLibro(Long libroId) {
        libroGateway.actualizarEstado(libroId, "APROBADO");
    }
}
