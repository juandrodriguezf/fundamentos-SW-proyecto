package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CompraGateway;
import co.edu.javeriana.proyecto.domain.Libro;

import java.util.List;
import java.util.stream.Collectors;

public class ObtenerBibliotecaPersonalUseCase {
    private final CompraGateway compraGateway;

    public ObtenerBibliotecaPersonalUseCase(CompraGateway compraGateway) {
        this.compraGateway = compraGateway;
    }

    public List<Libro> ejecutar(Long usuarioId, String filtroBusqueda) {
        List<Libro> misLibros = compraGateway.obtenerLibrosComprados(usuarioId);
        
        if (filtroBusqueda != null && !filtroBusqueda.trim().isEmpty()) {
            String q = filtroBusqueda.toLowerCase();
            misLibros = misLibros.stream()
                .filter(l -> l.getTitulo().toLowerCase().contains(q) || 
                             l.getAutor().toLowerCase().contains(q))
                .collect(Collectors.toList());
        }
        
        return misLibros;
    }
}
