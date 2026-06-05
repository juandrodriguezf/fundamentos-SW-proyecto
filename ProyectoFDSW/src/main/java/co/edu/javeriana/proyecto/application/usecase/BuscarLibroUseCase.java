package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.domain.Libro;
import java.util.List;

public class BuscarLibroUseCase {
    private final LibroGateway libroGateway;

    public BuscarLibroUseCase(LibroGateway libroGateway) {
        this.libroGateway = libroGateway;
    }

    public List<Libro> ejecutar(String filtro) {
        return libroGateway.buscarPorTitulo(filtro);
    }
}
