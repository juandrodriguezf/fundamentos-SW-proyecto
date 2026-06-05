package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.domain.Libro;

public class SubirLibroUseCase {
    private final LibroGateway libroGateway;

    public SubirLibroUseCase(LibroGateway libroGateway) {
        this.libroGateway = libroGateway;
    }

    public void subirLibro(String titulo, String autor, String categoria, double precio, String rutaArchivo) {
        if (rutaArchivo == null || (!rutaArchivo.toLowerCase().endsWith(".pdf") && !rutaArchivo.toLowerCase().endsWith(".epub"))) {
            throw new IllegalArgumentException("Formato no soportado. Use PDF o EPUB.");
        }

        Libro nuevoLibro = new Libro();
        nuevoLibro.setTitulo(titulo);
        nuevoLibro.setAutor(autor);
        nuevoLibro.setCategoria(categoria);
        nuevoLibro.setPrecio(precio);
        nuevoLibro.setRutaArchivo(rutaArchivo);
        nuevoLibro.setEstado("PENDIENTE");
        nuevoLibro.setCalificacionPromedio(0.0);
        nuevoLibro.setClics(0);
        nuevoLibro.setStock(1);

        libroGateway.guardar(nuevoLibro);
    }
}
