package co.edu.javeriana.proyecto.application.port.out;

import co.edu.javeriana.proyecto.domain.Etiqueta;
import java.util.List;

public interface EtiquetaGateway {
    List<Etiqueta> obtenerTodas();
    void guardar(Etiqueta etiqueta);
    void actualizar(Etiqueta etiqueta);
    void eliminar(Long id);
}
