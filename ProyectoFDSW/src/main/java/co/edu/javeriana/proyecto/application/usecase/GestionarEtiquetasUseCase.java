package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.EtiquetaGateway;
import co.edu.javeriana.proyecto.domain.Etiqueta;

import java.util.List;

public class GestionarEtiquetasUseCase {
    private final EtiquetaGateway etiquetaGateway;

    public GestionarEtiquetasUseCase(EtiquetaGateway etiquetaGateway) {
        this.etiquetaGateway = etiquetaGateway;
    }

    public List<Etiqueta> listarTodas() {
        return etiquetaGateway.obtenerTodas();
    }

    public void crear(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío.");
        }
        Etiqueta etiqueta = new Etiqueta(null, nombre.trim().toLowerCase());
        etiquetaGateway.guardar(etiqueta);
    }

    public void actualizar(Etiqueta etiqueta) {
        if (etiqueta.getNombre() == null || etiqueta.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío.");
        }
        etiqueta.setNombre(etiqueta.getNombre().trim().toLowerCase());
        etiquetaGateway.actualizar(etiqueta);
    }

    public void eliminar(Long id) {
        etiquetaGateway.eliminar(id);
    }
}
