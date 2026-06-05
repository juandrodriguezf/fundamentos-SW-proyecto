package co.edu.javeriana.proyecto.domain.state;

import co.edu.javeriana.proyecto.domain.Libro;

public class RechazadoState implements LibroState {
    @Override
    public String getNombreEstado() {
        return "RECHAZADO";
    }

    @Override
    public void aprobar(Libro libro) {
        libro.setEstadoInterno(new AprobadoState());
    }

    @Override
    public void rechazar(Libro libro) {
        throw new IllegalStateException("El libro ya se encuentra rechazado.");
    }
}
