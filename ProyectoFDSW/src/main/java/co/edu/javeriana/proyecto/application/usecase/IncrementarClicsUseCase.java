package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;

public class IncrementarClicsUseCase {
    private final LibroGateway libroGateway;

    public IncrementarClicsUseCase(LibroGateway libroGateway) {
        this.libroGateway = libroGateway;
    }

    public void ejecutar(Long libroId) {
        libroGateway.incrementarClics(libroId);
    }
}
