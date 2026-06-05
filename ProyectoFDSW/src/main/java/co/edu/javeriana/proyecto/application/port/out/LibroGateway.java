package co.edu.javeriana.proyecto.application.port.out;

import co.edu.javeriana.proyecto.domain.Libro;
import java.util.List;
import java.util.Map;

public interface LibroGateway {
    List<Libro> buscarPorTitulo(String filtro);
    List<Libro> buscarAvanzado(String texto, String categoria, double precioMin, double precioMax, String ordenamiento);
    List<Libro> obtenerTendencias(int limite);
    List<String> obtenerCategorias();
    List<String> obtenerEtiquetas();
    void incrementarClics(Long libroId);
    void guardar(Libro libro);
    void actualizarEstado(Long libroId, String estado);
    java.util.Optional<Libro> buscarPorId(Long id);

    // --- Admin ---
    List<Libro> obtenerTodos();
    void actualizar(Libro libro);
    void eliminar(Long libroId);
    Map<String, Integer> librosPorCategoria();
    List<Libro> obtenerConProblemas(); // libros sin portada, sin ISBN o precio=0
}
