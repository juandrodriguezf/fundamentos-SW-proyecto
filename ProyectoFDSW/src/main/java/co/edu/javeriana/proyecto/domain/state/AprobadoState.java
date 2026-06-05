package co.edu.javeriana.proyecto.domain.state;

import co.edu.javeriana.proyecto.domain.Libro;

public class AprobadoState implements LibroState {
    @Override
    public String getNombreEstado() {
        return "APROBADO";
    }

    @Override
    public void aprobar(Libro libro) {
        throw new IllegalStateException("El libro ya se encuentra aprobado.");
    }

    @Override
    public void rechazar(Libro libro) {
        libro.setEstadoInterno(new RechazadoState());
    }
}
