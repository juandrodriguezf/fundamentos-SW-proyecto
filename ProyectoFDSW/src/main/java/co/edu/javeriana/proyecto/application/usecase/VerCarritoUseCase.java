package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CarritoGateway;
import co.edu.javeriana.proyecto.domain.CarritoItem;
import java.util.List;

public class VerCarritoUseCase {
    private final CarritoGateway carritoGateway;

    public VerCarritoUseCase(CarritoGateway carritoGateway) {
        this.carritoGateway = carritoGateway;
    }

    public List<CarritoItem> ejecutar(String sessionId) {
        return carritoGateway.obtenerContenidoCarrito(sessionId);
    }
}
