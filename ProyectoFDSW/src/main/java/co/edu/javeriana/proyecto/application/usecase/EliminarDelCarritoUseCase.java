package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CarritoGateway;

public class EliminarDelCarritoUseCase {
    private final CarritoGateway carritoGateway;

    public EliminarDelCarritoUseCase(CarritoGateway carritoGateway) {
        this.carritoGateway = carritoGateway;
    }

    public void ejecutar(String sessionId, Long libroId) {
        carritoGateway.eliminarItem(sessionId, libroId);
    }
}
