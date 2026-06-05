package co.edu.javeriana.proyecto.application.port.out;

import co.edu.javeriana.proyecto.domain.Categoria;
import java.util.List;

public interface CategoriaGateway {
    List<Categoria> obtenerTodas();
    void guardar(Categoria categoria);
    void actualizar(Categoria categoria);
    void eliminar(Long id);
}
