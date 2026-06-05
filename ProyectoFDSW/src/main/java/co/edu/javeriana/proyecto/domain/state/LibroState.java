package co.edu.javeriana.proyecto.domain.state;

import co.edu.javeriana.proyecto.domain.Libro;

public interface LibroState {
    String getNombreEstado();
    void aprobar(Libro libro);
    void rechazar(Libro libro);
}
