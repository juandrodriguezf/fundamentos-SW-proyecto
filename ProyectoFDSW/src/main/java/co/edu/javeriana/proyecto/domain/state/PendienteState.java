package co.edu.javeriana.proyecto.domain.state;

import co.edu.javeriana.proyecto.domain.Libro;

public class PendienteState implements LibroState {
    @Override
    public String getNombreEstado() {
        return "PENDIENTE";
    }

    @Override
    public void aprobar(Libro libro) {
        libro.setEstadoInterno(new AprobadoState());
    }

    @Override
    public void rechazar(Libro libro) {
        libro.setEstadoInterno(new RechazadoState());
    }
}
