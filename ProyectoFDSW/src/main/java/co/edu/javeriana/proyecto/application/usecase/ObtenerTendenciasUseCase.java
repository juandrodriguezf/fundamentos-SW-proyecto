package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.domain.Libro;
import java.util.List;

public class ObtenerTendenciasUseCase {
    private final LibroGateway libroGateway;

    public ObtenerTendenciasUseCase(LibroGateway libroGateway) {
        this.libroGateway = libroGateway;
    }

    public List<Libro> ejecutar(int limite) {
        return libroGateway.obtenerTendencias(limite);
    }
}
