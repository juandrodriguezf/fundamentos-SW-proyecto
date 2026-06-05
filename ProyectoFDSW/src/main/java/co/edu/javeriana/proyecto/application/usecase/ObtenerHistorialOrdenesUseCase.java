package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CompraGateway;
import co.edu.javeriana.proyecto.domain.Compra;
import co.edu.javeriana.proyecto.domain.Orden;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObtenerHistorialOrdenesUseCase {
    private final CompraGateway compraGateway;

    public ObtenerHistorialOrdenesUseCase(CompraGateway compraGateway) {
        this.compraGateway = compraGateway;
    }

    public List<Orden> ejecutar(Long usuarioId) {
        List<Compra> historial = compraGateway.obtenerHistorial(usuarioId);
        
        // Agrupar compras por orden_id manteniendo el orden de insercion (cronologico inverso ya viene del Gateway)
        Map<String, List<Compra>> ordenesMap = new LinkedHashMap<>();
        for (Compra compra : historial) {
            ordenesMap.computeIfAbsent(compra.getOrdenId(), k -> new ArrayList<>()).add(compra);
        }

        List<Orden> ordenes = new ArrayList<>();
        for (Map.Entry<String, List<Compra>> entry : ordenesMap.entrySet()) {
            String ordenId = entry.getKey();
            List<Compra> items = entry.getValue();
            // Asumimos que todos los items de la misma orden tienen la misma fecha y usuario
            Orden orden = new Orden(
                ordenId,
                usuarioId,
                items.get(0).getFecha(),
                "Completado", // Simplificacion: Todas las compras guardadas son exitosas
                items
            );
            ordenes.add(orden);
        }

        return ordenes;
    }
}
