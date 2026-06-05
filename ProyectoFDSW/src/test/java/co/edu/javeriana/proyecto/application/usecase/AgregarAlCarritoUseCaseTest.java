package co.edu.javeriana.proyecto.application.usecase;

import co.edu.javeriana.proyecto.application.port.out.CarritoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgregarAlCarritoUseCaseTest {

    @Mock
    private CarritoGateway carritoGateway;

    private AgregarAlCarritoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AgregarAlCarritoUseCase(carritoGateway);
    }

    @Test
    void testEjecutar_DelegaAGateway() {
        useCase.ejecutar("session123", 1L, 2);
        verify(carritoGateway, times(1)).agregarAlCarrito("session123", 1L, 2);
    }
}
