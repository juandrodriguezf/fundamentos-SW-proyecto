package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CompraGateway;
import co.edu.javeriana.proyecto.domain.Libro;

import java.util.List;

public class ObtenerRecomendacionesUseCase {
    private final CompraGateway compraGateway;

    public ObtenerRecomendacionesUseCase(CompraGateway compraGateway) {
        this.compraGateway = compraGateway;
    }

    /**
     * Recomendaciones para la pantalla principal ("Recomendados para ti").
     * Si el usuario tiene historial, recomienda por categorias similares.
     * Si no tiene historial (venta en frio), recomienda los mejor calificados.
     */
    public List<Libro> ejecutar(Long usuarioId, int limite) {
        if (usuarioId == null) {
            return compraGateway.recomendarFallback(limite);
        }

        List<Libro> recomendaciones = compraGateway.recomendarPorHistorial(usuarioId, limite);

        if (recomendaciones.isEmpty()) {
            return compraGateway.recomendarFallback(limite);
        }

        return recomendaciones;
    }

    /**
     * "Usuarios que compraron este libro tambien compraron..."
     */
    public List<Libro> porLibro(Long libroId, int limite) {
        return compraGateway.recomendarPorLibro(libroId, limite);
    }
}
