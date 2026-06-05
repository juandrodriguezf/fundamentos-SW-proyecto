package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.domain.Libro;
import java.util.List;

public class BuscarLibroAvanzadoUseCase {
    private final LibroGateway libroGateway;

    public BuscarLibroAvanzadoUseCase(LibroGateway libroGateway) {
        this.libroGateway = libroGateway;
    }

    public List<Libro> ejecutar(String texto, String categoria, double precioMin, double precioMax, String ordenamiento) {
        return libroGateway.buscarAvanzado(texto, categoria, precioMin, precioMax, ordenamiento);
    }

    public List<String> obtenerCategorias() {
        return libroGateway.obtenerCategorias();
    }

    public List<String> obtenerEtiquetas() {
        return libroGateway.obtenerEtiquetas();
    }
}
