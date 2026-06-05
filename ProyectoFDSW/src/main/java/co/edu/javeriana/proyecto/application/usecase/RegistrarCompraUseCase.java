package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CompraGateway;
import co.edu.javeriana.proyecto.domain.Compra;

import java.util.List;

public class RegistrarCompraUseCase {
    private final CompraGateway compraGateway;

    public RegistrarCompraUseCase(CompraGateway compraGateway) {
        this.compraGateway = compraGateway;
    }

    public void ejecutar(List<Compra> compras) {
        for (Compra compra : compras) {
            compraGateway.registrarCompra(compra);
        }
    }
}
