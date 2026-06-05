package co.edu.javeriana.proyecto.application.port.out;

import co.edu.javeriana.proyecto.domain.Compra;
import co.edu.javeriana.proyecto.domain.Libro;
import java.util.List;

public interface CompraGateway {
    void registrarCompra(Compra compra);
    List<Compra> obtenerHistorial(Long usuarioId);
    List<Libro> recomendarPorHistorial(Long usuarioId, int limite);
    List<Libro> recomendarPorLibro(Long libroId, int limite);
    List<Libro> recomendarFallback(int limite);
    List<Libro> obtenerLibrosComprados(Long usuarioId);
}
