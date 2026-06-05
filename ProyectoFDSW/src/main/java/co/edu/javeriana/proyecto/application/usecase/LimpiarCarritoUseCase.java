package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CarritoGateway;

public class LimpiarCarritoUseCase {
    private final CarritoGateway carritoGateway;

    public LimpiarCarritoUseCase(CarritoGateway carritoGateway) {
        this.carritoGateway = carritoGateway;
    }

    public void ejecutar(String sessionId) {
        carritoGateway.limpiarCarrito(sessionId);
    }
}
