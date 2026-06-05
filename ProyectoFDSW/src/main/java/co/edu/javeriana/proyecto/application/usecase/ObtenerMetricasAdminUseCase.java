package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.AdminGateway;
import co.edu.javeriana.proyecto.domain.MetricasAdmin;

public class ObtenerMetricasAdminUseCase {
    private final AdminGateway adminGateway;

    public ObtenerMetricasAdminUseCase(AdminGateway adminGateway) {
        this.adminGateway = adminGateway;
    }

    public MetricasAdmin ejecutar() {
        return adminGateway.obtenerMetricas();
    }
}
