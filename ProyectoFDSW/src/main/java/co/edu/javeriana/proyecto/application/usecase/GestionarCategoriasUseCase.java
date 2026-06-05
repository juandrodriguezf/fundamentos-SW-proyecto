package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CategoriaGateway;
import co.edu.javeriana.proyecto.domain.Categoria;

import java.util.List;

public class GestionarCategoriasUseCase {
    private final CategoriaGateway categoriaGateway;

    public GestionarCategoriasUseCase(CategoriaGateway categoriaGateway) {
        this.categoriaGateway = categoriaGateway;
    }

    public List<Categoria> listarTodas() {
        return categoriaGateway.obtenerTodas();
    }

    public void crear(String nombre, String descripcion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        Categoria categoria = new Categoria(null, nombre.trim(), descripcion != null ? descripcion.trim() : "");
        categoriaGateway.guardar(categoria);
    }

    public void actualizar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        categoriaGateway.actualizar(categoria);
    }

    public void eliminar(Long id) {
        categoriaGateway.eliminar(id);
    }
}
