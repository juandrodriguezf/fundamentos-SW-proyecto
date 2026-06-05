package co.edu.javeriana.proyecto.application.port.out;

import co.edu.javeriana.proyecto.domain.CarritoItem;
import java.util.List;

public interface CarritoGateway {
    void agregarItem(String sessionId, Long libroId, int cantidad);
    void eliminarItem(String sessionId, Long libroId);
    List<CarritoItem> obtenerContenidoCarrito(String sessionId);
    void limpiarCarrito(String sessionId);
}
