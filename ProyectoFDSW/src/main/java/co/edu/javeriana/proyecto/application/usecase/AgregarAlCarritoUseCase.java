package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CarritoGateway;

public class AgregarAlCarritoUseCase {
    private final CarritoGateway carritoGateway;

    public AgregarAlCarritoUseCase(CarritoGateway carritoGateway) {
        this.carritoGateway = carritoGateway;
    }

    public void ejecutar(String sessionId, Long libroId, int cantidad) {
        carritoGateway.agregarItem(sessionId, libroId, cantidad);
    }
}
